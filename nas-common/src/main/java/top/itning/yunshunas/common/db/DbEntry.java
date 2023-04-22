package top.itning.yunshunas.common.db;

import lombok.Data;
import lombok.Getter;

/**
 * @author itning
 * @since 2023/4/6 12:16
 */
@Data
public class DbEntry {

    private int id;
    private String name;
    private Type type;
    private String jdbcUrl;
    private String username;
    private String password;

    public enum Type {
        MYSQL("""
                CREATE TABLE IF NOT EXISTS `music` (
                  `id` bigint NOT NULL AUTO_INCREMENT,
                  `music_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                  `lyric_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci DEFAULT NULL,
                  `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                  `singer` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL,
                  `type` int NOT NULL,
                  `gmt_create` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  `gmt_modified` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                  PRIMARY KEY (`id`) USING BTREE,
                  UNIQUE KEY `UK_music_id` (`music_id`) USING BTREE,
                  KEY `index_music_id` (`music_id`) USING BTREE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
                """),
        SQLITE("""
                CREATE TABLE IF NOT EXISTS `music` (
                  `id` INTEGER PRIMARY KEY autoincrement,
                  `music_id` TEXT NOT NULL unique,
                  `lyric_id` TEXT DEFAULT NULL,
                  `name` TEXT NOT NULL,
                  `singer` TEXT NOT NULL,
                  `type` INTEGER NOT NULL,
                  `gmt_create` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  `gmt_modified` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                );
                CREATE INDEX IF NOT EXISTS index_music_id ON music (music_id);
                """),
        ;
        @Getter
        private final String ddlSql;

        Type(String ddlSql) {
            this.ddlSql = ddlSql;
        }
    }
}
