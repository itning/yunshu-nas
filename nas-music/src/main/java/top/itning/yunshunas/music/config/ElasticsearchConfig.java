package top.itning.yunshunas.music.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.util.StringUtils;
import top.itning.yunshunas.common.db.ApplicationConfig;
import top.itning.yunshunas.common.event.ConfigChangeEvent;
import top.itning.yunshunas.music.entity.Lyric;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * @author itning
 * @since 2023/4/9 14:56
 */
@Slf4j
@Configuration
public class ElasticsearchConfig implements ApplicationListener<ConfigChangeEvent> {

    private final ApplicationConfig applicationConfig;

    private ElasticsearchTemplate elasticsearchTemplate;
    private RestClient restClient;
    private RestClientTransport restClientTransport;

    @Autowired
    public ElasticsearchConfig(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
    }

    @PostConstruct
    public void init() {
        ElasticsearchProperties properties = applicationConfig.getSetting(ElasticsearchProperties.class);
        if (Objects.isNull(properties) || !properties.isEnabled()) {
            return;
        }
        HttpHost[] hosts = properties.getUris().stream().map(this::createHttpHost).toArray(HttpHost[]::new);
        RestClientBuilder builder = RestClient.builder(hosts);
        if (properties.getPathPrefix() != null) {
            builder.setPathPrefix(properties.getPathPrefix());
        }
        restClient = builder.build();
        restClientTransport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        ElasticsearchClient elasticsearchClient = new ElasticsearchClient(restClientTransport);
        elasticsearchTemplate = new ElasticsearchTemplate(elasticsearchClient);
        IndexOperations indexOperations = elasticsearchTemplate.indexOps(Lyric.class);
        if (!indexOperations.exists()) {
            indexOperations.create();
        }
    }

    @PreDestroy
    public void destroy() {
        if (Objects.nonNull(restClientTransport)) {
            try {
                restClientTransport.close();
            } catch (IOException ignore) {
            }
        }
        if (Objects.nonNull(restClient)) {
            try {
                restClient.close();
            } catch (IOException ignore) {
            }
        }
        elasticsearchTemplate = null;
    }

    @Override
    public void onApplicationEvent(ConfigChangeEvent event) {
        if (event.getSource() instanceof ElasticsearchProperties) {
            this.destroy();
            this.init();
        }
    }

    public ElasticsearchTemplate getElasticsearchTemplate() {
        if (Objects.isNull(elasticsearchTemplate)) {
            throw new RuntimeException("Elasticsearch未配置，请先配置！");
        }
        return elasticsearchTemplate;
    }

    public boolean enabled() {
        return Objects.nonNull(elasticsearchTemplate);
    }

    private HttpHost createHttpHost(String uri) {
        try {
            return createHttpHost(URI.create(uri));
        } catch (IllegalArgumentException ex) {
            return HttpHost.create(uri);
        }
    }

    private HttpHost createHttpHost(URI uri) {
        if (!StringUtils.hasLength(uri.getUserInfo())) {
            return HttpHost.create(uri.toString());
        }
        try {
            return HttpHost.create(new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), uri.getPath(),
                    uri.getQuery(), uri.getFragment())
                    .toString());
        } catch (URISyntaxException ex) {
            throw new IllegalStateException(ex);
        }
    }

}
