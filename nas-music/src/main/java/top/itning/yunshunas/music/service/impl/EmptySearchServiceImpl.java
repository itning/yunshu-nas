package top.itning.yunshunas.music.service.impl;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import top.itning.yunshunas.music.entity.SearchResult;
import top.itning.yunshunas.music.service.SearchService;

import java.util.List;

/**
 * @author itning
 * @since 2022/11/13 11:34
 */
@Service
@ConditionalOnProperty(value = "spring.data.elasticsearch.repositories.enabled", havingValue = "false")
public class EmptySearchServiceImpl implements SearchService {
    @Override
    public void saveOrUpdateLyric(String musicId, String lyricId, String content) {

    }

    @Override
    public void deleteLyric(String lyricId) {

    }

    @Override
    public List<SearchResult> searchLyric(String keyword, Pageable pageable) {
        return null;
    }
}
