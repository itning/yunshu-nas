package top.itning.yunshunas.music.repository.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.itning.yunshunas.common.db.ApplicationConfig;
import top.itning.yunshunas.music.entity.Music;
import top.itning.yunshunas.music.repository.MusicRepository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @author itning
 * @since 2023/4/6 14:42
 */
@Service
public class MusicRepositoryImpl extends AbstractRepository implements MusicRepository {

    public MusicRepositoryImpl(ApplicationConfig applicationConfig) {
        super(applicationConfig);
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
    public boolean deleteById(Long id) {
        int updated = getJdbcTemplate().update("DELETE FROM music WHERE id=?", id);
        return updated != 0;
    }

    @Override
    public Music update(Music music) {
        String sql = "UPDATE music SET ";
        List<Object> params = new ArrayList<>();
        if (StringUtils.isNotBlank(music.getName())) {
            sql += "name=?, ";
            params.add(music.getName());
        }
        if (StringUtils.isNotBlank(music.getSinger())) {
            sql += "singer=?, ";
            params.add(music.getSinger());
        }
        if (StringUtils.isNotBlank(music.getMusicId())) {
            sql += "music_id=?, ";
            params.add(music.getMusicId());
        }
        if (StringUtils.isNotBlank(music.getLyricId())) {
            sql += "lyric_id=?, ";
            params.add(music.getLyricId());
        }
        if (Objects.nonNull(music.getType())) {
            sql += "type=?, ";
            params.add(music.getType());
        }
        sql += "gmt_modified=CURRENT_TIMESTAMP WHERE id=?";
        params.add(music.getId());
        int updated = getJdbcTemplate().update(sql, params.toArray());
        if (updated != 1) {
            return null;
        }
        return findById(music.getId()).orElse(null);
    }

    @Override
    public List<Music> findAll() {
        return getJdbcTemplate().query("SELECT * FROM music ORDER BY gmt_create DESC", new BeanPropertyRowMapper<>(Music.class));
    }

    @Override
    public List<Music> findAllByNameLikeOrSingerLike(String name, String singer) {
        return getJdbcTemplate().query("SELECT * FROM music WHERE name LIKE ? OR singer LIKE ? ORDER BY gmt_create DESC", ps -> {
            ps.setString(1, name);
            ps.setString(2, singer);
        }, new BeanPropertyRowMapper<>(Music.class));
    }

    @Override
    public List<Music> findAllByNameLike(String name) {
        return getJdbcTemplate().query("SELECT * FROM music WHERE name LIKE ? ORDER BY gmt_create DESC",
                ps -> ps.setString(1, name),
                new BeanPropertyRowMapper<>(Music.class));
    }

    @Override
    public List<Music> findAllBySingerLike(String singer) {
        return getJdbcTemplate().query("SELECT * FROM music WHERE singer LIKE ? ORDER BY gmt_create DESC",
                ps -> ps.setString(1, singer),
                new BeanPropertyRowMapper<>(Music.class));
    }

    @Override
    public Optional<Music> findByMusicId(String musicId) {
        List<Music> result = getJdbcTemplate().query("SELECT * FROM music WHERE music_id = ?", ps -> ps.setString(1, musicId), new BeanPropertyRowMapper<>(Music.class));
        if (CollectionUtils.isEmpty(result)) {
            return Optional.empty();
        }
        return Optional.ofNullable(result.getFirst());
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
        return Optional.ofNullable(result.getFirst());
    }

    @Override
    public Optional<Music> findByNameAndSingerAndType(String name, String singer, Integer type) {
        if (StringUtils.isAnyBlank(name, singer) || null == type) {
            return Optional.empty();
        }
        List<Music> result = getJdbcTemplate().query("SELECT * FROM music WHERE name = ? AND singer= ? AND type = ?", ps -> {
            ps.setString(1, name);
            ps.setString(2, singer);
            ps.setInt(3, type);
        }, new BeanPropertyRowMapper<>(Music.class));
        if (CollectionUtils.isEmpty(result)) {
            return Optional.empty();
        }
        return Optional.ofNullable(result.getFirst());
    }
}
