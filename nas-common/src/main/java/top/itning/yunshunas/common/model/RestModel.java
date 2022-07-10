package top.itning.yunshunas.common.model;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.Serializable;

/**
 * @author itning
 * @since 2020/9/5 12:39
 */
public class RestModel<T> implements Serializable {
    private int code;
    private String msg;
    private T data;

    public RestModel() {

    }

    private RestModel(HttpStatus status, String msg, T data) {
        this(status.value(), msg, data);
    }

    private RestModel(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public static <T> ResponseEntity<RestModel<T>> ok(T data) {
        return ResponseEntity.ok(new RestModel<>(HttpStatus.OK, "查询成功", data));
    }

    public static <T> ResponseEntity<RestModel<T>> created() {
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    public static <T> ResponseEntity<RestModel<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new RestModel<>(HttpStatus.CREATED, "创建成功", data));
    }

    public static ResponseEntity<Void> noContent() {
        return ResponseEntity.noContent().build();
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
