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
import java.util.*;
import java.util.stream.Collectors;

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
        NasProperties nasProperties = Optional.ofNullable(applicationConfig.getSetting(NasProperties.class)).orElse(new NasProperties());
        NasMusicProperties nasMusicProperties = applicationConfig.getSetting(NasMusicProperties.class);
        init(nasProperties, nasMusicProperties);
    }

    private void init(NasProperties nasProperties, NasMusicProperties nasMusicProperties) throws Exception {
        if (Objects.isNull(nasMusicProperties)) {
            log.warn("music data source configuration is not available; keep the current data sources");
            return;
        }
        List<NasMusicProperties.MusicDataSourceConfig> dataSourceList = nasMusicProperties.getDataSource();
        if (CollectionUtils.isEmpty(dataSourceList)) {
            log.warn("music data source configuration is empty; keep the current data sources");
            return;
        }
        Map<String, DataSourceWrapper> newMusicDataSourceMap = new HashMap<>();
        Map<String, DataSourceWrapper> newLyricDataSourceMap = new HashMap<>();
        Map<String, DataSourceWrapper> newCoverDataSourceMap = new HashMap<>();

        DataSourceWrapper readMusicDataSource = null;
        DataSourceWrapper readLyricDataSource = null;
        DataSourceWrapper readCoverDataSource = null;
        for (NasMusicProperties.MusicDataSourceConfig dataSourceConfig : dataSourceList) {
            String name = dataSourceConfig.getName();
            DataSource dataSource = tryNewInstance(name, dataSourceConfig.getClassName(), dataSourceConfig, nasProperties);
            DataSourceWrapper dataSourceWrapper = new DataSourceWrapper(dataSource, dataSourceConfig);
            if (dataSource instanceof MusicDataSource) {
                newMusicDataSourceMap.put(name, dataSourceWrapper);
                log.info("add music data source name:{} datasource:{}", name, dataSource);
                if (Objects.isNull(readMusicDataSource) && dataSourceConfig.isCanRead()) {
                    readMusicDataSource = dataSourceWrapper;
                    log.info("add can read music data source name:{} datasource:{}", name, dataSource);
                }
            }
            if (dataSource instanceof LyricDataSource) {
                newLyricDataSourceMap.put(name, dataSourceWrapper);
                log.info("add lyric data source name:{} datasource:{}", name, dataSource);
                if (Objects.isNull(readLyricDataSource) && dataSourceConfig.isCanRead()) {
                    readLyricDataSource = dataSourceWrapper;
                    log.info("add can read lyric data source name:{} datasource:{}", name, dataSource);
                }
            }
            if (dataSource instanceof CoverDataSource) {
                newCoverDataSourceMap.put(name, dataSourceWrapper);
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

        synchronized (readDataSourceMap) {
            musicDataSourceMap.clear();
            musicDataSourceMap.putAll(newMusicDataSourceMap);
            lyricDataSourceMap.clear();
            lyricDataSourceMap.putAll(newLyricDataSourceMap);
            coverDataSourceMap.clear();
            coverDataSourceMap.putAll(newCoverDataSourceMap);
            readDataSourceMap.clear();
            readDataSourceMap.put(MusicDataSource.class, readMusicDataSource);
            readDataSourceMap.put(LyricDataSource.class, readLyricDataSource);
            readDataSourceMap.put(CoverDataSource.class, readCoverDataSource);
        }
    }

    @SneakyThrows
    @Override
    public void onApplicationEvent(ConfigChangeEvent event) {
        if (event.getSource() instanceof NasMusicProperties) {
            try {
                NasProperties nasProperties = Optional.ofNullable(applicationConfig.getSetting(NasProperties.class)).orElse(new NasProperties());
                init(nasProperties, (NasMusicProperties) event.getSource());
            } catch (Exception e) {
                log.error("failed to reload music data source configuration", e);
            }
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
        Constructor<?>[] declaredConstructors = dataSourceClass.getDeclaredConstructors();

        for (Constructor<?> constructor : declaredConstructors) {
            try {
                Class<?>[] parameterTypes = constructor.getParameterTypes();
                Object[] args = prepareArgs(parameterTypes, musicDataSourceConfig, nasProperties);
                if (args != null) {
                    constructor.setAccessible(true);
                    DataSource dataSource = (DataSource) constructor.newInstance(args);
                    log.debug("success new instance [{}] use constructor with parameter types: {}", name,
                            Arrays.stream(parameterTypes).map(Class::getName).collect(Collectors.joining(", ")));
                    return dataSource;
                }
            } catch (Exception e) {
                log.debug("try new instance [{}] use constructor failed. for class name {}, error: {}", name, dataSourceClass.getName(), e.getMessage());
            }
        }

        throw new IllegalArgumentException("can not find datasource constructor");
    }

    private Object[] prepareArgs(Class<?>[] parameterTypes, NasMusicProperties.MusicDataSourceConfig musicDataSourceConfig, NasProperties nasProperties) {
        Object[] args = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramType = parameterTypes[i];
            if (paramType.equals(NasMusicProperties.MusicDataSourceConfig.class)) {
                args[i] = musicDataSourceConfig;
            } else if (paramType.equals(NasProperties.class)) {
                args[i] = nasProperties;
            } else {
                return null;
            }
        }

        return args;
    }

}
