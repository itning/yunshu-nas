package top.itning.yunshunas.config.security;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import top.itning.yunshunas.common.db.DbSourceConfig;

/**
 * @author itning
 * @since 2021/10/16 12:36
 */
@Configuration
public class FilterRegistration {
    @Bean
    public FilterRegistrationBean<BasicFilter> basicFilterFilterRegistrationBean(DbSourceConfig dbSourceConfig) {
        FilterRegistrationBean<BasicFilter> basicFilterFilterRegistrationBean = new FilterRegistrationBean<>(new BasicFilter(dbSourceConfig));
        basicFilterFilterRegistrationBean.addUrlPatterns("/*");
        basicFilterFilterRegistrationBean.setName("basicFilter");
        return basicFilterFilterRegistrationBean;
    }
}
