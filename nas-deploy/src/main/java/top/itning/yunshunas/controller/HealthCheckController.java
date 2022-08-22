package top.itning.yunshunas.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author itning
 * @since 2022/8/22 20:09
 */
@RestController
public class HealthCheckController {

    @GetMapping("/health")
    public String health() {
        return "UP";
    }
}
