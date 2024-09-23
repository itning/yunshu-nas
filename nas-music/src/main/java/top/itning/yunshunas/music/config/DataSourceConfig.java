package top.itning.yunshunas.music.config;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.CollectionUtils;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.common.db.ApplicationConfig;
import top.itning.yunshunas.common.event.ConfigChangeEvent;
import top.itning.yunshunas.music.datasource.CoverDataSource;
import top.itning.yunshunas.music.datasource.DataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;

/**
 * 数据源配置
 *
 * @author itning
 * @since 2022/1/12 13:01
 */
@Slf4j
@Configuration
public class DataSourceConfig implements ApplicationListener<ConfigChangeEvent> {

    private final Map<String, DataSourceWrapper> musicDataSourceMap = new HashMap<>();
    private final Map<String, DataSourceWrapper> lyricDataSourceMap = new HashMap<>();
    private final Map<String, DataSourceWrapper> coverDataSourceMap = new HashMap<>();

    private final Map<Class<? extends DataSource>, DataSourceWrapper> readDataSourceMap = new HashMap<>();
    private final String port;
    private final ApplicationConfig applicationConfig;

    @Autowired
    public DataSourceConfig(@Value("${server.port}") String port,
                            ApplicationConfig applicationConfig) throws Exception {
        this.port = port;
        this.applicationConfig = applicationConfig;
        init();
    }

    public void init() throws Exception {
        musicDataSourceMap.clear();
        lyricDataSourceMap.clear();
        coverDataSourceMap.clear();
        readDataSourceMap.clear();

        NasProperties nasProperties = Optional.ofNullable(applicationConfig.getSetting(NasProperties.class)).orElse(new NasProperties());
        NasMusicProperties nasMusicProperties = applicationConfig.getSetting(NasMusicProperties.class);
        if (Objects.isNull(nasMusicProperties)) {
            return;
        }
        List<NasMusicProperties.MusicDataSourceConfig> dataSourceList = nasMusicProperties.getDataSource();
        if (CollectionUtils.isEmpty(dataSourceList)) {
            return;
        }
        DataSourceWrapper readMusicDataSource = null;
        DataSourceWrapper readLyricDataSource = null;
        DataSourceWrapper readCoverDataSource = null;
        for (NasMusicProperties.MusicDataSourceConfig dataSourceConfig : dataSourceList) {
            String name = dataSourceConfig.getName();
            DataSource dataSource = tryNewInstance(name, dataSourceConfig.getClassName(), dataSourceConfig, nasProperties);
            DataSourceWrapper dataSourceWrapper = new DataSourceWrapper(dataSource, dataSourceConfig);
            if (dataSource instanceof MusicDataSource) {
                musicDataSourceMap.put(name, dataSourceWrapper);
                log.info("add music data source name:{} datasource:{}", name, dataSource);
                if (Objects.isNull(readMusicDataSource) && dataSourceConfig.isCanRead()) {
                    readMusicDataSource = dataSourceWrapper;
                    log.info("add can read music data source name:{} datasource:{}", name, dataSource);
                }
            }
            if (dataSource instanceof LyricDataSource) {
                lyricDataSourceMap.put(name, dataSourceWrapper);
                log.info("add lyric data source name:{} datasource:{}", name, dataSource);
                if (Objects.isNull(readLyricDataSource) && dataSourceConfig.isCanRead()) {
                    readLyricDataSource = dataSourceWrapper;
                    log.info("add can read lyric data source name:{} datasource:{}", name, dataSource);
                }
            }
            if (dataSource instanceof CoverDataSource) {
                coverDataSourceMap.put(name, dataSourceWrapper);
                log.info("add cover data source name:{} datasource:{}", name, dataSource);
                if (Objects.isNull(readCoverDataSource) && dataSourceConfig.isCanRead()) {
                    readCoverDataSource = dataSourceWrapper;
                    log.info("add can read cover data source name:{} datasource:{}", name, dataSource);
                }
            }
        }

        if (Objects.isNull(readMusicDataSource)) {
            throw new IllegalArgumentException("At least one music data source set read is true");
        }
        if (Objects.isNull(readLyricDataSource)) {
            throw new IllegalArgumentException("At least one lyric data source set read is true");
        }
        if (Objects.isNull(readCoverDataSource)) {
            throw new IllegalArgumentException("At least one cover data source set read is true");
        }

        readDataSourceMap.put(MusicDataSource.class, readMusicDataSource);
        readDataSourceMap.put(LyricDataSource.class, readLyricDataSource);
        readDataSourceMap.put(CoverDataSource.class, readCoverDataSource);
    }

