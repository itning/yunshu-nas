package top.itning.yunshunas.music.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import top.itning.yunshunas.music.converter.MusicConverter;
import top.itning.yunshunas.music.datasource.CoverDataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;
import top.itning.yunshunas.music.dto.MusicChangeDTO;
import top.itning.yunshunas.music.dto.MusicDTO;
import top.itning.yunshunas.music.dto.MusicManageDTO;
import top.itning.yunshunas.music.dto.MusicMetaInfo;
import top.itning.yunshunas.music.entity.Music;
import top.itning.yunshunas.music.repository.MusicRepository;
import top.itning.yunshunas.music.service.MusicManageService;
import top.itning.yunshunas.music.service.SearchService;
import top.itning.yunshunas.music.service.UploadService;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
    public List<MusicManageDTO> getMusicList() {
        return musicRepository.findAll().stream().map(item -> {
            MusicManageDTO musicManageDTO = MusicConverter.INSTANCE.music2ManageDto(item);
            musicManageDTO.setMusicUri(musicDataSource.getMusic(musicManageDTO.getMusicId()));
            musicManageDTO.setLyricUri(lyricDataSource.getLyric(musicManageDTO.getLyricId()));
            musicManageDTO.setCoverUri(coverDataSource.getCover(musicManageDTO.getMusicId()));
            return musicManageDTO;
        }).collect(Collectors.toList());
    }

    @Override
    public List<MusicManageDTO> fuzzySearch(String keyword) {
        keyword = "%" + keyword + "%";
        return musicRepository.findAllByNameLikeOrSingerLike(keyword, keyword)
                .stream()
                .map(item -> {
                    MusicManageDTO musicDTO = MusicConverter.INSTANCE.music2ManageDto(item);
                    musicDTO.setMusicUri(musicDataSource.getMusic(musicDTO.getMusicId()));
                    musicDTO.setLyricUri(lyricDataSource.getLyric(musicDTO.getLyricId()));
                    musicDTO.setCoverUri(coverDataSource.getCover(musicDTO.getMusicId()));
                    return musicDTO;
                })
                .collect(Collectors.toList());
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

        MusicMetaInfo musicMetaInfo = new MusicMetaInfo();
        musicMetaInfo.setTitle(changeDTO.getName());
        musicMetaInfo.setArtists(Collections.singletonList(changeDTO.getSinger()));

        MultipartFile coverFile = changeDTO.getCoverFile();
        File coverTempFile = null;
        if (null != changeDTO.getCoverFile()) {
            MusicMetaInfo.CoverPicture coverPicture = new MusicMetaInfo.CoverPicture();

            String filenameExtension = org.springframework.util.StringUtils.getFilenameExtension(coverFile.getOriginalFilename());
            coverTempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString().replaceAll("-", "") + "." + filenameExtension);
            coverFile.transferTo(coverTempFile);
            coverPicture.setFile(coverTempFile);
            coverPicture.setMimeType(coverFile.getContentType());
            musicMetaInfo.setCoverPictures(Collections.singletonList(coverPicture));
        }

        MultipartFile musicFile = changeDTO.getMusicFile();
        MusicDTO musicDTO = null;
        if (null != musicFile) {
            log.debug("修改音乐文件 {}", musicFile.getOriginalFilename());
            try {
                musicDTO = uploadService.editMusic(changeDTO.getMusicId(), musicFile, musicMetaInfo);
            } finally {
                if (null != coverTempFile) {
                    log.info("删除封面临时文件，结果：{}", coverTempFile.delete());
                }
            }
        } else if (musicMetaInfo.needModify()) {
            log.debug("修改音乐元数据 {}", musicMetaInfo);
            uploadService.editMetaInfo(changeDTO.getMusicId(), musicMetaInfo);
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
        Music savedMusic = musicRepository.update(music);
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

        MusicMetaInfo musicMetaInfo = new MusicMetaInfo();
        musicMetaInfo.setTitle(music.getName());
        musicMetaInfo.setArtists(Collections.singletonList(music.getSinger()));
        musicMetaInfo.setAlbum(null);
        MultipartFile coverFile = music.getCoverFile();
        File coverTempFile = null;
        if (null != coverFile) {
            MusicMetaInfo.CoverPicture coverPicture = new MusicMetaInfo.CoverPicture();

            String filenameExtension = org.springframework.util.StringUtils.getFilenameExtension(coverFile.getOriginalFilename());
            coverTempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + UUID.randomUUID().toString().replaceAll("-", "") + "." + filenameExtension);
            coverFile.transferTo(coverTempFile);
            coverPicture.setFile(coverTempFile);
            coverPicture.setMimeType(coverFile.getContentType());
            musicMetaInfo.setCoverPictures(Collections.singletonList(coverPicture));
        }

        try {
            MusicDTO musicDTO = uploadService.uploadMusic(musicFile, musicMetaInfo);
            MultipartFile lyricFile = music.getLyricFile();
            if (null != lyricFile) {
                byte[] contentBytes = uploadService.uploadLyric(musicDTO.getMusicId(), lyricFile);
                searchService.saveOrUpdateLyric(musicDTO.getMusicId(), musicDTO.getLyricId(), new String(contentBytes, StandardCharsets.UTF_8));
            }
            return musicDTO;
        } finally {
            if (null != coverTempFile) {
                log.info("删除封面临时文件，结果：{}", coverTempFile.delete());
            }
        }
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
        musicRepository.deleteById(music.getId());
        searchService.deleteLyric(music.getLyricId());
    }
}
