package top.itning.yunshu.yunshunas.entity;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Email 配置
 *
 * @author itning
 */
@ConfigurationProperties(prefix = "nas")
@Component
public class NasProperties {
    /**
     * 文件输出目录
     */
    private String outDir;
    /**
     * ffmpeg bin 目录
     */
    private String ffmpegBinDir;
    /**
     * aria2c所在目录
     */
    private String aria2cDir;

    public String getOutDir() {
        return outDir;
    }

    public void setOutDir(String outDir) {
        this.outDir = outDir;
    }

    public String getFfmpegBinDir() {
        return ffmpegBinDir;
    }

    public void setFfmpegBinDir(String ffmpegBinDir) {
        this.ffmpegBinDir = ffmpegBinDir;
    }

    public String getAria2cDir() {
        return aria2cDir;
    }

    public void setAria2cDir(String aria2cDir) {
        this.aria2cDir = aria2cDir;
    }
}
