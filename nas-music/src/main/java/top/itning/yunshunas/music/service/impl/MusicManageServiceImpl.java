package top.itning.yunshunas.music.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.itning.yunshunas.music.converter.MusicConverter;
import top.itning.yunshunas.music.datasource.CoverDataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;
import top.itning.yunshunas.music.dto.MusicChangeDTO;
import top.itning.yunshunas.music.dto.MusicDTO;
import top.itning.yunshunas.music.dto.MusicManageDTO;
import top.itning.yunshunas.music.entity.Music;
import top.itning.yunshunas.music.repository.MusicRepository;
import top.itning.yunshunas.music.service.MusicManageService;
import top.itning.yunshunas.music.service.SearchService;
import top.itning.yunshunas.music.service.UploadService;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

/**
 * 音乐管理服务实现
 *
 * @author itning
 * @since 2022/7/10 14:40
 */
@Slf4j
@Service
public class MusicManageServiceImpl implements MusicManageService {
    private final MusicRepository musicRepository;
    private final MusicDataSource musicDataSource;
    private final LyricDataSource lyricDataSource;
    private final CoverDataSource coverDataSource;
    private final UploadService uploadService;
    private final SearchService searchService;

    @Autowired
    public MusicManageServiceImpl(MusicRepository musicRepository, MusicDataSource musicDataSource, LyricDataSource lyricDataSource, CoverDataSource coverDataSource, UploadService uploadService, SearchService searchService) {
        this.musicRepository = musicRepository;
        this.musicDataSource = musicDataSource;
        this.lyricDataSource = lyricDataSource;
        this.coverDataSource = coverDataSource;
        this.uploadService = uploadService;
        this.searchService = searchService;
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

    @Override
    public MusicDTO editMusic(MusicChangeDTO changeDTO) throws Exception {
        if (Objects.isNull(changeDTO)) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (StringUtils.isBlank(changeDTO.getMusicId())) {
            throw new IllegalArgumentException("音乐ID不能为空");
        }

        MultipartFile musicFile = changeDTO.getMusicFile();
        MusicDTO musicDTO = null;
        if (null != musicFile) {
            log.debug("修改音乐文件 {}", musicFile.getOriginalFilename());
            musicDTO = uploadService.editMusic(changeDTO.getMusicId(), musicFile);
        }

        Music music = musicRepository.findByMusicId(changeDTO.getMusicId()).orElseThrow(() -> new IllegalArgumentException("音乐不存在"));
        MultipartFile lyricFile = changeDTO.getLyricFile();
        if (null != lyricFile) {
            log.debug("修改歌词文件 {}", lyricFile.getOriginalFilename());
            byte[] contentBytes = uploadService.editLyric(changeDTO.getMusicId(), lyricFile);
            searchService.saveOrUpdateLyric(changeDTO.getMusicId(), music.getLyricId(), new String(contentBytes, StandardCharsets.UTF_8));
        }

        if (StringUtils.isAllBlank(changeDTO.getName(), changeDTO.getSinger())) {
            if (null == musicDTO) {
                musicDTO = MusicConverter.INSTANCE.entity2dto(music);
                musicDTO.setMusicUri(musicDataSource.getMusic(musicDTO.getMusicId()));
                musicDTO.setLyricUri(lyricDataSource.getLyric(musicDTO.getLyricId()));
                musicDTO.setCoverUri(coverDataSource.getCover(musicDTO.getMusicId()));
            }
            log.info("未修改音乐信息");
            return musicDTO;
        }

        if (StringUtils.isNotBlank(changeDTO.getName())) {
            log.debug("修改音乐名称 {}", changeDTO.getName());
            music.setName(changeDTO.getName());
        }
        if (StringUtils.isNotBlank(changeDTO.getSinger())) {
            log.debug("修改音乐歌手 {}", changeDTO.getSinger());
            music.setSinger(changeDTO.getSinger());
        }
        music.setGmtModified(new Date());

        log.info("修改音乐 {}", music);
        Music savedMusic = musicRepository.saveAndFlush(music);
        musicDTO = MusicConverter.INSTANCE.entity2dto(savedMusic);
        musicDTO.setMusicUri(musicDataSource.getMusic(musicDTO.getMusicId()));
        musicDTO.setLyricUri(lyricDataSource.getLyric(musicDTO.getLyricId()));
        musicDTO.setCoverUri(coverDataSource.getCover(musicDTO.getMusicId()));
        return musicDTO;
    }

    @Override
    public MusicDTO addMusic(MusicChangeDTO music) throws Exception {
        if (Objects.isNull(music)) {
            throw new IllegalArgumentException("参数不能为空");
        }
        MultipartFile musicFile = music.getMusicFile();
        if (null == musicFile) {
            throw new IllegalArgumentException("音乐文件不能为空");
        }
        MusicDTO musicDTO = uploadService.uploadMusic(musicFile);
        MultipartFile lyricFile = music.getLyricFile();
        if (null != lyricFile) {
            byte[] contentBytes = uploadService.uploadLyric(musicDTO.getMusicId(), lyricFile);
            searchService.saveOrUpdateLyric(musicDTO.getMusicId(), musicDTO.getLyricId(), new String(contentBytes, StandardCharsets.UTF_8));
        }
        return musicDTO;
    }

    @Override
    public void deleteMusic(String musicId) {
        if (StringUtils.isBlank(musicId)) {
            throw new IllegalArgumentException("音乐ID不能为空");
        }
        Music music = musicRepository.findByMusicId(musicId).orElseThrow(() -> new IllegalArgumentException("音乐不存在"));
        musicDataSource.deleteMusic(musicId);
        lyricDataSource.deleteLyric(music.getLyricId());
        coverDataSource.deleteCover(music.getMusicId());
        musicRepository.delete(music);
        musicRepository.flush();
        searchService.deleteLyric(music.getLyricId());
    }
}
