package top.itning.yunshunas.config.ftp;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.listener.ListenerFactory;
import org.apache.ftpserver.usermanager.UserFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import top.itning.yunshunas.common.config.NasFtpProperties;
import top.itning.yunshunas.common.db.ApplicationConfig;
import top.itning.yunshunas.common.event.ConfigChangeEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author itning
 * @since 2023/4/5 8:48
 */
@Slf4j
@Configuration
public class FtpConfig implements ApplicationListener<ConfigChangeEvent> {

    private final Map<String, FtpServer> instance = new HashMap<>();

    private final ApplicationConfig applicationConfig;

    @Autowired
    public FtpConfig(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @PostConstruct
    public void init() throws FtpException {
        NasFtpProperties nasFtpProperties = applicationConfig.getSetting(NasFtpProperties.class);
        if (Objects.isNull(nasFtpProperties)) {
            return;
        }
        List<NasFtpProperties.FtpConfig> ftpConfigList = nasFtpProperties.getConfig();
        if (Objects.isNull(ftpConfigList)) {
            return;
        }

        this.destroy();
        for (NasFtpProperties.FtpConfig config : ftpConfigList) {
            String name = config.getName();

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
            try {
                instance.put(name, server);
                server.start();
                log.info("start ftp {} for port {}", name, config.getPort());
            } catch (Exception e) {
                server.stop();
                instance.remove(name);
                throw e;
            }
        }
    }

    @PreDestroy
    public void destroy() {
        for (Map.Entry<String, FtpServer> item : instance.entrySet()) {
            String name = item.getKey();
            FtpServer server = item.getValue();

            if (!server.isStopped()) {
                server.stop();
                log.info("stop ftp {}", name);
            }
        }
    }

    @SneakyThrows
    @Override
    public void onApplicationEvent(ConfigChangeEvent event) {
        if (event.getSource() instanceof NasFtpProperties) {
            this.destroy();
            this.init();
        }
    }
}
