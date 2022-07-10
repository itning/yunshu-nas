package top.itning.yunshunas.music.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import top.itning.yunshunas.music.converter.MusicConverter;
import top.itning.yunshunas.music.datasource.CoverDataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;
import top.itning.yunshunas.music.dto.MusicManageDTO;
import top.itning.yunshunas.music.repository.MusicRepository;
import top.itning.yunshunas.music.service.MusicManageService;

/**
 * 音乐管理服务实现
 *
 * @author itning
 * @since 2022/7/10 14:40
 */
@Service
public class MusicManageServiceImpl implements MusicManageService {
    private final MusicRepository musicRepository;
    private final MusicDataSource musicDataSource;
    private final LyricDataSource lyricDataSource;
    private final CoverDataSource coverDataSource;

    @Autowired
    public MusicManageServiceImpl(MusicRepository musicRepository, MusicDataSource musicDataSource, LyricDataSource lyricDataSource, CoverDataSource coverDataSource) {
        this.musicRepository = musicRepository;
        this.musicDataSource = musicDataSource;
        this.lyricDataSource = lyricDataSource;
        this.coverDataSource = coverDataSource;
    }

    @Override
    public Page<MusicManageDTO> getMusicList(Pageable pageable) {
        return musicRepository.findAll(pageable).map(item -> {
            MusicManageDTO musicManageDTO = MusicConverter.INSTANCE.music2ManageDto(item);
            musicManageDTO.setMusicUri(musicDataSource.getMusic(musicManageDTO.getMusicId()));
            musicManageDTO.setLyricUri(lyricDataSource.getLyric(musicManageDTO.getLyricId()));
            musicManageDTO.setCoverUri(coverDataSource.getCover(musicManageDTO.getMusicId()));
            return musicManageDTO;
        });
    }

    @Override
    public Page<MusicManageDTO> fuzzySearch(String keyword, Pageable pageable) {
        keyword = "%" + keyword + "%";
        return musicRepository.findAllByNameLikeOrSingerLike(keyword, keyword, pageable).map(item -> {
            MusicManageDTO musicDTO = MusicConverter.INSTANCE.music2ManageDto(item);
            musicDTO.setMusicUri(musicDataSource.getMusic(musicDTO.getMusicId()));
            musicDTO.setLyricUri(lyricDataSource.getLyric(musicDTO.getLyricId()));
            musicDTO.setCoverUri(coverDataSource.getCover(musicDTO.getMusicId()));
            return musicDTO;
        });
    }

    @Override
    public MusicManageDTO getOneMusic(String musicId) {
        return musicRepository.findByMusicId(musicId).map(item -> {
            MusicManageDTO musicDTO = MusicConverter.INSTANCE.music2ManageDto(item);
            musicDTO.setMusicUri(musicDataSource.getMusic(musicDTO.getMusicId()));
            musicDTO.setLyricUri(lyricDataSource.getLyric(musicDTO.getLyricId()));
            musicDTO.setCoverUri(coverDataSource.getCover(musicDTO.getMusicId()));
            return musicDTO;
        }).orElse(null);
    }
}
