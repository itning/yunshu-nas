package top.itning.yunshunas.music.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Configuration properties for Elasticsearch.
 *
 * @author itning
 * @see org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchProperties
 * @since 2023/4/9 14:53
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ElasticsearchProperties {

    private boolean enabled;

    private List<String> uris = new ArrayList<>(Collections.singletonList("http://localhost:9200"));

    /**
     * Username for authentication with Elasticsearch.
     */
    private String username;

    /**
     * Password for authentication with Elasticsearch.
     */
    private String password;

    /**
     * Prefix added to the path of every request sent to Elasticsearch.
     */
    private String pathPrefix;
}
