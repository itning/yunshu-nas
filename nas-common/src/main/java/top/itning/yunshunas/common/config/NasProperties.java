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

    /**
     * 使用腾讯云COS数据源
     */
    private boolean enableTencentCosDataSource;

    /**
     * SECRETID和SECRETKEY请登录访问管理控制台 https://console.cloud.tencent.com/cam/capi 进行查看和管理
     */
    private String tencentCosSecretId;

    /**
     * SECRETID和SECRETKEY请登录访问管理控制台 https://console.cloud.tencent.com/cam/capi 进行查看和管理
     */
    private String tencentCosSecretKey;

    /**
     * 设置 bucket 的地域, COS 地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
     */
    private String tencentCosRegionName;

    /**
     * 腾讯COS BucketName
     */
    private String tencentCosBucketName;

    /**
     * 腾讯云内容分发网络（CDN）域名
     */
    private String tencentCosCdnUrl;

    /**
     * 文件数据源URL前缀
     */
    private String fileDataSourceUrlPrefix;
}
