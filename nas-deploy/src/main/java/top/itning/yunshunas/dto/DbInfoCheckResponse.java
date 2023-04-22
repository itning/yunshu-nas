package top.itning.yunshunas.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author itning
 * @since 2023/4/9 10:47
 */
@Data
public class DbInfoCheckResponse implements Serializable {
    private Boolean success;
    private String message;
}
