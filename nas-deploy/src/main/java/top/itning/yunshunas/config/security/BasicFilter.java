package top.itning.yunshunas.config.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.common.db.ApplicationConfig;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author itning
 * @since 2021/10/16 12:11
 */
@Slf4j
public class BasicFilter extends OncePerRequestFilter {

    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();
    private static final String HEALTH_CHECK_PATH = "/health";

    private final ApplicationConfig applicationConfig;

    public BasicFilter(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        NasProperties nasProperties = applicationConfig.getSetting(NasProperties.class);
        if (Objects.isNull(nasProperties)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!nasProperties.isEnableBasicAuth()) {
            filterChain.doFilter(request, response);
            return;
        }
        NasProperties.BasicAuthConfig basicAuthConfig = nasProperties.getBasicAuth();
        if (Objects.isNull(basicAuthConfig)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (!CollectionUtils.isEmpty(basicAuthConfig.getIgnorePath()) && basicAuthConfig.getIgnorePath().stream().anyMatch(path -> ANT_PATH_MATCHER.match(path, request.getRequestURI()))) {
            filterChain.doFilter(request, response);
            return;
        }
        if (HEALTH_CHECK_PATH.equals(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.isNotBlank(authorizationHeader) && authorizationHeader.startsWith("Basic ")) {
            try {
                String authString = authorizationHeader.substring(6);
                byte[] decodeBase64 = Base64.decodeBase64(authString);
                String[] usernameAndPassword = new String(decodeBase64, StandardCharsets.UTF_8).split(":");
                if (usernameAndPassword.length == 2) {
                    if (basicAuthConfig.getUsername().equals(usernameAndPassword[0]) && basicAuthConfig.getPassword().equals(usernameAndPassword[1])) {
                        filterChain.doFilter(request, response);
                        return;
                    } else {
                        log.warn("用户名/密码不正确 {} {}", usernameAndPassword[0], usernameAndPassword[1]);
                    }
                }
            } catch (Exception e) {
                log.error("认证出错", e);
            }
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.TEXT_HTML_VALUE);
        response.setCharacterEncoding("utf-8");
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"Access to the staging site\"");
        PrintWriter writer = response.getWriter();
        writer.write("<h1>401</h1>");
        writer.flush();
        writer.close();
    }
}
