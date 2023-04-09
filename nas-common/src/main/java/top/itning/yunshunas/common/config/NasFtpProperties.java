package top.itning.yunshunas.common.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.File;
import java.util.List;

/**
 * FTP 配置
 *
 * @author itning
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NasFtpProperties {

    private List<FtpConfig> config;

    @Data
    public static class FtpConfig {
        /**
         * Ftp服务名称
         */
        private String name;
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
