package top.itning.yunshunas.config.log;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * @author itning
 * @since 2023/5/13 14:57
 */
@Slf4j
public class TraceInterceptor implements HandlerInterceptor {
    private static final String TRACE_ID_LOG = "traceId";
    private static final String TRACE_ID_HEADER = "X-TraceId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null) {
            traceId = UUID.randomUUID().toString().replace("-", "");
        }

        MDC.put(TRACE_ID_LOG, traceId);
        log.debug("[{}] [{}{}]", request.getMethod(), request.getRequestURI(), request.getQueryString() == null ? "" : "?" + request.getQueryString());
        response.setHeader(TRACE_ID_HEADER, traceId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        MDC.remove(TRACE_ID_LOG);
    }
}
