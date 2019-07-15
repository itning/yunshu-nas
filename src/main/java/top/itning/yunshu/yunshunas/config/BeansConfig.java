package top.itning.yunshu.yunshunas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.itning.yunshu.yunshunas.entity.NasProperties;
import top.itning.yunshu.yunshunas.video.Video2M3u8Helper;

/**
 * @author itning
 * @date 2019/7/14 15:35
 */
@Configuration
public class BeansConfig {
    private final NasProperties nasProperties;

    public BeansConfig(NasProperties nasProperties) {
        this.nasProperties = nasProperties;
    }

    @Bean
    public Video2M3u8Helper video2M3u8Helper() {
        return new Video2M3u8Helper(nasProperties.getFfmpegBinDir());
    }
}
