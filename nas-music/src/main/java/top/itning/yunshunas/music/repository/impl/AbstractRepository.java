package top.itning.yunshunas.music.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import top.itning.yunshunas.common.db.DbSourceConfig;

/**
 * @author ning.wang
 * @since 2023/4/6 14:43
 */
public abstract class AbstractRepository {

    private final DbSourceConfig dbSourceConfig;

    protected AbstractRepository(DbSourceConfig dbSourceConfig) {
        this.dbSourceConfig = dbSourceConfig;
    }

    protected JdbcTemplate getJdbcTemplate() {
        return dbSourceConfig.getJdbcTemplate();
    }
}
