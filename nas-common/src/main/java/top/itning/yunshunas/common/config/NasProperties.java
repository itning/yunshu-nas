package top.itning.yunshunas.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Email 配置
 *
 * @author itning
 */
@ConfigurationProperties(prefix = "nas")
@Component
@Data
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
     * aria2c文件
     */
    private String aria2cFile;
    /**
     * 音乐文件目录
     */
    private String musicFileDir;
    /**
     * 歌词文件目录
     */
    private String lyricFileDir;

    /**
     * 开启基础认证
     */
    private boolean enableBasicAuth;

    /**
     * 基础认证用户名
     */
    private String basicAuthUsername;

    /**
     * 基础认证密码
     */
    private String basicAuthPassword;

    /**
     * 基础认证忽略路径
     */
    private List<String> ignorePath;
}
