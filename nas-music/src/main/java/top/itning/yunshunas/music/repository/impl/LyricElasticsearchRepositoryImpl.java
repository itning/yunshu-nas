package top.itning.yunshunas.music.repository.impl;

import org.springframework.stereotype.Service;
import top.itning.yunshunas.music.entity.Lyric;
import top.itning.yunshunas.music.repository.LyricElasticsearchRepository;

/**
 * @author itning
 * @since 2023/4/9 15:13
 */
@Service
public class LyricElasticsearchRepositoryImpl implements LyricElasticsearchRepository {
    @Override
    public void save(Lyric lyric) {

    }

    @Override
    public void deleteById(String lyricId) {

    }
}
