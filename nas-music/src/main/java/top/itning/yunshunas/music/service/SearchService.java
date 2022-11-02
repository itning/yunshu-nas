package top.itning.yunshunas.music.service;

import org.springframework.data.domain.Page;
import top.itning.yunshunas.music.entity.Lyric;

/**
 * @author ning.wang
 * @since 2022/11/2 15:39
 */
public interface SearchService {
    void addLyric(String musicId, String lyricId, String content);
    Page<Lyric> searchLyric(String keyword);
}
