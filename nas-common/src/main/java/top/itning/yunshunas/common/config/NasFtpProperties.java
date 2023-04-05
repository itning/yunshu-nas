package top.itning.yunshunas.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * FTP 配置
 *
 * @author itning
 */
@ConfigurationProperties(prefix = "nas.ftp")
@Component
@Data
public class NasFtpProperties {

    private Map<String, FtpConfig> config;

    @Data
    public static class FtpConfig {
        /**
         * 监听端口 默认21
         */
        private int port = 21;
        /**
         * 监听地址 默认本地地址
         */
        private String serverAddress;
        /**
         * 用户信息
         */
        private List<User> users;
    }

    @Data
    public static class User {
        /**
         * 开启匿名访问
         * 开启后不需要配置用户名密码
         */
        private boolean enableAnonymousAccess;
        /**
         * 用户名
         */
        private String username;
        /**
         * 密码
         */
        private String password;
        /**
         * 该用户的根目录
         */
        private File homeDir;
    }
}
