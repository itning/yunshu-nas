package top.itning.yunshunas.music.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.itning.yunshunas.music.converter.MusicConverter;
import top.itning.yunshunas.music.datasource.CoverDataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;
import top.itning.yunshunas.music.dto.MusicDTO;
import top.itning.yunshunas.music.repository.MusicRepository;
import top.itning.yunshunas.music.service.MusicService;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author itning
 * @since 2020/9/5 11:25
 */
@Service
public class MusicServiceImpl implements MusicService {
    private final MusicRepository musicRepository;
    private final MusicDataSource musicDataSource;
    private final LyricDataSource lyricDataSource;
    private final CoverDataSource coverDataSource;

    @Autowired
    public MusicServiceImpl(MusicRepository musicRepository, MusicDataSource musicDataSource, LyricDataSource lyricDataSource, CoverDataSource coverDataSource) {
        this.musicRepository = musicRepository;
        this.musicDataSource = musicDataSource;
        this.lyricDataSource = lyricDataSource;
        this.coverDataSource = coverDataSource;
    }

    @Override
    public List<MusicDTO> findAll() {
        return musicRepository.findAll().stream().map(item -> {
            MusicDTO musicDTO = MusicConverter.INSTANCE.entity2dto(item);
            musicDTO.setMusicUri(musicDataSource.getMusic(musicDTO.getMusicId()));
            musicDTO.setLyricUri(lyricDataSource.getLyric(musicDTO.getLyricId()));
            musicDTO.setCoverUri(coverDataSource.getCover(musicDTO.getMusicId()));
            return musicDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<MusicDTO> fuzzySearch(String keyword) {
        keyword = "%" + keyword + "%";
        return musicRepository.findAllByNameLikeOrSingerLike(keyword, keyword).stream().map(item -> {
            MusicDTO musicDTO = MusicConverter.INSTANCE.entity2dto(item);
            musicDTO.setMusicUri(musicDataSource.getMusic(musicDTO.getMusicId()));
            musicDTO.setLyricUri(lyricDataSource.getLyric(musicDTO.getLyricId()));
            musicDTO.setCoverUri(coverDataSource.getCover(musicDTO.getMusicId()));
            return musicDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<MusicDTO> fuzzySearchName(String keyword) {
        keyword = "%" + keyword + "%";
        return musicRepository.findAllByNameLike(keyword).stream().map(item -> {
            MusicDTO musicDTO = MusicConverter.INSTANCE.entity2dto(item);
            musicDTO.setMusicUri(musicDataSource.getMusic(musicDTO.getMusicId()));
            musicDTO.setLyricUri(lyricDataSource.getLyric(musicDTO.getLyricId()));
            musicDTO.setCoverUri(coverDataSource.getCover(musicDTO.getMusicId()));
            return musicDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<MusicDTO> fuzzySearchSinger(String keyword) {
        keyword = "%" + keyword + "%";
        return musicRepository.findAllBySingerLike(keyword).stream().map(item -> {
            MusicDTO musicDTO = MusicConverter.INSTANCE.entity2dto(item);
            musicDTO.setMusicUri(musicDataSource.getMusic(musicDTO.getMusicId()));
            musicDTO.setLyricUri(lyricDataSource.getLyric(musicDTO.getLyricId()));
            musicDTO.setCoverUri(coverDataSource.getCover(musicDTO.getMusicId()));
            return musicDTO;
        }).collect(Collectors.toList());
    }
}
