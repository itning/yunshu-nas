package top.itning.yunshunas.music.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.music.datasource.CoverDataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;
import top.itning.yunshunas.music.datasource.impl.FileDataSource;
import top.itning.yunshunas.music.datasource.impl.MixedDataSource;
import top.itning.yunshunas.music.datasource.impl.TencentCosDataSource;

/**
 * 数据源配置
 *
 * @author itning
 * @since 2022/1/12 13:01
 */
@Slf4j
@Configuration
public class DataSourceConfig {

    @Bean
    public MusicDataSource musicDataSource(NasProperties nasProperties) {
        MusicDataSource musicDataSource;
        if (nasProperties.isEnableMixedDataSource()) {
            musicDataSource = new MixedDataSource(nasProperties);
        } else if (nasProperties.isEnableTencentCosDataSource()) {
            musicDataSource = new TencentCosDataSource(nasProperties);
        } else {
            musicDataSource = new FileDataSource(nasProperties);
        }
        log.info("MusicDataSource实现：{}", musicDataSource.getClass().getName());
        return musicDataSource;
    }

    @Bean
    public LyricDataSource lyricDataSource(NasProperties nasProperties) {
        LyricDataSource lyricDataSource;
        if (nasProperties.isEnableMixedDataSource()) {
            lyricDataSource = new MixedDataSource(nasProperties);
        } else if (nasProperties.isEnableTencentCosDataSource()) {
            lyricDataSource = new TencentCosDataSource(nasProperties);
        } else {
            lyricDataSource = new FileDataSource(nasProperties);
        }
        log.info("LyricDataSource实现：{}", lyricDataSource.getClass().getName());
        return lyricDataSource;
    }

    @Bean
    public CoverDataSource coverDataSource(NasProperties nasProperties) {
        CoverDataSource coverDataSource;
        if (nasProperties.isEnableMixedDataSource()) {
            coverDataSource = new MixedDataSource(nasProperties);
        } else if (nasProperties.isEnableTencentCosDataSource()) {
            coverDataSource = new TencentCosDataSource(nasProperties);
        } else {
            coverDataSource = new FileDataSource(nasProperties);
        }
        log.info("CoverDataSource实现：{}", coverDataSource.getClass().getName());
        return coverDataSource;
    }
}
