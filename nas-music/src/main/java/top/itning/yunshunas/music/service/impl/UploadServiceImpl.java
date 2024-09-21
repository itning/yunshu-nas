package top.itning.yunshunas.music.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.converter.MusicConverter;
import top.itning.yunshunas.music.datasource.CoverDataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;
import top.itning.yunshunas.music.dto.MusicDTO;
import top.itning.yunshunas.music.dto.MusicMetaInfo;
import top.itning.yunshunas.music.entity.Music;
import top.itning.yunshunas.music.repository.MusicRepository;
import top.itning.yunshunas.music.service.MusicMetaInfoService;
import top.itning.yunshunas.music.service.UploadService;

import java.io.ByteArrayInputStream;
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
    private final CoverDataSource coverDataSource;
    private final RestTemplate restTemplate;

    @Autowired
    public UploadServiceImpl(MusicMetaInfoService musicMetaInfoService,
                             MusicRepository musicRepository,
                             MusicDataSource musicDataSource,
                             LyricDataSource lyricDataSource,
                             CoverDataSource coverDataSource,
                             RestTemplate restTemplate) {
        this.musicMetaInfoService = musicMetaInfoService;
        this.musicRepository = musicRepository;
        this.musicDataSource = musicDataSource;
        this.lyricDataSource = lyricDataSource;
        this.coverDataSource = coverDataSource;
        this.restTemplate = restTemplate;
    }

    @Override
    public MusicDTO uploadMusic(MultipartFile file, MusicMetaInfo musicMetaInfo) throws Exception {
        String musicId = UUID.randomUUID().toString().replaceAll("-", "");
        File musicTempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + musicId);
        file.transferTo(musicTempFile);

        MusicType musicType = MusicType.getMusicTypeFromFilePath(file.getOriginalFilename()).orElseThrow(() -> new IllegalArgumentException("不支持的文件类型"));

        MusicMetaInfo musicMetaInfoFromFile = musicMetaInfoService.metaInfo(musicTempFile, musicType);

        if (null == musicMetaInfoFromFile) {
            throw new IllegalArgumentException("音乐标签数据解析失败");
        }

        if (StringUtils.isAllBlank(musicMetaInfoFromFile.getTitle(), musicMetaInfo.getTitle())) {
            throw new IllegalArgumentException("音乐标题为空");
        }
        if (CollectionUtils.isEmpty(musicMetaInfoFromFile.getArtists()) && CollectionUtils.isEmpty(musicMetaInfo.getArtists())) {
            throw new IllegalArgumentException("艺术家为空");
        }
        if (StringUtils.isBlank(musicMetaInfoFromFile.getAlbum())) {
            log.warn("{} 专辑信息为空", file.getOriginalFilename());
        }
        if (CollectionUtils.isEmpty(musicMetaInfoFromFile.getCoverPictures()) && CollectionUtils.isEmpty(musicMetaInfo.getCoverPictures())) {
            log.warn("{} 封面信息为空", file.getOriginalFilename());
        } else {
            MusicMetaInfo.CoverPicture coverPicture = !CollectionUtils.isEmpty(musicMetaInfo.getCoverPictures()) ?
                    musicMetaInfo.getCoverPictures().get(0) :
                    musicMetaInfoFromFile.getCoverPictures().get(0);

            if (null != coverPicture.getFile()) {
                coverPicture.setBinaryData(FileUtils.readFileToByteArray(coverPicture.getFile()));
            }

            if (coverPicture.getBinaryData() != null && coverPicture.getBinaryData().length > 0) {
                String mimeType = coverPicture.getMimeType();
                if (StringUtils.isBlank(mimeType)) {
                    mimeType = "image/png";
                }
                coverDataSource.addCover(musicId, mimeType, coverPicture.getBinaryData());
            }
        }
        try {
            musicMetaInfoService.editMetaInfo(musicTempFile, musicType, merge(musicMetaInfoFromFile, musicMetaInfo));
            musicDataSource.addMusic(musicTempFile, musicType, musicId);
        } catch (Exception e) {
            coverDataSource.deleteCover(musicId);
            musicDataSource.deleteMusic(musicId);
            throw e;
        }
        Music music = new Music();
        music.setMusicId(musicId);
        music.setName(StringUtils.isNotBlank(musicMetaInfo.getTitle()) ? musicMetaInfo.getTitle() : musicMetaInfoFromFile.getTitle());
        music.setSinger(!CollectionUtils.isEmpty(musicMetaInfo.getArtists()) ? musicMetaInfo.getArtists().get(0) : musicMetaInfoFromFile.getArtists().get(0));
        music.setLyricId(musicId);
        music.setType(musicType.getType());
        log.info("写入数据库：{}", music);
        try {
            musicRepository.save(music);
        } catch (Exception e) {
            log.error("写入数据库异常，移除已经拷贝的文件：music {} cover {}", musicDataSource.deleteMusic(musicId), coverDataSource.deleteCover(musicId), e);
            throw e;
        }
        log.info("上传音乐文件完成，音乐ID：{}", musicId);
        MusicDTO musicDTO = MusicConverter.INSTANCE.entity2dto(music);
        musicDTO.setMusicUri(musicDataSource.getMusic(musicDTO.getMusicId()));
        musicDTO.setLyricUri(lyricDataSource.getLyric(musicDTO.getLyricId()));
        musicDTO.setCoverUri(coverDataSource.getCover(musicDTO.getMusicId()));
        return musicDTO;
    }

    @Override
    public MusicDTO editMusic(String musicId, MultipartFile file, MusicMetaInfo musicMetaInfo) throws Exception {

        Music music = musicRepository.findByMusicId(musicId).orElseThrow(() -> new IllegalArgumentException("音乐不存在"));

        File musicTempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + musicId);
        file.transferTo(musicTempFile);

        MusicType musicType = MusicType.getMusicTypeFromFilePath(file.getOriginalFilename()).orElseThrow(() -> new IllegalArgumentException("不支持的文件类型"));
        MusicMetaInfo musicMetaInfoFromFile = musicMetaInfoService.metaInfo(musicTempFile, musicType);
        if (null == musicMetaInfoFromFile) {
            throw new IllegalArgumentException("音乐标签数据解析失败");
        }

        if (StringUtils.isAllBlank(musicMetaInfoFromFile.getTitle(), musicMetaInfo.getTitle())) {
            throw new IllegalArgumentException("音乐标题为空");
        }
        if (CollectionUtils.isEmpty(musicMetaInfoFromFile.getArtists()) && CollectionUtils.isEmpty(musicMetaInfo.getArtists())) {
            throw new IllegalArgumentException("艺术家为空");
        }

        if (StringUtils.isBlank(musicMetaInfoFromFile.getAlbum())) {
            log.warn("{} 专辑信息为空", file.getOriginalFilename());
        }

        if (CollectionUtils.isEmpty(musicMetaInfoFromFile.getCoverPictures())) {
            log.warn("{} 封面信息为空", file.getOriginalFilename());
        } else {
            MusicMetaInfo.CoverPicture coverPicture = !CollectionUtils.isEmpty(musicMetaInfo.getCoverPictures()) ?
                    musicMetaInfo.getCoverPictures().get(0) :
                    musicMetaInfoFromFile.getCoverPictures().get(0);

            if (null != coverPicture.getFile()) {
                coverPicture.setBinaryData(FileUtils.readFileToByteArray(coverPicture.getFile()));
            }
            if (coverPicture.getBinaryData() != null && coverPicture.getBinaryData().length > 0) {
                String mimeType = coverPicture.getMimeType();
                if (StringUtils.isBlank(mimeType)) {
                    mimeType = "image/png";
                }
                coverDataSource.deleteCover(musicId);
                coverDataSource.addCover(musicId, mimeType, coverPicture.getBinaryData());
            }
        }
        musicDataSource.deleteMusic(musicId);
        musicMetaInfoService.editMetaInfo(musicTempFile, musicType, merge(musicMetaInfoFromFile, musicMetaInfo));
        musicDataSource.addMusic(musicTempFile, musicType, musicId);

        music.setName(StringUtils.isNotBlank(musicMetaInfo.getTitle()) ? musicMetaInfo.getTitle() : musicMetaInfoFromFile.getTitle());
        music.setSinger(!CollectionUtils.isEmpty(musicMetaInfo.getArtists()) ? musicMetaInfo.getArtists().get(0) : musicMetaInfoFromFile.getArtists().get(0));
        music.setType(musicType.getType());
        music.setGmtModified(null);
        log.info("写入数据库：{}", music);
        try {
            musicRepository.update(music);
        } catch (Exception e) {
            log.error("写入数据库异常，移除已经拷贝的文件：{}", musicDataSource.deleteMusic(musicId));
        }
        log.info("修改音乐文件完成，音乐ID：{}", musicId);
        MusicDTO musicDTO = MusicConverter.INSTANCE.entity2dto(music);
        musicDTO.setMusicUri(musicDataSource.getMusic(musicDTO.getMusicId()));
        musicDTO.setLyricUri(lyricDataSource.getLyric(musicDTO.getLyricId()));
        musicDTO.setCoverUri(coverDataSource.getCover(musicDTO.getMusicId()));
        return musicDTO;
    }

    @Override
    public void editMetaInfo(String musicId, MusicMetaInfo musicMetaInfo) throws Exception {
        Music music = musicRepository.findByMusicId(musicId).orElseThrow(() -> new IllegalArgumentException("音乐不存在"));
        MusicType musicType = MusicType.getMediaTypeEnum(music.getType()).orElseThrow(() -> new IllegalArgumentException("不支持的文件类型"));
        File file = musicDataSource.getMusicFile(musicId);

        if (!CollectionUtils.isEmpty(musicMetaInfo.getCoverPictures())) {
            MusicMetaInfo.CoverPicture coverPicture = musicMetaInfo.getCoverPictures().get(0);

            if (null != coverPicture.getFile()) {
                coverPicture.setBinaryData(FileUtils.readFileToByteArray(coverPicture.getFile()));
            }
            if (coverPicture.getBinaryData() != null && coverPicture.getBinaryData().length > 0) {
                String mimeType = coverPicture.getMimeType();
                if (StringUtils.isBlank(mimeType)) {
                    mimeType = "image/png";
                }
                coverDataSource.deleteCover(musicId);
                coverDataSource.addCover(musicId, mimeType, coverPicture.getBinaryData());
            }
        }

        musicMetaInfoService.editMetaInfo(file, musicType, musicMetaInfo);
        musicDataSource.deleteMusic(musicId);
        musicDataSource.addMusic(file, musicType, musicId);

        if (StringUtils.isNotBlank(musicMetaInfo.getTitle())) {
            music.setName(musicMetaInfo.getTitle());
        }

        if (!CollectionUtils.isEmpty(musicMetaInfo.getArtists())) {
            music.setSinger(musicMetaInfo.getArtists().get(0));
        }

        music.setType(musicType.getType());
        music.setGmtModified(null);
        musicRepository.update(music);
    }

    @Override
    public byte[] uploadLyric(String musicId, MultipartFile file) throws Exception {
        Music music = musicRepository.findByMusicId(musicId).orElseThrow(() -> new IllegalArgumentException("音乐没找到：" + musicId));
        String lyricId = music.getLyricId();
        try {
            byte[] bytes = file.getBytes();
            lyricDataSource.addLyric(new ByteArrayInputStream(bytes), file.getSize(), lyricId);
            return bytes;
        } catch (Exception e) {
            lyricDataSource.deleteLyric(lyricId);
            throw e;
        }
    }

    @Override
    public byte[] editLyric(String musicId, MultipartFile file) throws Exception {
        Music music = musicRepository.findByMusicId(musicId).orElseThrow(() -> new IllegalArgumentException("音乐没找到：" + musicId));
        String lyricId = music.getLyricId();
        lyricDataSource.deleteLyric(lyricId);
        byte[] bytes = file.getBytes();
        lyricDataSource.addLyric(new ByteArrayInputStream(bytes), file.getSize(), lyricId);
        return bytes;
    }

    private MusicMetaInfo merge(MusicMetaInfo musicMetaInfoFromFile, MusicMetaInfo musicMetaInfoFromUser) {

        MusicMetaInfo needEditMusicMetaInfo = new MusicMetaInfo();
        if (StringUtils.isNotBlank(musicMetaInfoFromUser.getTitle()) && StringUtils.isNotBlank(musicMetaInfoFromFile.getTitle())) {
            if (!musicMetaInfoFromUser.getTitle().equals(musicMetaInfoFromFile.getTitle())) {
                needEditMusicMetaInfo.setTitle(musicMetaInfoFromUser.getTitle());
            }
        } else if (StringUtils.isNotBlank(musicMetaInfoFromUser.getTitle())) {
            needEditMusicMetaInfo.setTitle(musicMetaInfoFromUser.getTitle());
        }

        if (!CollectionUtils.isEmpty(musicMetaInfoFromUser.getArtists()) && !CollectionUtils.isEmpty(musicMetaInfoFromFile.getArtists())) {
            if (!musicMetaInfoFromUser.getArtists().equals(musicMetaInfoFromFile.getArtists())) {
                needEditMusicMetaInfo.setArtists(musicMetaInfoFromUser.getArtists());
            }
        } else if (CollectionUtils.isEmpty(musicMetaInfoFromUser.getArtists())) {
            needEditMusicMetaInfo.setArtists(musicMetaInfoFromUser.getArtists());
        }

        needEditMusicMetaInfo.setCoverPictures(musicMetaInfoFromUser.getCoverPictures());

        log.info("修改的文件信息：{}", needEditMusicMetaInfo);

        return needEditMusicMetaInfo;
    }
}
