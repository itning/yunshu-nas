package top.itning.yunshunas.music.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import top.itning.yunshunas.music.entity.Lyric;
import top.itning.yunshunas.music.repository.LyricElasticsearchRepository;
import top.itning.yunshunas.music.service.SearchService;

/**
 * @author ning.wang
 * @since 2022/11/2 15:39
 */
@Service
public class SearchServiceImpl implements SearchService {
    private final LyricElasticsearchRepository lyricElasticsearchRepository;

    @Autowired
    public SearchServiceImpl(LyricElasticsearchRepository lyricElasticsearchRepository) {
        this.lyricElasticsearchRepository = lyricElasticsearchRepository;
    }

    @Override
    public Page<Lyric> searchLyric(String keyword) {
        Page<Lyric> search = lyricElasticsearchRepository.searchByContent(keyword, Pageable.ofSize(10));
        return search;
    }
}
