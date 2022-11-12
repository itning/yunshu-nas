package top.itning.yunshunas.music.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import top.itning.yunshunas.music.datasource.CoverDataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;
import top.itning.yunshunas.music.entity.Lyric;
import top.itning.yunshunas.music.entity.Music;
import top.itning.yunshunas.music.entity.SearchResult;
import top.itning.yunshunas.music.repository.LyricElasticsearchRepository;
import top.itning.yunshunas.music.repository.MusicRepository;
import top.itning.yunshunas.music.service.SearchService;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

    private final LyricElasticsearchRepository lyricElasticsearchRepository;
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;
    private final MusicRepository musicRepository;
    private final MusicDataSource musicDataSource;
    private final LyricDataSource lyricDataSource;
    private final CoverDataSource coverDataSource;

    @Autowired
    public SearchServiceImpl(LyricElasticsearchRepository lyricElasticsearchRepository, ElasticsearchRestTemplate elasticsearchRestTemplate, MusicRepository musicRepository, MusicDataSource musicDataSource, LyricDataSource lyricDataSource, CoverDataSource coverDataSource) {
        this.lyricElasticsearchRepository = lyricElasticsearchRepository;
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
        this.musicRepository = musicRepository;
        this.musicDataSource = musicDataSource;
        this.lyricDataSource = lyricDataSource;
        this.coverDataSource = coverDataSource;
    }

    @Override
    public void saveOrUpdateLyric(String musicId, String lyricId, String content) {
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

        lyricElasticsearchRepository.save(lyric);
    }

    @Override
    public void deleteLyric(String lyricId) {
        lyricElasticsearchRepository.deleteById(lyricId);
    }

    @Override
    public List<SearchResult> searchLyric(String keyword, Pageable pageable) {
        SearchHits<Lyric> search = elasticsearchRestTemplate.search(
                new NativeSearchQueryBuilder()
                        .withQuery(QueryBuilders.matchPhraseQuery(SEARCH_FILED_FOR_LYRIC, keyword))
                        .withHighlightBuilder(new HighlightBuilder().field(SEARCH_FILED_FOR_LYRIC, 50, 1))
                        .withPageable(pageable)
                        .build()
                , Lyric.class
        );
        if (!search.hasSearchHits()) {
            return Collections.emptyList();
        }

        return search.getSearchHits().stream()
                .map(item -> {
                    Lyric lyric = item.getContent();

                    Music music = musicRepository.findByMusicId(lyric.getMusicId()).orElseThrow(() -> new IllegalStateException("检索音乐信息失败，返回空"));

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
}
