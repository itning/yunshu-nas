package top.itning.yunshunas.common.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.List;
import java.util.Objects;

/**
 * Email 配置
 *
 * @author itning
 */
@ConfigurationProperties(prefix = "nas")
@Component
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NasProperties {

    private String defaultDbPath;

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
     * 基础认证配置
     */
    private BasicAuthConfig basicAuth;

    /**
     * 服务端地址
     */
    private URL serverUrl;

    /**
     * 开启基础认证
     *
     * @return 是否开启
     */
    public boolean isEnableBasicAuth() {
        return Objects.nonNull(basicAuth);
    }

    @Data
    public static class BasicAuthConfig {
        /**
         * 基础认证用户名
         */
        private String username;

        /**
         * 基础认证密码
         */
        private String password;

        /**
         * 基础认证忽略路径
         */
        private List<String> ignorePath;
    }
}
