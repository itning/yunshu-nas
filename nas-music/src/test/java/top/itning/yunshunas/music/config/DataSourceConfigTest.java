package top.itning.yunshunas.music.config;

import org.junit.jupiter.api.Test;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.common.db.ApplicationConfig;
import top.itning.yunshunas.common.event.ConfigChangeEvent;
import top.itning.yunshunas.music.datasource.MusicDataSource;
import top.itning.yunshunas.music.datasource.impl.FileDataSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataSourceConfigTest {

    @Test
    void initializesDataSourcesWhenConfigurationIsAddedAfterStartup() throws Exception {
        ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
        when(applicationConfig.getSetting(NasProperties.class)).thenReturn(new NasProperties());
        when(applicationConfig.getSetting(NasMusicProperties.class)).thenReturn(null);

        DataSourceConfig dataSourceConfig = new DataSourceConfig("8888", applicationConfig);
        NasMusicProperties properties = new NasMusicProperties();
        NasMusicProperties.MusicDataSourceConfig musicDataSourceConfig = new NasMusicProperties.MusicDataSourceConfig();
        musicDataSourceConfig.setName("local");
        musicDataSourceConfig.setClassName(FileDataSource.class);
        properties.setDataSource(List.of(musicDataSourceConfig));
        when(applicationConfig.getSetting(NasMusicProperties.class)).thenReturn(properties);

        dataSourceConfig.onApplicationEvent(new ConfigChangeEvent(properties));

        assertSame(dataSourceConfig.musicDataSourceMap().get("local"),
                dataSourceConfig.readDataSourceMap().get(MusicDataSource.class));
    }

    @Test
    void keepsLastKnownGoodDataSourcesWhenReloadHasNoMusicConfiguration() throws Exception {
        ApplicationConfig applicationConfig = mock(ApplicationConfig.class);
        NasMusicProperties properties = new NasMusicProperties();
        NasMusicProperties.MusicDataSourceConfig musicDataSourceConfig = new NasMusicProperties.MusicDataSourceConfig();
        musicDataSourceConfig.setName("local");
        musicDataSourceConfig.setClassName(FileDataSource.class);
        properties.setDataSource(List.of(musicDataSourceConfig));

        when(applicationConfig.getSetting(NasProperties.class)).thenReturn(new NasProperties());
        when(applicationConfig.getSetting(NasMusicProperties.class)).thenReturn(properties);

        DataSourceConfig dataSourceConfig = new DataSourceConfig("8888", applicationConfig);
        DataSourceConfig.DataSourceWrapper expected = dataSourceConfig.musicDataSourceMap().get("local");

        when(applicationConfig.getSetting(NasMusicProperties.class)).thenReturn(null);
        dataSourceConfig.init();

        assertSame(expected, dataSourceConfig.musicDataSourceMap().get("local"));
        assertSame(expected, dataSourceConfig.readDataSourceMap().get(MusicDataSource.class));
    }
}
