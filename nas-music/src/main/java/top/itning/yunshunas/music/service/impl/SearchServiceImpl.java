package top.itning.yunshunas.music.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
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
    public void addLyric(String musicId, String lyricId, String content) {
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
    public Page<Lyric> searchLyric(String keyword) {
        SearchHits<Lyric> search1 = elasticsearchRestTemplate.search(StringQuery.builder(keyword).withFields("content").withPageable(Pageable.ofSize(20)).build(), Lyric.class);

        Page<Lyric> search = lyricElasticsearchRepository.searchByContent(keyword, Pageable.ofSize(10));
        return search;
    }
}
