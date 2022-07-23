package top.itning.yunshunas.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

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
     * 文件数据源配置
     */
    private FileDataSourceConfig fileDataSource;

    /**
     * 基础认证配置
     */
    private BasicAuthConfig basicAuth;

    /**
     * 使用腾讯云COS数据源
     */
    private TencentCosDataSourceConfig tencentCosDataSource;

    /**
     * 支持备份的文件数据源
     */
    private BackupFileDataSourceConfig backupFileDataSource;

    /**
     * 使用混合数据源：音乐数据写文件，歌词和封面写文件和腾讯云
     * 歌词和封面从腾讯云获取，音乐文件从文件获取
     */
    private boolean enableMixedDataSource;

    /**
     * 开启支持备份的文件数据源
     *
     * @return 是否开启
     */
    public boolean isEnableBackupFileDataSource() {
        return Objects.nonNull(backupFileDataSource);
    }

    /**
     * 开启腾讯云数据源
     *
     * @return 是否开启
     */
    public boolean isEnableTencentCosDataSource() {
        return Objects.nonNull(tencentCosDataSource);
    }

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

    @Data
    public static class TencentCosDataSourceConfig {

        /**
         * SECRETID和SECRETKEY请登录<a href="https://console.cloud.tencent.com/cam/capi">访问管理控制台</a>进行查看和管理
         */
        private String secretId;

        /**
         * SECRETID和SECRETKEY请登录<a href="https://console.cloud.tencent.com/cam/capi">访问管理控制台</a>进行查看和管理
         */
        private String secretKey;

        /**
         * 设置 bucket 的地域, COS 地域的简称请参照<a href="https://cloud.tencent.com/document/product/436/6224">这里</a>
         */
        private String regionName;

        /**
         * 腾讯COS BucketName
         */
        private String bucketName;

        /**
         * 腾讯云内容分发网络（CDN）域名
         */
        private String cdnUrl;
    }

    @Data
    public static class FileDataSourceConfig {
        /**
         * 备份音乐文件目录
         */
        private String musicFileDir;
        /**
         * 备份歌词文件目录
         */
        private String lyricFileDir;

        /**
         * 文件数据源URL前缀
         */
        private String urlPrefix;
    }

    @Data
    public static class BackupFileDataSourceConfig {
        /**
         * 备份音乐文件目录
         */
        private String musicFileDir;
        /**
         * 备份歌词文件目录
         */
        private String lyricFileDir;
    }
}
