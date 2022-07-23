package top.itning.yunshunas.music.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.music.datasource.CoverDataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;
import top.itning.yunshunas.music.datasource.impl.BackupFileDataSource;
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

    private final MusicDataSource musicDataSource;
    private final LyricDataSource lyricDataSource;
    private final CoverDataSource coverDataSource;

    public DataSourceConfig(NasProperties nasProperties, @Value("${server.port}") String port) {
        if (nasProperties.isEnableMixedDataSource()) {
            MixedDataSource mixedDataSource = new MixedDataSource(nasProperties, port);
            musicDataSource = mixedDataSource;
            lyricDataSource = mixedDataSource;
            coverDataSource = mixedDataSource;
        } else if (nasProperties.isEnableTencentCosDataSource()) {
            TencentCosDataSource tencentCosDataSource = new TencentCosDataSource(nasProperties);
            musicDataSource = tencentCosDataSource;
            lyricDataSource = tencentCosDataSource;
            coverDataSource = tencentCosDataSource;
        } else if (nasProperties.isEnableBackupFileDataSource()) {
            BackupFileDataSource backupFileDataSource = new BackupFileDataSource(nasProperties, port);
            musicDataSource = backupFileDataSource;
            lyricDataSource = backupFileDataSource;
            coverDataSource = backupFileDataSource;
        } else {
            FileDataSource fileDataSource = new FileDataSource(nasProperties, port);
            musicDataSource = fileDataSource;
            lyricDataSource = fileDataSource;
            coverDataSource = fileDataSource;
        }
    }

    @Bean
    public MusicDataSource musicDataSource() {
        log.info("MusicDataSource实现：{}", musicDataSource.getClass().getName());
        return musicDataSource;
    }

    @Bean
    public LyricDataSource lyricDataSource() {
        log.info("LyricDataSource实现：{}", lyricDataSource.getClass().getName());
        return lyricDataSource;
    }

    @Bean
    public CoverDataSource coverDataSource() {
        log.info("CoverDataSource实现：{}", coverDataSource.getClass().getName());
        return coverDataSource;
    }
}
