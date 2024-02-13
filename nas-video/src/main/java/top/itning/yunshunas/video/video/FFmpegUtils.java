package top.itning.yunshunas.video.video;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import top.itning.yunshunas.common.util.CommandUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @author itning
 * @since 2024/2/12 13:42
 */
@Slf4j
public class FFmpegUtils {
    /**
     * 获取视频文件的视频编码信息（第一个视频流）
     *
     * @param ffprobePath ffprobe文件路径
     * @param file        视频文件
     * @return 编码信息
     * @throws IOException 执行失败
     */
    public static String getVideoCodec(String ffprobePath, String file) throws IOException {
        return CommandUtils.process(Arrays.asList(
                ffprobePath,
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=codec_name",
                "-of", "csv=p=0",
                file
        ));
    }

    /**
     * 获取视频文件的音频编码信息（第一个音频流）
     *
     * @param ffprobePath ffprobe文件路径
     * @param file        视频文件
     * @return 编码信息
     * @throws IOException 执行失败
     */
    public static String getAudioCodec(String ffprobePath, String file) throws IOException {
        return CommandUtils.process(Arrays.asList(
                ffprobePath,
                "-v", "error",
                "-select_streams", "a:0",
                "-show_entries", "stream=codec_name",
                "-of", "csv=p=0",
                file
        ));
    }

    /**
     * 获取视频文件的总帧数信息（第一个视频流）
     *
     * @param ffprobePath ffprobe文件路径
     * @param file        视频文件
     * @return 帧数信息，获取失败时返回-1
     * @throws IOException 执行失败
     */
    public static long getVideoTotalFrame(String ffprobePath, String file) throws IOException {
        return NumberUtils.toLong(CommandUtils.process(Arrays.asList(
                ffprobePath,
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=nb_frames",
                "-of", "csv=p=0",
                file
        )), -1);
    }

    /**
     * 获取视频文件的平均码率信息（第一个视频流）
     *
     * @param ffprobePath ffprobe文件路径
     * @param file        视频文件
     * @return 码率信息，获取失败时返回-1
     * @throws IOException 执行失败
     */
    public static long getVideoBitRate(String ffprobePath, String file) throws IOException {
        return NumberUtils.toLong(CommandUtils.process(Arrays.asList(
                ffprobePath,
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=bit_rate",
                "-of", "csv=p=0",
                file
        )), -1);
    }

