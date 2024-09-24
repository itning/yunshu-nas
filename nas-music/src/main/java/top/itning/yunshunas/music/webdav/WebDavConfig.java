package top.itning.yunshunas.music.webdav;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.util.pattern.PathPatternParser;
import top.itning.yunshunas.music.repository.MusicRepository;

/**
 * @author itning
 * @since 2024/9/24 17:46
 */
@Configuration
public class WebDavConfig {
    @Bean
    public FilterRegistrationBean<WebDavFilter> webDavFilterFilterRegistrationBean(MusicRepository musicRepository,
                                                                                   PathPatternParser mvcPatternParser) {
        FilterRegistrationBean<WebDavFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new WebDavFilter(musicRepository, mvcPatternParser));
        registration.addUrlPatterns("/*");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return registration;
    }
}
