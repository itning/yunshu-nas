package top.itning.yunshunas.music.repository.impl;

import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.itning.yunshunas.common.db.DbSourceConfig;
import top.itning.yunshunas.music.entity.Music;
import top.itning.yunshunas.music.repository.MusicRepository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/**
 * @author ning.wang
 * @since 2023/4/6 14:42
 */
@Service
public class MusicRepositoryImpl extends AbstractRepository implements MusicRepository {

    public MusicRepositoryImpl(DbSourceConfig dbSourceConfig) {
        super(dbSourceConfig);
    }

    @Override
    public Music save(Music music) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        int updated = getJdbcTemplate().update(connection -> {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO music(music_id, lyric_id, name, singer, type) VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, music.getMusicId());
            ps.setString(2, music.getLyricId());
            ps.setString(3, music.getName());
            ps.setString(4, music.getSinger());
            ps.setInt(5, music.getType());
            return ps;
        }, keyHolder);
        if (updated != 1) {
            return null;
        }
        Optional<Long> key = Optional.ofNullable(keyHolder.getKey()).map(Number::longValue);
        return key.flatMap(this::findById).orElse(null);
    }

    @Override
    public boolean delete(Music music) {
        return false;
    }

    @Override
    public List<Music> findAll() {
        return getJdbcTemplate().query("SELECT * FROM music", new BeanPropertyRowMapper<>(Music.class));
    }

    @Override
    public List<Music> findAllByNameLikeOrSingerLike(String name, String singer) {
        return getJdbcTemplate().query("SELECT * FROM music WHERE name LIKE '%?%'OR singer LIKE '%?%'", ps -> {
            ps.setString(1, name);
            ps.setString(2, singer);
        }, new BeanPropertyRowMapper<>(Music.class));
    }

    @Override
    public List<Music> findAllByNameLike(String name) {
        return getJdbcTemplate().query("SELECT * FROM music WHERE name LIKE '%?%'",
                ps -> ps.setString(1, name),
                new BeanPropertyRowMapper<>(Music.class));
    }

    @Override
    public List<Music> findAllBySingerLike(String singer) {
        return getJdbcTemplate().query("SELECT * FROM music WHERE singer LIKE '%?%'",
                ps -> ps.setString(1, singer),
                new BeanPropertyRowMapper<>(Music.class));
    }

    @Override
    public Optional<Music> findByMusicId(String musicId) {
        List<Music> result = getJdbcTemplate().query("SELECT * FROM music WHERE music_id = ?", ps -> ps.setString(1, musicId), new BeanPropertyRowMapper<>(Music.class));
        if (CollectionUtils.isEmpty(result)) {
            return Optional.empty();
        }
        return Optional.ofNullable(result.get(0));
    }

    @Override
    public Optional<Music> findById(Long id) {
        if (null == id) {
            return Optional.empty();
        }
        List<Music> result = getJdbcTemplate().query("SELECT * FROM music WHERE id = ?", ps -> ps.setLong(1, id), new BeanPropertyRowMapper<>(Music.class));
        if (CollectionUtils.isEmpty(result)) {
            return Optional.empty();
        }
        return Optional.ofNullable(result.get(0));
    }
}
