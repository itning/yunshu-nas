package top.itning.yunshunas.config;

import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import jakarta.servlet.MultipartConfigElement;

/**
 * @author itning
 * @since 2022/8/5 20:53
 */
@Configuration
public class BeanConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setLocation(System.getProperty("java.io.tmpdir"));
        return factory.createMultipartConfig();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
