package top.itning.yunshunas.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author itning
 * @since 2022/8/5 21:01
 */
@Slf4j
@RestController
public class LogController {

    private final SseEmitter sseEmitter;

    public LogController() {
        sseEmitter = new SseEmitter(0L);
        ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            try {
                sseEmitter.send(SseEmitter.event().id(UUID.randomUUID().toString()).data(LocalDateTime.now().toString()).build());
            } catch (IOException e) {
                log.error("send error", e);
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    @GetMapping("/log")
    public SseEmitter getLog() {
        return sseEmitter;
    }
}
