package top.itning.yunshunas.music.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import top.itning.yunshunas.music.dto.RestModel;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;

/**
 * @author itning
 * @date 2020/9/5 12:41
 */
@ControllerAdvice
@Slf4j
public class ExceptionResolver {
    /**
     * json 格式错误消息
     *
     * @param response HttpServletResponse
     * @param e        Exception
     * @return 异常消息
     */
    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public RestModel<?> jsonErrorHandler(HttpServletResponse response, Exception e) {
        log.error("jsonErrorHandler->{}:{}", e.getClass().getSimpleName(), e.getMessage(), e);
        RestModel<?> restModel = new RestModel<>();
        restModel.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        restModel.setMsg(e.getMessage());
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        return restModel;
    }

    @ExceptionHandler(value = ConstraintViolationException.class)
    @ResponseBody
    public RestModel<?> clientAbortException(HttpServletResponse response, ConstraintViolationException e) {
        RestModel<?> restModel = new RestModel<>();
        restModel.setCode(HttpStatus.BAD_REQUEST.value());
        restModel.setMsg(e.getMessage());
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return restModel;
    }
}
