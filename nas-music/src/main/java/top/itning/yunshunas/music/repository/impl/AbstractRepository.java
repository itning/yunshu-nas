package top.itning.yunshunas.music.repository.impl;

import org.springframework.jdbc.core.JdbcTemplate;
import top.itning.yunshunas.common.db.ApplicationConfig;

/**
 * @author itning
 * @since 2023/4/6 14:43
 */
public abstract class AbstractRepository {

    private final ApplicationConfig applicationConfig;

    protected AbstractRepository(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    protected JdbcTemplate getJdbcTemplate() {
        return applicationConfig.getJdbcTemplate();
    }
}