    /**
     * 转换视频文件
     *
     * @param ffmpegPath  ffmpeg文件路径
     * @param file        视频文件
     * @param outFileName 输出文件名
     * @param params      参数
     * @param commandInfo 输出信息
     * @throws IOException 转换失败
     */
    public static void converterVideo(String ffmpegPath,
                                      String file,
                                      String outFileName,
                                      ConverterParams params,
                                      Consumer<String> commandInfo) throws IOException {
        List<String> command = new ArrayList<>();
        command.add(ffmpegPath);
        command.add("-y");
        command.add("-loglevel");
        command.add("quiet");
        command.add("-i");
        command.add(file);
        command.add("-codec:v");
        command.add(Optional.ofNullable(params.getVideoCodec()).orElse("copy"));
        if (null != params.getVideoCodec()) {
            Optional.ofNullable(params.getVideoBitRate()).ifPresent(videoBitRate -> {
                command.add("-b:v");
                command.add(videoBitRate);
            });
            Optional.ofNullable(params.getMinVideoBitRate()).ifPresent(minVideoBitRate -> {
                command.add("-minrate");
                command.add(minVideoBitRate);
            });
            Optional.ofNullable(params.getMinVideoBitRate()).ifPresent(maxVideoBitRate -> {
                command.add("-maxrate");
                command.add(maxVideoBitRate);
            });
            Optional.ofNullable(params.getMinVideoBitRate()).ifPresent(videoBufSize -> {
                command.add("-bufsize");
                command.add(videoBufSize);
            });
        }
        command.add("-codec:a");
        command.add(Optional.ofNullable(params.getAudioCodec()).orElse("copy"));
        if (null != params.getAudioCodec()) {
            Optional.ofNullable(params.getAudioBitRate()).ifPresent(audioBitRate -> {
                command.add("-b:a");
                command.add(audioBitRate);
            });
        }
        if (Optional.ofNullable(params.getCover2hls()).orElse(true)) {
            command.add("-hls_time");
            command.add(Optional.ofNullable(params.getHlsTime()).orElse(10).toString());
            command.add("-hls_list_size");
            command.add(Optional.ofNullable(params.getHlsListSize()).orElse(0).toString());
            command.add("-start_number");
            command.add(Optional.ofNullable(params.getStartNumber()).orElse(0).toString());
            command.add("-f");
            command.add("hls");
            command.add("-hls_segment_filename");
            command.add(Optional.ofNullable(params.getHlsSegmentFileName()).orElse("output_%03d.ts"));
        }
        command.add(outFileName);
        command.add("-progress");
        command.add("-");
        command.add("-nostats");
        log.info("The final command: {}", String.join(" ", command));
        //- `frame=388`：已处理的帧数。这表示 FFmpeg 已经读取、解码、可能重新编码和写入了388帧。
        //- `fps=127.16`：当前的处理速度，以帧每秒（Frames Per Second）计。这里的意思是 FFmpeg 正在以每秒处理约127.16帧的速度运行。
        //- `stream_0_0_q=29.0`：当前视频流的量化参数。量化参数越低，输出视频质量越高（但文件大小也越大）。这个参数的具体含义可能会根据使用的编解码器有所不同。
        //- `bitrate=1104.8kbits/s`：当前的平均比特率，以千比特每秒（kilobits per second）计。这表示视频（和可能的音频）数据的平均数据传输速率。
        //- `total_size=2097200`：到目前为止生成的输出文件大小，以字节为单位。
        //- `out_time_us=15185850`：到目前为止的输出时长，以微秒为单位。
        //- `out_time_ms=15185850`：到目前为止的输出时长，以毫秒为单位。与 `out_time_us` 相同，只不过单位不同。
        //- `out_time=00:00:15.185850`：到目前为止的输出时长，以时:分:秒.微秒的格式表示。这里显示的是已经处理了大约15秒的视频。
        //- `dup_frames=0`：到目前为止，由于编码过程中的某些需求（例如帧率转换），被复制的帧数。这里的0表示没有帧被复制。
        //- `drop_frames=0`：到目前为止，被丢弃的帧数。帧可能会因为多种原因被丢弃，例如保持同步或适应目标帧率。这里的0表示没有帧被丢弃。
        //- `speed=4.98x`：处理速度相对于实时播放速度的比率。这里的4.98x意味着 FFmpeg 正在以实时速度的近5倍处理视频。
        //- `progress=continue`：这个输出指示处理的当前状态。这里可能是一个打印错误，通常情况下应该是 `progress=continue` 表示处理正在继续。如果处理完成，这里会显示 `progress=end`。
        CommandUtils.process(command, commandInfo);
    }

    @Data
    @Builder
    public static class ConverterParams {
        private Boolean cover2hls;
        private String videoCodec;
        private String audioCodec;
        private Integer startNumber;
        private Integer hlsTime;
        private Integer hlsListSize;
        private String hlsSegmentFileName;
        private String videoBitRate;
        private String audioBitRate;
        private String minVideoBitRate;
        private String maxVideoBitRate;
        private String videoBufSize;

        public static ConverterParams toHls() {
            return ConverterParams.builder()
                    .cover2hls(true)
                    .videoCodec("libx264")
                    .audioCodec("aac")
                    .videoBitRate("5000k")
                    .minVideoBitRate("2000k")
                    .maxVideoBitRate("10000k")
                    .videoBufSize("5000k")
                    .build();
        }

        public static ConverterParams defaultParams() {
            return ConverterParams.builder()
                    .cover2hls(false)
                    .videoCodec("libx264")
                    .audioCodec("aac")
                    .videoBitRate("5000k")
                    .minVideoBitRate("2000k")
                    .maxVideoBitRate("10000k")
                    .videoBufSize("5000k")
                    .build();
        }
    }
}
