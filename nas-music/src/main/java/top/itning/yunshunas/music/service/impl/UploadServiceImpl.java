package top.itning.yunshunas.music.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;
import top.itning.yunshunas.music.dto.MusicMetaInfo;
import top.itning.yunshunas.music.entity.Music;
import top.itning.yunshunas.music.repository.MusicRepository;
import top.itning.yunshunas.music.service.MusicMetaInfoService;
import top.itning.yunshunas.music.service.UploadService;

import java.io.File;
import java.util.UUID;

/**
 * @author itning
 * @since 2021/10/16 11:34
 */
@Slf4j
@Service
public class UploadServiceImpl implements UploadService {
    private final MusicMetaInfoService musicMetaInfoService;
    private final MusicRepository musicRepository;
    private final MusicDataSource musicDataSource;
    private final LyricDataSource lyricDataSource;

    @Autowired
    public UploadServiceImpl(MusicMetaInfoService musicMetaInfoService,
                             MusicRepository musicRepository,
                             MusicDataSource musicDataSource,
                             LyricDataSource lyricDataSource) {
        this.musicMetaInfoService = musicMetaInfoService;
        this.musicRepository = musicRepository;
        this.musicDataSource = musicDataSource;
        this.lyricDataSource = lyricDataSource;
    }

    @Override
    public void uploadMusic(MultipartFile file) throws Exception {
        String musicId = UUID.randomUUID().toString().replaceAll("-", "");
        File musicTempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + musicId);
        file.transferTo(musicTempFile);

        MusicType musicType = MusicType.getMusicTypeFromFilePath(file.getOriginalFilename()).orElseThrow(() -> new IllegalArgumentException("不支持的文件类型"));
        MusicMetaInfo musicMetaInfo = musicMetaInfoService.metaInfo(musicTempFile, musicType);
        if (null == musicMetaInfo) {
            throw new IllegalArgumentException("音乐标签数据解析失败");
        }
        if (StringUtils.isBlank(musicMetaInfo.getTitle())) {
            throw new IllegalArgumentException("音乐标题为空");
        }
        if (CollectionUtils.isEmpty(musicMetaInfo.getArtists())) {
            throw new IllegalArgumentException("艺术家为空");
        }
        if (StringUtils.isBlank(musicMetaInfo.getAlbum())) {
            log.warn("{} 专辑信息为空", file.getOriginalFilename());
        }
        if (CollectionUtils.isEmpty(musicMetaInfo.getCoverPictures())) {
            log.warn("{} 封面信息为空", file.getOriginalFilename());
        }
        musicDataSource.addMusic(musicTempFile, musicType, musicId);
        Music music = new Music();
        music.setMusicId(musicId);
        music.setName(musicMetaInfo.getTitle());
        music.setSinger(musicMetaInfo.getArtists().get(0));
        music.setLyricId(musicId);
        music.setType(musicType.getType());
        log.info("写入数据库：{}", music);
        try {
            musicRepository.save(music);
            musicRepository.flush();
        } catch (Exception e) {
            log.error("写入数据库异常，移除已经拷贝的文件：{}", musicDataSource.deleteMusic(musicId));
        }
        log.info("上传音乐文件完成，音乐ID：{}", musicId);
    }

    @Override
    public void uploadLyric(String musicId, MultipartFile file) throws Exception {
        Music music = musicRepository.findByMusicId(musicId).orElseThrow(() -> new IllegalArgumentException("音乐没找到：" + musicId));
        String lyricId = music.getLyricId();
        lyricDataSource.addLyric(file.getInputStream(), lyricId);
    }
}
