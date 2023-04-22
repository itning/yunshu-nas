package top.itning.yunshunas.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.itning.yunshunas.common.config.NasFtpProperties;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.common.db.ApplicationConfig;
import top.itning.yunshunas.common.model.RestModel;
import top.itning.yunshunas.common.util.JsonUtils;
import top.itning.yunshunas.music.config.ElasticsearchProperties;
import top.itning.yunshunas.music.config.NasMusicProperties;

/**
 * @author itning
 * @since 2023/4/7 13:41
 */
@Validated
@RequestMapping("/api/setting")
@RestController
public class SettingController {
    private final ApplicationConfig applicationConfig;

    @Autowired
    public SettingController(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @GetMapping("/{type}")
    public ResponseEntity<RestModel<Object>> getSetting(@PathVariable String type) {
        switch (type) {
            case "nas" -> {
                return RestModel.ok(applicationConfig.getSetting(NasProperties.class));
            }
            case "datasource" -> {
                return RestModel.ok(applicationConfig.getSetting(NasMusicProperties.class));
            }
            case "ftp" -> {
                return RestModel.ok(applicationConfig.getSetting(NasFtpProperties.class));
            }
            case "es" -> {
                return RestModel.ok(applicationConfig.getSetting(ElasticsearchProperties.class));
            }
            default -> throw new IllegalArgumentException("未知类型");
        }
    }

    @PostMapping("/{type}")
    public ResponseEntity<RestModel<Object>> setSetting(@PathVariable String type, @RequestBody String value) throws Exception {
        switch (type) {
            case "nas" -> {
                NasProperties nasProperties = JsonUtils.OBJECT_MAPPER.readValue(value, NasProperties.class);
                return RestModel.ok(applicationConfig.setSetting(nasProperties));
            }
            case "datasource" -> {
                NasMusicProperties nasMusicProperties = JsonUtils.OBJECT_MAPPER.readValue(value, NasMusicProperties.class);
                return RestModel.ok(applicationConfig.setSetting(nasMusicProperties));
            }
            case "ftp" -> {
                NasFtpProperties nasFtpProperties = JsonUtils.OBJECT_MAPPER.readValue(value, NasFtpProperties.class);
                return RestModel.ok(applicationConfig.setSetting(nasFtpProperties));
            }
            case "es" -> {
                ElasticsearchProperties elasticsearchProperties = JsonUtils.OBJECT_MAPPER.readValue(value, ElasticsearchProperties.class);
                return RestModel.ok(applicationConfig.setSetting(elasticsearchProperties));
            }
            default -> throw new IllegalArgumentException("未知类型");
        }
    }
}
