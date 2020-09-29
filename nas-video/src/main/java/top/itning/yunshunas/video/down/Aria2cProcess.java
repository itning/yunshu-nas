package top.itning.yunshunas.video.down;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.itning.yunshunas.common.config.NasProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static top.itning.yunshunas.common.util.CommandUtils.process;

/**
 * @author itning
 * @date 2019/7/17 20:52
 */
@Component
public class Aria2cProcess {
    private static final Logger logger = LoggerFactory.getLogger(Aria2cProcess.class);

    public Aria2cProcess(NasProperties nasProperties) {
        ThreadPoolExecutor synchronousBlockingSingleService = new ThreadPoolExecutor(1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("aria2c-pool-%d").build());
        synchronousBlockingSingleService.submit(() -> {
            List<String> command = new ArrayList<>();
            command.add(nasProperties.getAria2cFile());
            command.add("--rpc-listen-port");
            command.add("6800");
            command.add("--enable-rpc");
            command.add("--rpc-listen-all");
            try {
                process(command, line -> {
                    if (logger.isDebugEnabled() && StringUtils.isNotBlank(line)) {
                        logger.debug(line);
                    }
                });
            } catch (Exception e) {
                logger.error("start aria2c process error", e);
            }
        });
    }
}
