package top.itning.yunshunas.video.down;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.common.db.ApplicationConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static top.itning.yunshunas.common.util.CommandUtils.process;

/**
 * @author itning
 * @since 2019/7/17 20:52
 */
@Component
public class Aria2cProcess {
    private static final Logger logger = LoggerFactory.getLogger(Aria2cProcess.class);

    public Aria2cProcess(ApplicationConfig applicationConfig) {
        NasProperties nasProperties = applicationConfig.getSetting(NasProperties.class);
        if (Objects.isNull(nasProperties)) {
            return;
        }
        if (StringUtils.isBlank(nasProperties.getAria2cFile())) {
            return;
        }
        Thread.Builder.OfVirtual virtual = Thread.ofVirtual().name("aria2c-pool-", 0);
        virtual.start(() -> {
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
