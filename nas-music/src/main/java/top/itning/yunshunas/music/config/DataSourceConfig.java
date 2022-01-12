package top.itning.yunshunas.music.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.impl.FileMusicAndLyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;
import top.itning.yunshunas.music.datasource.impl.TencentCosMusicAndLyricDataSource;

/**
 * 数据源配置
 *
 * @author itning
 * @since 2022/1/12 13:01
 */
@Configuration
public class DataSourceConfig {

    @Bean
    public MusicDataSource musicDataSource(NasProperties nasProperties) {
        if (nasProperties.isEnableTencentCosDataSource()) {
            return new TencentCosMusicAndLyricDataSource(nasProperties);
        }
        return new FileMusicAndLyricDataSource(nasProperties);
    }

    @Bean
    public LyricDataSource lyricDataSource(NasProperties nasProperties) {
        if (nasProperties.isEnableTencentCosDataSource()) {
            return new TencentCosMusicAndLyricDataSource(nasProperties);
        }
        return new FileMusicAndLyricDataSource(nasProperties);
    }
}
