package top.itning.yunshunas.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.io.Serializable;

/**
 * @author itning
 * @since 2023/4/6 14:30
 */
@Data
public class DbInfoRequest implements Serializable {
    @NotEmpty(message = "数据库名称不能为空")
    private String name;
    @NotEmpty(message = "数据库类型不能为空")
    private String type;
    @NotEmpty(message = "数据库连接地址不能为空")
    private String jdbcUrl;
    private String username;
    private String password;
}
