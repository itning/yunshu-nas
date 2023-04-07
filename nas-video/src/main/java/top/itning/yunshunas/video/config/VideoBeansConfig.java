package top.itning.yunshunas.video.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.common.db.DbSourceConfig;
import top.itning.yunshunas.video.video.Video2M3u8Helper;

import java.util.Optional;

/**
 * @author itning
 * @since 2019/7/14 15:35
 */
@Configuration
public class VideoBeansConfig {
    private final NasProperties nasProperties;

    @Autowired
    public VideoBeansConfig(DbSourceConfig dbSourceConfig) {
        this.nasProperties = Optional.ofNullable(dbSourceConfig.getSetting(NasProperties.class)).orElse(new NasProperties());
    }

    @Bean
    public Video2M3u8Helper video2M3u8Helper() {
        return new Video2M3u8Helper(nasProperties.getFfmpegBinDir());
    }
}