    @SneakyThrows
    @Override
    public void onApplicationEvent(ConfigChangeEvent event) {
        if (event.getSource() instanceof NasMusicProperties) {
            this.init();
        }
    }

    public record DataSourceWrapper(DataSource dataSource, NasMusicProperties.MusicDataSourceConfig config) {
    }

    @Bean
    public Map<Class<? extends DataSource>, DataSourceWrapper> readDataSourceMap() {
        return readDataSourceMap;
    }

    @Bean
    public Map<String, DataSourceWrapper> musicDataSourceMap() {
        return musicDataSourceMap;
    }

    @Bean
    public Map<String, DataSourceWrapper> lyricDataSourceMap() {
        return lyricDataSourceMap;
    }

    @Bean
    public Map<String, DataSourceWrapper> coverDataSourceMap() {
        return coverDataSourceMap;
    }

    private DataSource tryNewInstance(String name, Class<? extends DataSource> dataSourceClass, NasMusicProperties.MusicDataSourceConfig musicDataSourceConfig, NasProperties nasProperties) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        //TODO itning 重构 先查出所有的构造方法
        DataSource dataSource = null;
        try {
            Constructor<? extends DataSource> declaredConstructor = dataSourceClass.getDeclaredConstructor();
            dataSource = declaredConstructor.newInstance();
        } catch (NoSuchMethodException e) {
            log.debug("try new instance [{}] use [{}] constructor failed. for class name {}", name, e.getMessage(), dataSourceClass.getName());
        }
        if (Objects.isNull(dataSource)) {
            try {
                Constructor<? extends DataSource> declaredConstructor = dataSourceClass.getDeclaredConstructor(NasMusicProperties.MusicDataSourceConfig.class);
                dataSource = declaredConstructor.newInstance(musicDataSourceConfig);
            } catch (NoSuchMethodException e) {
                log.debug("try new instance [{}] use [{}] constructor failed. for class name {}", name, e.getMessage(), dataSourceClass.getName());
            }
        }
        if (Objects.isNull(dataSource)) {
            try {
                Constructor<? extends DataSource> declaredConstructor = dataSourceClass.getDeclaredConstructor(NasMusicProperties.MusicDataSourceConfig.class, NasProperties.class);
                dataSource = declaredConstructor.newInstance(musicDataSourceConfig, nasProperties);
            } catch (NoSuchMethodException e) {
                log.debug("try new instance [{}] use [{}] constructors failed. for class name {}", name, e.getMessage(), dataSourceClass.getName());
            }
        }
        if (Objects.isNull(dataSource)) {
            try {
                Constructor<? extends DataSource> declaredConstructor = dataSourceClass.getDeclaredConstructor(NasProperties.class, NasMusicProperties.MusicDataSourceConfig.class);
                dataSource = declaredConstructor.newInstance(nasProperties, musicDataSourceConfig);
            } catch (NoSuchMethodException e) {
                log.debug("try new instance [{}] use [{}] constructors failed. for class name {}", name, e.getMessage(), dataSourceClass.getName());
            }
        }
        if (Objects.isNull(dataSource)) {
            throw new IllegalArgumentException("can not find datasource constructor");
        }
        return dataSource;
    }

}
