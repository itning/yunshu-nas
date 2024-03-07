package top.itning.yunshunas.common.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import top.itning.yunshunas.common.event.ConfigChangeEvent;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static top.itning.yunshunas.common.util.JsonUtils.OBJECT_MAPPER;

/**
 * @author itning
 * @since 2023/4/6 10:44
 */
@Slf4j
@Component
public class ApplicationConfig {

    private HikariDataSource applicationDataSource;
    private HikariDataSource userDataSource;
    private JdbcTemplate applicationJdbcTemplate;
    private JdbcTemplate jdbcTemplate;
    private DbEntry dbEntry;

    @Value("${nas.defaultDbPath:yunshu-nas.db}")
    private String defaultDbPath;
    private LoadingCache<Class<?>, Object> settingCache;

    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public ApplicationConfig(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @PostConstruct
    public void init() {
        log.info("use default db path:{}", defaultDbPath);
        String jdbcUrl = "jdbc:sqlite:" + defaultDbPath;
        HikariConfig config = new HikariConfig();
        config.setPoolName("SettingDataSourcePool-" + defaultDbPath);
        config.setJdbcUrl(jdbcUrl);

        String createTableQuery = """
                CREATE TABLE IF NOT EXISTS db (
                id INTEGER PRIMARY KEY autoincrement,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                jdbcUrl TEXT NOT NULL,
                username TEXT DEFAULT NULL,
                password TEXT DEFAULT NULL
                );
                """;

        config.setConnectionInitSql(createTableQuery);
        applicationDataSource = new HikariDataSource(config);
        this.applicationJdbcTemplate = new JdbcTemplate(applicationDataSource, false);
        this.applicationJdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS setting (
                id INTEGER PRIMARY KEY autoincrement,
                key TEXT NOT NULL unique,
                value TEXT NOT NULL
                );
                """
        );
        settingCache = Caffeine.newBuilder()
                .maximumSize(1024L)
                .expireAfterWrite(1, TimeUnit.HOURS)
                .build(new CacheLoader<>() {
                    @Override
                    public @Nullable Object load(Class<?> tClass) throws Exception {
                        List<String> results = applicationJdbcTemplate.query("SELECT value FROM setting WHERE key = ?", new SingleColumnRowMapper<>(String.class), tClass.getName());
                        if (CollectionUtils.isEmpty(results)) {
                            return null;
                        }
                        String value = results.getFirst();
                        return OBJECT_MAPPER.readValue(value, tClass);
                    }
                });

        List<DbEntry> results = applicationJdbcTemplate.query("SELECT * FROM db ORDER BY id DESC LIMIT 1", new BeanPropertyRowMapper<>(DbEntry.class));
        if (CollectionUtils.isEmpty(results)) {
            return;
        }
        dbEntry = results.getFirst();
        log.info("Get DB Info: {}", dbEntry.getName());
        if (Objects.isNull(dbEntry)) {
            return;
        }
        this.jdbcTemplate = getJdbcTemplate(dbEntry);
    }

    @PreDestroy
    public void destroy() {
        if (Objects.nonNull(userDataSource)) {
            userDataSource.close();
        }
        if (Objects.nonNull(applicationDataSource)) {
            applicationDataSource.close();
        }
    }

    private JdbcTemplate getJdbcTemplate(DbEntry dbEntry) {
        HikariDataSource dataSource = getDataSource(dbEntry);
        if (Objects.nonNull(userDataSource)) {
            userDataSource.close();
        }
        userDataSource = dataSource;
        return new JdbcTemplate(userDataSource, true);
    }

    private HikariDataSource getDataSource(DbEntry dbEntry) {
        return this.getDataSource(dbEntry, null);
    }

    private HikariDataSource getDataSource(DbEntry dbEntry, Long connectionTimeoutMs) {
        HikariConfig config = new HikariConfig();
        config.setPoolName("UserDataSourcePool-" + dbEntry.getId() + "-" + dbEntry.getName());
        config.setJdbcUrl(dbEntry.getJdbcUrl());
        config.setUsername(dbEntry.getUsername());
        config.setPassword(dbEntry.getPassword());
        config.setConnectionInitSql(dbEntry.getType().getDdlSql());
        if (Objects.nonNull(connectionTimeoutMs)) {
            config.setConnectionTimeout(connectionTimeoutMs);
        } else {
            // 从连接池获取连接时最大等待时间, 单位毫秒, 默认值 30秒, 至少 250ms
            config.setConnectionTimeout(5000L);
        }
        // 检测连接是否有效的超时时间
        config.setValidationTimeout(3000L);
        // 连接可以在池中的最大闲置时间
        config.setIdleTimeout(5 * 60 * 1000L);
        // 连接最大存活时间
        config.setMaxLifetime(10 * 60 * 1000L);
        config.setMinimumIdle(5);
        config.setMaximumPoolSize(10);
        return new HikariDataSource(config);
    }


    public DbCheckConnectionResult checkConnection(DbEntry dbEntry) {
        try (HikariDataSource dataSource = getDataSource(dbEntry, 3000L)) {
            return dataSource.isRunning() ? DbCheckConnectionResult.success() : DbCheckConnectionResult.failed("running status is false");
        } catch (Exception e) {
            log.warn("check connection failed {}", dbEntry, e);
            return DbCheckConnectionResult.failed(e);
        } finally {
            if (dbEntry.getType() == DbEntry.Type.SQLITE && Objects.nonNull(dbEntry.getJdbcUrl()) && dbEntry.getJdbcUrl().toLowerCase().startsWith("jdbc:sqlite:")) {
                String path = dbEntry.getJdbcUrl().substring(12);
                File file = new File(path);
                if (file.exists() && file.canRead()) {
                    log.debug("删除测试后的文件：{} {}", file, file.delete());
                }
            }
        }
    }

    public void setDataSource(DbEntry dbEntry) {

        String insertSql = "INSERT INTO db(name, type,jdbcUrl,username,password) VALUES (?, ?, ?, ?, ?)";
        int updated = applicationJdbcTemplate.update(insertSql, dbEntry.getName(), dbEntry.getType().name(), dbEntry.getJdbcUrl(), dbEntry.getUsername(), dbEntry.getPassword());
        if (updated != 1) {
            throw new RuntimeException("插入数据库失败 " + updated);
        }
        this.jdbcTemplate = getJdbcTemplate(dbEntry);
        this.dbEntry = dbEntry;
    }

    public JdbcTemplate getJdbcTemplate() {
        if (Objects.isNull(jdbcTemplate)) {
            throw new RuntimeException("数据库未配置，请先配置数据库！");
        }
        return jdbcTemplate;
    }

    public DbEntry getDbEntry() {
        return this.dbEntry;
    }

    @SuppressWarnings("all")
    public <T> T getSetting(Class<T> tClass) {
        return (T) settingCache.get(tClass);
    }

    public <T> T setSetting(T obj) {
        if (Objects.isNull(getSetting(obj.getClass()))) {
            return createSetting(obj);
        } else {
            return updateSetting(obj);
        }
    }

    public <T> T updateSetting(T obj) {
        try {
            int updated = applicationJdbcTemplate.update("UPDATE setting SET value = ? WHERE key = ?", OBJECT_MAPPER.writeValueAsString(obj), obj.getClass().getName());
            if (updated != 1) {
                return null;
            }
            settingCache.invalidate(obj.getClass());
            publishConfigChangeEvent(obj);
            return obj;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T createSetting(T obj) {
        try {
            int updated = applicationJdbcTemplate.update("INSERT INTO setting(key, value) VALUES (?, ?)", obj.getClass().getName(), OBJECT_MAPPER.writeValueAsString(obj));
            if (updated != 1) {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        settingCache.invalidate(obj.getClass());
        publishConfigChangeEvent(obj);
        return obj;
    }

    private void publishConfigChangeEvent(Object object) {
        ConfigChangeEvent customSpringEvent = new ConfigChangeEvent(object);
        applicationEventPublisher.publishEvent(customSpringEvent);
    }
}
