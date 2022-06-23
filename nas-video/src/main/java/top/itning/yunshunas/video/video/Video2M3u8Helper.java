package top.itning.yunshunas.video.video;

import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;
import top.itning.yunshunas.common.util.Tuple2;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import static com.jayway.jsonpath.Criteria.where;
import static com.jayway.jsonpath.Filter.filter;
import static top.itning.yunshunas.common.util.CommandUtils.process;

/**
 * ffmpeg 4.1.3 版本测试通过
 *
 * @author itning
 * @since 2019/7/13 23:12
 */
public class Video2M3u8Helper {
    private static final Logger logger = LoggerFactory.getLogger(Video2M3u8Helper.class);

    /**
     * 视频编码
     */
    private static final String VIDEO_H_264 = "Video: h264";
    /**
     * 音频编码
     */
    private static final String AUDIO_AAC = "Audio: aac";
    /**
     * 进度起始字符串
     */
    private static final String START_FRAME_STR = "frame=";
    /**
     * 进度结束字符串
     */
    private static final String END_FRAME_STR = "fps";
    /**
     * 进度百分比格式化
     */
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00%");
    /**
     * 视频分割时间
     */
    private static final String SPLIT_TIME_SECOND = "10";


    private final String ffmpegLocation;
    private final String ffprobeLocation;
    private Progress progress;

    public Video2M3u8Helper(String ffmpegBinDir) {
        this.ffmpegLocation = ffmpegBinDir + File.separator + "ffmpeg";
        this.ffprobeLocation = ffmpegBinDir + File.separator + "ffprobe";
    }

    /**
     * 进度条
     */
    public interface Progress {
        /**
         * 视频准备开始转码时回调
         *
         * @param fromFile 源文件
         * @param toPath   目标路径
         * @param fileName 文件名（不要扩展名）
         */
        default void onStart(String fromFile, String toPath, String fileName) {

        }

        /**
         * 日志
         *
         * @param line 每条日志
         */
        default void onLine(String line) {
        }

        /**
         * 视频转换完成
         *
         * @param fromFile 源文件
         * @param toPath   目标路径
         * @param fileName 文件名（不要扩展名）
         */
        default void onFinish(String fromFile, String toPath, String fileName) {
        }

        /**
         * 视频转换错误时
         *
         * @param e        出现的异常
         * @param fromFile 源文件
         * @param toPath   目标路径
         * @param fileName 文件名（不要扩展名）
         */
        default void onError(Exception e, String fromFile, String toPath, String fileName) {
        }

        /**
         * 转换进度
         *
         * @param frame       当前帧数
         * @param totalFrames 总帧数
         * @param percentage  百分比
         * @param line        原始消息
         */
        default void onProgress(long frame, long totalFrames, String percentage, String line) {
        }
    }

