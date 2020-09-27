package top.itning.yunshu.yunshunas.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author itning
 * @date 2020/9/23 18:48
 */
@ConfigurationProperties(prefix = "music")
@Component
@Data
public class MusicConfigProperties {
    /**
     * 音乐文件夹路径
     */
    private String filePath;
}
