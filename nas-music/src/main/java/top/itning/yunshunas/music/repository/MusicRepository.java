package top.itning.yunshunas.music.repository;

import top.itning.yunshunas.music.entity.Music;

import java.util.List;
import java.util.Optional;

/**
 * @author itning
 * @since 2020/9/5 11:15
 */
public interface MusicRepository {
    Music save(Music music);

    boolean deleteById(Long id);

    Music update(Music music);

    List<Music> findAll();

    List<Music> findAllByNameLikeOrSingerLike(String name, String singer);

    List<Music> findAllByNameLike(String name);

    List<Music> findAllBySingerLike(String singer);

    Optional<Music> findByMusicId(String musicId);

    Optional<Music> findById(Long id);

    Optional<Music> findByNameAndSingerAndType(String name, String singer, Integer type);
}
