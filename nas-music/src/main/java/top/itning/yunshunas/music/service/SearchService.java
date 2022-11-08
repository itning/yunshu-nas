package top.itning.yunshunas.music.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.SearchHits;
import top.itning.yunshunas.music.entity.Lyric;

/**
 * @author ning.wang
 * @since 2022/11/2 15:39
 */
public interface SearchService {
    /**
     *
     * @param musicId
     * @param lyricId
     * @param content
     */
    void addLyric(String musicId, String lyricId, String content);


    SearchHits<Lyric> searchLyric(String keyword, Pageable pageable);
}
