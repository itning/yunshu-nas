package top.itning.yunshunas.music.service.impl;

import co.elastic.clients.elasticsearch._types.query_dsl.MatchPhraseQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.IndexOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.HighlightQuery;
import org.springframework.data.elasticsearch.core.query.highlight.Highlight;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightField;
import org.springframework.data.elasticsearch.core.query.highlight.HighlightFieldParameters;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import top.itning.yunshunas.music.config.DataSourceConfig;
import top.itning.yunshunas.music.config.ElasticsearchConfig;
import top.itning.yunshunas.music.datasource.CoverDataSource;
import top.itning.yunshunas.music.datasource.DataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;
import top.itning.yunshunas.music.entity.Lyric;
import top.itning.yunshunas.music.entity.Music;
import top.itning.yunshunas.music.entity.SearchResult;
import top.itning.yunshunas.music.repository.MusicRepository;
import top.itning.yunshunas.music.service.SearchService;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 搜索服务实现
 *
 * @author itning
 * @since 2022/11/2 15:39
 */
@Slf4j
@Service
public class SearchServiceImpl implements SearchService {
    /**
     * 歌词搜索字段
     */
    private static final String SEARCH_FILED_FOR_LYRIC = "content";

    private final MusicRepository musicRepository;
    private final MusicDataSource musicDataSource;
    private final LyricDataSource lyricDataSource;
    private final CoverDataSource coverDataSource;
    private final ElasticsearchConfig elasticsearchConfig;
    private final RestTemplate restTemplate;
    private final Map<Class<? extends DataSource>, DataSourceConfig.DataSourceWrapper> readDataSourceMap;

    @Autowired
    public SearchServiceImpl(MusicRepository musicRepository,
                             MusicDataSource musicDataSource,
                             LyricDataSource lyricDataSource,
                             CoverDataSource coverDataSource,
                             ElasticsearchConfig elasticsearchConfig,
                             RestTemplate restTemplate,
                             Map<Class<? extends DataSource>, DataSourceConfig.DataSourceWrapper> readDataSourceMap) {
        this.musicRepository = musicRepository;
        this.musicDataSource = musicDataSource;
        this.lyricDataSource = lyricDataSource;
        this.coverDataSource = coverDataSource;
        this.elasticsearchConfig = elasticsearchConfig;
        this.restTemplate = restTemplate;
        this.readDataSourceMap = readDataSourceMap;
    }

    @Override
    public void saveOrUpdateLyric(String musicId, String lyricId, String content) {
        if (!elasticsearchConfig.enabled()) {
            return;
        }
        if (StringUtils.isAnyBlank(musicId, lyricId)) {
            throw new IllegalArgumentException("音乐ID或歌词ID为空！");
        }
        if (StringUtils.isBlank(content)) {
            return;
        }

        content = Arrays.stream(content.split("\n"))
                .map(itemLine -> {
                    if (itemLine.startsWith("[")) {
                        int lastIndex = itemLine.indexOf("]");
                        if (lastIndex == -1) {
                            return itemLine;
                        }
                        return itemLine.substring(lastIndex + 1);
                    }
                    return itemLine;
                })
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.joining("\n"));

        Lyric lyric = new Lyric();
        lyric.setMusicId(musicId);
        lyric.setLyricId(lyricId);
        lyric.setContent(content);

        elasticsearchConfig.getElasticsearchTemplate().save(lyric);
    }

    @Override
    public void deleteLyric(String lyricId) {
        if (!elasticsearchConfig.enabled()) {
            return;
        }
        elasticsearchConfig.getElasticsearchTemplate().delete(lyricId, Lyric.class);
    }

    @Override
    public List<SearchResult> searchLyric(String keyword) {
        if (!elasticsearchConfig.enabled()) {
            return Collections.emptyList();
        }
        SearchHits<Lyric> search = elasticsearchConfig.getElasticsearchTemplate().search(
                new NativeQueryBuilder()
                        .withQuery(new Query.Builder().matchPhrase(new MatchPhraseQuery.Builder().field(SEARCH_FILED_FOR_LYRIC).query(keyword).build()).build())
                        .withHighlightQuery(new HighlightQuery(new Highlight(Collections.singletonList(new HighlightField(SEARCH_FILED_FOR_LYRIC, HighlightFieldParameters.builder().withFragmentSize(50).withNumberOfFragments(1).build()))), null))
                        .build(),
                Lyric.class
        );

        if (!search.hasSearchHits()) {
            return Collections.emptyList();
        }

        return search.getSearchHits().stream()
                .map(item -> {
                    Lyric lyric = item.getContent();

                    Music music = musicRepository.findByMusicId(lyric.getMusicId()).orElseThrow(() -> {
                        log.error("检索音乐信息失败，返回空 音乐ID：{} 歌词ID：{}", lyric.getMusicId(), lyric.getLyricId());
                        return new IllegalStateException("检索音乐信息失败，返回空");
                    });

                    SearchResult searchResult = new SearchResult();
                    searchResult.setMusicId(music.getMusicId());
                    searchResult.setName(music.getName());
                    searchResult.setSinger(music.getSinger());
                    searchResult.setLyricId(music.getLyricId());
                    searchResult.setType(music.getType());
                    searchResult.setMusicUri(musicDataSource.getMusic(music.getMusicId()));
                    searchResult.setLyricUri(lyricDataSource.getLyric(music.getLyricId()));
                    searchResult.setCoverUri(coverDataSource.getCover(music.getMusicId()));
                    searchResult.setHighlightFields(item.getHighlightField(SEARCH_FILED_FOR_LYRIC));
                    return searchResult;
                })
                .collect(Collectors.toList());
    }

    @Override
    public void reInit() {
        if (!elasticsearchConfig.enabled()) {
            return;
        }

        IndexOperations lyricIndexOp = elasticsearchConfig.getElasticsearchTemplate().indexOps(Lyric.class);
        if (lyricIndexOp.exists()) {
            boolean success = lyricIndexOp.delete();
            if (!success) {
                log.error("delete index failed");
            }
        }
        boolean success = lyricIndexOp.create();
        if (!success) {
            log.error("create index failed");
        }

        DataSourceConfig.DataSourceWrapper wrapper = readDataSourceMap.get(LyricDataSource.class);
        LyricDataSource dataSource = (LyricDataSource) wrapper.dataSource();
        List<Music> musicList = musicRepository.findAll();
        for (Music music : musicList) {
            URI uri = dataSource.getLyric(music.getLyricId());
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(uri, String.class);
            if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
                log.warn("get music failed, status:{} uri:{}", responseEntity.getStatusCode(), uri);
                continue;
            }
            String body = responseEntity.getBody();
            if (StringUtils.isBlank(body)) {
                log.warn("get body failed, status:{} uri:{}", responseEntity.getStatusCode(), uri);
                continue;
            }
            this.saveOrUpdateLyric(music.getMusicId(), music.getLyricId(), body);
        }
    }
}
