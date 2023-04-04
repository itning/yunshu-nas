package top.itning.yunshunas.music.util;

import org.apache.commons.lang3.StringUtils;
import top.itning.yunshunas.common.util.CommandUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author itning
 * @since 2023/4/4 19:44
 */
public class FFmpegUtils {
    /**
     * 修改音频文件的元数据信息
     *
     * @param ffmpegBinDir ffmpeg bin目录
     * @param inputPath    输入文件路径
     * @param outputPath   输出文件路径
     * @param title        歌曲名
     * @param artist       艺术家名
     * @param album        专辑名
     * @param coverPath    封面图片路径
     * @param resultInfo   处理结果信息
     * @throws IOException 处理失败
     */
    public static void modifyMetadata(String ffmpegBinDir, String inputPath, String outputPath, String title, String artist, String album, String coverPath, Consumer<String> resultInfo) throws IOException {

        List<String> command = new ArrayList<>();
        command.add(ffmpegBinDir + File.separator + "ffmpeg");
        command.add("-i");
        command.add(inputPath);
        command.add("-map_metadata");
        command.add("0");
        command.add("-codec");
        command.add("copy");
        command.add("-id3v2_version");
        command.add("3");

        if (StringUtils.isNotBlank(title)) {
            command.add("-metadata");
            command.add("title=" + title);
        }

        if (StringUtils.isNotBlank(artist)) {
            command.add("-metadata");
            command.add("artist=" + artist);
        }

        if (StringUtils.isNotBlank(artist)) {
            command.add("-metadata");
            command.add("album=" + album);
        }

        if (StringUtils.isNotBlank(coverPath)) {
            command.add("-metadata");
            command.add("picture=" + coverPath);
        }

        command.add(outputPath);

        CommandUtils.process(command, resultInfo);
    }
}
