package top.itning.yunshunas.controller;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.common.db.ApplicationConfig;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.Optional;
import java.util.Properties;

@Slf4j
@Controller
public class FrontPageController {

    @Value("${server.port}")
    private String port;

    private final ApplicationConfig applicationConfig;

    @Autowired(required = false)
    private BuildProperties buildProperties;

    @Autowired
    public FrontPageController(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @PostConstruct
    public void postConstruct() throws IOException {
        if (null == buildProperties) {
            Properties properties = new Properties();
            properties.load(getClass().getClassLoader().getResourceAsStream("build-info-dev.properties"));
            buildProperties = new BuildProperties(properties);
            log.warn("填充BuildProperties对象，仅用于开发环境");
        }
    }

    @GetMapping("/")
    public String index(Model model) {
        URL url = Optional.ofNullable(applicationConfig.getSetting(NasProperties.class))
                .map(NasProperties::getServerUrl)
                .orElseGet(() -> {
                    try {
                        final String serverUrlStr = System.getenv("SERVER_URL");
                        if (StringUtils.isNotBlank(serverUrlStr)) {
                            log.info("use env server url: {}", serverUrlStr);
                            return new URI(serverUrlStr).toURL();
                        } else {
                            return null;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        if (null != url) {
            model.addAttribute("nasUrl", url);
        }
        model.addAttribute("buildProperties", buildProperties);
        return "index";
    }
}
