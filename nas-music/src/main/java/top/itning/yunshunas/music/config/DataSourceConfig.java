package top.itning.yunshunas.music.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.music.datasource.CoverDataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.impl.FileDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;
import top.itning.yunshunas.music.datasource.impl.TencentCosDataSource;

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
            return new TencentCosDataSource(nasProperties);
        }
        return new FileDataSource(nasProperties);
    }

    @Bean
    public LyricDataSource lyricDataSource(NasProperties nasProperties) {
        if (nasProperties.isEnableTencentCosDataSource()) {
            return new TencentCosDataSource(nasProperties);
        }
        return new FileDataSource(nasProperties);
    }

    @Bean
    public CoverDataSource coverDataSource(NasProperties nasProperties) {
        if (nasProperties.isEnableTencentCosDataSource()) {
            return new TencentCosDataSource(nasProperties);
        }
        return new FileDataSource(nasProperties);
    }
}
