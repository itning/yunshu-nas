package top.itning.yunshunas.music.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import top.itning.yunshunas.music.converter.MusicConverter;
import top.itning.yunshunas.music.datasource.CoverDataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;
import top.itning.yunshunas.music.dto.MusicDTO;
import top.itning.yunshunas.music.repository.MusicRepository;
import top.itning.yunshunas.music.service.MusicService;

import javax.transaction.Transactional;

/**
 * @author itning
 * @date 2020/9/5 11:25
 */
@Transactional(rollbackOn = Exception.class)
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
    public Page<MusicDTO> findAll(Pageable pageable) {
        return musicRepository.findAll(pageable).map(item -> {
            MusicDTO musicDTO = MusicConverter.INSTANCE.entity2dto(item);
            musicDTO.setMusicUri(musicDataSource.getMusic(musicDTO.getMusicId()));
            musicDTO.setLyricUri(lyricDataSource.getLyric(musicDTO.getLyricId()));
            musicDTO.setCoverUri(coverDataSource.getCover(musicDTO.getMusicId()));
            return musicDTO;
        });
    }

    @Override
    public Page<MusicDTO> fuzzySearch(String keyword, Pageable pageable) {
        keyword = "%" + keyword + "%";
        return musicRepository.findAllByNameLikeOrSingerLike(keyword, keyword, pageable).map(MusicConverter.INSTANCE::entity2dto);
    }

    @Override
    public Page<MusicDTO> fuzzySearchName(String keyword, Pageable pageable) {
        keyword = "%" + keyword + "%";
        return musicRepository.findAllByNameLike(keyword, pageable).map(MusicConverter.INSTANCE::entity2dto);
    }

    @Override
    public Page<MusicDTO> fuzzySearchSinger(String keyword, Pageable pageable) {
        keyword = "%" + keyword + "%";
        return musicRepository.findAllBySingerLike(keyword, pageable).map(MusicConverter.INSTANCE::entity2dto);
    }
}
