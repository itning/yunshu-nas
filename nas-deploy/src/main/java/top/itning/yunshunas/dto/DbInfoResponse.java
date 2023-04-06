package top.itning.yunshunas.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author itning
 * @since 2023/4/6 14:30
 */
@Data
public class DbInfoResponse implements Serializable {
    private String name;
    private String type;
    private String jdbcUrl;
    private String username;
}
