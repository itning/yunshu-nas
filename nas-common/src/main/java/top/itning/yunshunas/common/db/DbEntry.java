package top.itning.yunshunas.common.db;

import lombok.Data;

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
        MYSQL,
        SQLITE,
        ;
    }
}
