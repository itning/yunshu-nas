package top.itning.yunshunas.music.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import top.itning.yunshunas.common.model.RestModel;

import java.io.IOException;

/**
 * @author itning
 * @since 2020/9/5 12:41
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

    @ExceptionHandler(value = AsyncRequestNotUsableException.class)
    public void asyncRequestNotUsableException(AsyncRequestNotUsableException e) throws IOException {
        log.warn("asyncRequestNotUsableException->{}", e.getMessage());
    }

    @ExceptionHandler(value = HttpRequestMethodNotSupportedException.class)
    @ResponseBody
    public RestModel<?> httpRequestMethodNotSupportedException(HttpServletResponse response, HttpRequestMethodNotSupportedException e) throws JsonProcessingException {
        log.warn("httpRequestMethodNotSupportedException-> {}", e.getBody());
        RestModel<?> restModel = new RestModel<>();
        restModel.setCode(HttpStatus.BAD_REQUEST.value());
        restModel.setMsg(e.getMessage());
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return restModel;
    }

    @ExceptionHandler(value = NoResourceFoundException.class)
    @ResponseBody
    public RestModel<?> noResourceFoundException(HttpServletResponse response, NoResourceFoundException e) throws JsonProcessingException {
        log.warn("noResourceFoundException-> {}", e.getBody());
        RestModel<?> restModel = new RestModel<>();
        restModel.setCode(HttpStatus.NOT_FOUND.value());
        restModel.setMsg(e.getMessage());
        response.setStatus(HttpStatus.NOT_FOUND.value());
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

    @ExceptionHandler(value = IllegalArgumentException.class)
    @ResponseBody
    public RestModel<?> illegalArgumentException(HttpServletResponse response, IllegalArgumentException e) {
        RestModel<?> restModel = new RestModel<>();
        restModel.setCode(HttpStatus.BAD_REQUEST.value());
        restModel.setMsg(e.getMessage());
        response.setStatus(HttpStatus.BAD_REQUEST.value());
        return restModel;
    }
}