    /**
     * 视频文件转码成符合HLS规范的视频文件
     *
     * @param fromFile  源文件
     * @param toPath    目标文件夹
     * @param copyVideo 是否直接复制，不进行视频转码
     * @param copyAudio 是否直接复制，不进行音频转码
     * @return 转码完成的文件路径
     * @throws IOException IOException
     */
    private String videoStandardization(String fromFile, String toPath, boolean copyVideo, boolean copyAudio) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("3/4. start copy {} {} {} {}", fromFile, toPath, copyAudio, copyAudio);
        }
        final long videoFrames = getVideoFrames(fromFile);
        String randomFileName = DigestUtils.md5DigestAsHex(fromFile.getBytes()) + ".mp4";
        List<String> command = new ArrayList<>(8);
        command.add(ffmpegLocation);
        command.add("-i");
        command.add(fromFile);
        command.add("-vcodec");
        if (copyVideo) {
            command.add("copy");
        } else {
            command.add("h264");
        }
        command.add("-acodec");
        if (copyAudio) {
            command.add("copy");
        } else {
            command.add("aac");
        }
        command.add(toPath + File.separator + randomFileName);

        process(command, line -> {
            if (progress != null) {
                progress.onLine(line);
                progress(line, videoFrames);
            }
        });
        if (logger.isDebugEnabled()) {
            logger.debug("4/4. end copy {} {} {} {}", fromFile, toPath, copyAudio, copyAudio);
        }
        return command.get(command.size() - 1);
    }

    /**
     * 转换视频文件为M3U8
     *
     * @param fromFile 源文件
     * @param toPath   目标路径
     * @param fileName 文件名（不要扩展名）
     * @param progress 进度条
     */
    public void videoConvert(final String fromFile, final String toPath, final String fileName, Progress progress) {
        if (logger.isDebugEnabled()) {
            logger.debug("start videoConvert {} {} {}", fromFile, toPath, fileName);
        }
        this.progress = progress;
        try {
            if (progress != null) {
                progress.onStart(fromFile, toPath, fileName);
            }
            Tuple2<Boolean, Boolean> compliance = checkComplianceWithSpecificationsForHls(fromFile);
            if (logger.isDebugEnabled()) {
                logger.debug("video: {} audio: {}", compliance.t1(), compliance.t2());
            }
            String copy = videoStandardization(fromFile, toPath, compliance.t1(), compliance.t2());
            // 构建命令
            List<String> command = new ArrayList<>(16);
            command.add(ffmpegLocation);
            command.add("-i");
            command.add(copy);
            command.add("-codec");
            command.add("copy");
            command.add("-vbsf");
            command.add("h264_mp4toannexb");
            command.add("-map");
            command.add("0");
            command.add("-f");
            command.add("segment");
            command.add("-segment_list");
            command.add(toPath + File.separator + fileName + ".m3u8");
            command.add("-segment_time");
            command.add(SPLIT_TIME_SECOND);
            command.add(toPath + File.separator + fileName + "-%03d.ts");

            process(command, line -> {
                if (progress != null) {
                    progress.onLine(line);
                }
            });

            boolean delete = new File(toPath + File.separator + DigestUtils.md5DigestAsHex(fromFile.getBytes()) + ".mp4").delete();
            if (logger.isDebugEnabled()) {
                logger.debug("delete fromFile copy file {}", delete);
                logger.debug("end videoConvert {} {} {}", fromFile, toPath, fileName);
            }
            if (progress != null) {
                progress.onFinish(fromFile, toPath, fileName);
            }
        } catch (Exception e) {
            if (logger.isDebugEnabled()) {
                logger.debug("video convert exception: {}", e.getMessage());
            }
            if (progress != null) {
                progress.onError(e, fromFile, toPath, fileName);
            }
        }
    }

    /**
     * 转换视频文件为M3U8
     *
     * @param fromFile 源文件
     * @param toPath   目标路径
     * @param fileName 文件名（不要扩展名）
     */
    public void videoConvert(String fromFile, String toPath, String fileName) {
        videoConvert(fromFile, toPath, fileName, null);
    }

    /**
     * 检查视频文件是否符合HLS规范
     *
     * @param wantCheckVideoFile 想要检查的视频文件
     * @return 1.视频符合？ 2.音频符合？
     * @throws IOException IOException
     */
    private Tuple2<Boolean, Boolean> checkComplianceWithSpecificationsForHls(String wantCheckVideoFile) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("1/4. start checkComplianceWithSpecificationsForHls {}", wantCheckVideoFile);
        }
        List<String> command = new ArrayList<>(3);
        command.add(ffmpegLocation);
        command.add("-i");
        command.add(wantCheckVideoFile);
        StringBuilder stringBuilder = new StringBuilder();
        process(command, stringBuilder::append);
        String s = stringBuilder.toString();
        boolean video = s.contains(VIDEO_H_264);
        boolean audio = s.contains(AUDIO_AAC);
        if (logger.isDebugEnabled()) {
            logger.debug("2/4. end checkComplianceWithSpecificationsForHls {}", wantCheckVideoFile);
        }
        //音视频都是HLS规范
        if (video && audio) {
            return new Tuple2<>(true, true);
        }
        //视频不符合HLS规范，音频符合
        else if (!video && audio) {
            return new Tuple2<>(false, true);
        }
        //音频不符合HLS规范，视频符合
        else if (video) {
            return new Tuple2<>(true, false);
        } else {
            return new Tuple2<>(false, false);
        }
    }

    /**
     * 进度条
     *
     * @param line        输出的信息
     * @param totalFrames 视频总共帧数
     */
    private void progress(String line, final long totalFrames) {
        if (progress != null && totalFrames != -1 && line.startsWith(START_FRAME_STR)) {
            int j = line.indexOf(END_FRAME_STR);
            String frame = line.substring(6, j).trim();
            long f = NumberUtils.toLong(frame, -1);
            String percentage = DECIMAL_FORMAT.format((double) f / (double) totalFrames);
            progress.onProgress(f, totalFrames, percentage, line);
        } else if (progress != null) {
            progress.onProgress(-1, totalFrames, null, line);
        }
    }

    /**
     * 获取视频帧数
     *
     * @param videoFile 视频文件
     * @return 帧数（字符串转长整形失败会返回-1）
     * @throws IOException IOException
     */
    private long getVideoFrames(String videoFile) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("start getVideoFrames");
        }
        List<String> command = new ArrayList<>(8);
        command.add(ffprobeLocation);
        command.add("-v");
        command.add("quiet");
        command.add("-print_format");
        command.add("json");
        command.add("-show_format");
        command.add("-show_streams");
        command.add(videoFile);
        StringBuilder stringBuilder = new StringBuilder();
        process(command, stringBuilder::append);
        String s = stringBuilder.toString();

        Filter videoFilter = filter(where("codec_type").is("video"));
        JSONArray read = JsonPath.read(s, "$.streams[?].nb_frames", videoFilter);
        if (logger.isDebugEnabled()) {
            logger.debug("end getVideoFrames");
        }
        if (read.isEmpty()) {
            if (logger.isDebugEnabled()) {
                logger.debug("json array is empty");
            }
            return -1;
        } else {
            return NumberUtils.toLong(read.get(0).toString(), -1);
        }
    }
}
