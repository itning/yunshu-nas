package top.itning.yunshunas.common.db;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import top.itning.yunshunas.common.config.NasProperties;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author itning
 * @since 2023/4/6 10:44
 */
@Slf4j
@Component
public class DbSourceConfig {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final NasProperties nasProperties;
    private HikariDataSource dbInfoDataSource;
    private HikariDataSource userDataSource;
    private JdbcTemplate dbInfoJdbcTemplate;
    private JdbcTemplate jdbcTemplate;
    private DbEntry dbEntry;

    public DbSourceConfig(NasProperties nasProperties) {
        this.nasProperties = nasProperties;
    }

    @PostConstruct
    public void init() {
        String jdbcUrl = Optional.ofNullable(nasProperties.getDefaultDbPath()).map(it -> "jdbc:sqlite:" + it).orElse("jdbc:sqlite:yunshu-nas.db");
        HikariConfig config = new HikariConfig();
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
        dbInfoDataSource = new HikariDataSource(config);
        this.dbInfoJdbcTemplate = new JdbcTemplate(dbInfoDataSource, false);
        this.dbInfoJdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS setting (
                id INTEGER PRIMARY KEY autoincrement,
                key TEXT NOT NULL unique,
                value TEXT NOT NULL
                );
                """
        );
        List<DbEntry> results = dbInfoJdbcTemplate.query("SELECT * FROM db ORDER BY id DESC LIMIT 1", new BeanPropertyRowMapper<>(DbEntry.class));
        if (CollectionUtils.isEmpty(results)) {
            return;
        }
        dbEntry = results.get(0);
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
        if (Objects.nonNull(dbInfoDataSource)) {
            dbInfoDataSource.close();
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
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbEntry.getJdbcUrl());
        config.setUsername(dbEntry.getUsername());
        config.setPassword(dbEntry.getPassword());
        config.setConnectionInitSql(dbEntry.getType().getDdlSql());
        return new HikariDataSource(config);
    }


    public boolean checkConnection(DbEntry dbEntry) {
        try (HikariDataSource dataSource = getDataSource(dbEntry)) {
            return dataSource.isRunning();
        } catch (Exception e) {
            log.warn("check connection failed {}", dbEntry, e);
            return false;
        }
    }

    public void setDataSource(DbEntry dbEntry) {

        String insertSql = "INSERT INTO db(name, type,jdbcUrl,username,password) VALUES (?, ?, ?, ?, ?)";
        int updated = dbInfoJdbcTemplate.update(insertSql, dbEntry.getName(), dbEntry.getType().name(), dbEntry.getJdbcUrl(), dbEntry.getUsername(), dbEntry.getPassword());
        if (updated != 1) {
            throw new RuntimeException("插入数据库失败 " + updated);
        }
        this.jdbcTemplate = getJdbcTemplate(dbEntry);
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

    public <T> T getSetting(String key, Class<T> tClass) {
        List<T> results = dbInfoJdbcTemplate.query("SELECT * FROM setting WHERE key = ?", new BeanPropertyRowMapper<>(tClass), key);
        if (CollectionUtils.isEmpty(results)) {
            return null;
        }
        return results.get(0);
    }

    public <T> T updateSetting(String key, T obj) {
        try {
            int updated = dbInfoJdbcTemplate.update("UPDATE setting SET value = ? WHERE key = ?", OBJECT_MAPPER.writeValueAsString(obj), key);
            if (updated != 1) {
                return null;
            }
            return obj;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public <T> T createSetting(String key, T obj) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int updated = getJdbcTemplate().update(connection -> {
            try {
                PreparedStatement ps = connection.prepareStatement("INSERT INTO setting(key, value) VALUES (?, ?)", Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, key);
                ps.setString(2, OBJECT_MAPPER.writeValueAsString(obj));
                return ps;
            } catch (Exception e) {
                throw new SQLException(e);
            }
        }, keyHolder);
        if (updated != 1 && keyHolder.getKey() != null) {
            return null;
        }
        return obj;
    }
}
