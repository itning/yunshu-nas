package top.itning.yunshu.yunshunas.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import top.itning.yunshu.yunshunas.entity.Music;

import java.util.Optional;

/**
 * @author itning
 * @date 2020/9/5 11:15
 */
public interface MusicRepository extends JpaRepository<Music, Long> {
    Page<Music> findAllByNameLikeOrSingerLike(String name, String singer, Pageable pageable);

    Page<Music> findAllByNameLike(String name, Pageable pageable);

    Page<Music> findAllBySingerLike(String singer, Pageable pageable);

    Optional<Music> findByMusicId(String musicId);

    boolean existsByName(String name);

    void deleteByMusicId(String musicId);
}
