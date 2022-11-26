package top.itning.yunshunas.config.deploy;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;

/**
 * @author itning
 * @since 2020/9/5 20:00
 */
@Configuration
public class CustomWebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] allMethod = Arrays.stream(HttpMethod.values()).map(HttpMethod::name).toArray(String[]::new);
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowCredentials(true)
                .allowedMethods(allMethod)
                .maxAge(86400);
    }
}