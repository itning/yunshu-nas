package top.itning.yunshunas.config.ftp;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.UserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import top.itning.yunshunas.common.config.NasFtpProperties;
import top.itning.yunshunas.common.db.DbSourceConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author itning
 * @since 2023/4/5 8:48
 */
@Slf4j
@Configuration
public class FtpConfig {

    private Map<String, FtpServer> instance;

    private final DbSourceConfig dbSourceConfig;

    @Autowired
    public FtpConfig(DbSourceConfig dbSourceConfig) {
        this.dbSourceConfig = dbSourceConfig;
    }

    @PostConstruct
    public void init() throws FtpException {
        NasFtpProperties nasFtpProperties = dbSourceConfig.getSetting(NasFtpProperties.class);
        if (Objects.isNull(nasFtpProperties)) {
            return;
        }
        Map<String, NasFtpProperties.FtpConfig> configMap = nasFtpProperties.getConfig();
        if (Objects.isNull(configMap)) {
            return;
        }

        instance = new HashMap<>();
        for (Map.Entry<String, NasFtpProperties.FtpConfig> item : configMap.entrySet()) {
            String name = item.getKey();
            NasFtpProperties.FtpConfig config = item.getValue();

            FtpServerFactory serverFactory = new FtpServerFactory();
            ListenerFactory listenerFactory = new ListenerFactory();
            listenerFactory.setPort(config.getPort());
            listenerFactory.setServerAddress(config.getServerAddress());
            serverFactory.addListener(name, listenerFactory.createListener());

            for (NasFtpProperties.User user : config.getUsers()) {
                UserFactory userFactory;
                if (user.isEnableAnonymousAccess()) {
                    userFactory = new UserFactory();
                    userFactory.setName("anonymous");
                    userFactory.setPassword("anonymous");
                } else {
                    userFactory = new UserFactory();
                    userFactory.setName(user.getUsername());
                    userFactory.setPassword(user.getPassword());
                }
                userFactory.setHomeDirectory(user.getHomeDir().getPath());
                serverFactory.getUserManager().save(userFactory.createUser());
            }
            FtpServer server = serverFactory.createServer();
            server.start();
            log.info("start ftp {} for port {}", name, config.getPort());

            instance.put(name, server);
        }
    }

    @PreDestroy
    public void destroy() {
        if (Objects.isNull(instance)) {
            return;
        }
        for (Map.Entry<String, FtpServer> item : instance.entrySet()) {
            String name = item.getKey();
            FtpServer server = item.getValue();

            if (!server.isStopped()) {
                server.stop();
                log.info("stop ftp {}", name);
            }
        }
    }
}
