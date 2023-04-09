package top.itning.yunshunas.common.db;

import lombok.Data;

import java.util.Objects;

/**
 * @author itning
 * @since 2023/4/9 10:48
 */
@Data
public class DbCheckConnectionResult {
    private boolean success;
    private String message;

    public static DbCheckConnectionResult success() {
        DbCheckConnectionResult dbCheckConnectionResult = new DbCheckConnectionResult();
        dbCheckConnectionResult.setSuccess(true);
        dbCheckConnectionResult.setMessage("success");
        return dbCheckConnectionResult;
    }

    public static DbCheckConnectionResult failed(String message) {
        DbCheckConnectionResult dbCheckConnectionResult = new DbCheckConnectionResult();
        dbCheckConnectionResult.setSuccess(false);
        dbCheckConnectionResult.setMessage(message);
        return dbCheckConnectionResult;
    }

    public static DbCheckConnectionResult failed(Exception e) {
        DbCheckConnectionResult dbCheckConnectionResult = new DbCheckConnectionResult();
        dbCheckConnectionResult.setSuccess(false);
        Throwable fistCause = getFistCause(e);
        dbCheckConnectionResult.setMessage(fistCause.getMessage());
        return dbCheckConnectionResult;
    }

    private static Throwable getFistCause(Throwable throwable) {
        Throwable cause = throwable.getCause();
        if (Objects.isNull(cause)) {
            return throwable;
        }
        return getFistCause(cause);
    }
}
