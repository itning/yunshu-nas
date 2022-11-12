package top.itning.yunshunas.music.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;
import top.itning.yunshunas.music.entity.Lyric;
import top.itning.yunshunas.music.repository.LyricElasticsearchRepository;
import top.itning.yunshunas.music.service.SearchService;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @author ning.wang
 * @since 2022/11/2 15:39
 */
@Service
public class SearchServiceImpl implements SearchService {
    private final LyricElasticsearchRepository lyricElasticsearchRepository;
    private final ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    public SearchServiceImpl(LyricElasticsearchRepository lyricElasticsearchRepository, ElasticsearchRestTemplate elasticsearchRestTemplate) {
        this.lyricElasticsearchRepository = lyricElasticsearchRepository;
        this.elasticsearchRestTemplate = elasticsearchRestTemplate;
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
    public SearchHits<Lyric> searchLyric(String keyword, Pageable pageable) {
        return elasticsearchRestTemplate.search(
                new NativeSearchQueryBuilder()
                        .withQuery(QueryBuilders.matchQuery("content", keyword).analyzer("ik_smart").minimumShouldMatch("100%"))
                        .withHighlightBuilder(new HighlightBuilder().field("content"))
                        .withPageable(pageable)
                        .build()
                , Lyric.class
        );
    }
}
