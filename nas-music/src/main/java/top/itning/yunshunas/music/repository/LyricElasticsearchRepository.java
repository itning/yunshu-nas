package top.itning.yunshunas.music.repository;

import top.itning.yunshunas.music.entity.Lyric;

/**
 * @author itning
 * @since 2022/11/2 15:31
 */
public interface LyricElasticsearchRepository {
    void save(Lyric lyric);

    void deleteById(String lyricId);
}
