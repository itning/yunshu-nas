package top.itning.yunshunas.music.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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


    Page<Lyric> searchLyric(String keyword, Pageable pageable);
}
