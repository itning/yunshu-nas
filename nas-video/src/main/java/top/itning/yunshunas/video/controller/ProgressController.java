package top.itning.yunshunas.video.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author itning
 * @date 2019/7/16 17:55
 */
@Controller
public class ProgressController {
    @GetMapping("/progress")
    public String progress() {
        return "progress";
    }
}
