package top.itning.yunshunas.music.service.impl;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.itning.yunshunas.common.util.MultipartFileSender;
import top.itning.yunshunas.music.config.DataSourceConfig;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.datasource.DataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;
import top.itning.yunshunas.music.dto.MusicMetaInfo;
import top.itning.yunshunas.music.entity.Music;
import top.itning.yunshunas.music.repository.MusicRepository;
import top.itning.yunshunas.music.service.FileService;
import top.itning.yunshunas.music.service.MusicMetaInfoService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;

import static org.springframework.http.HttpHeaders.*;

/**
 * @author itning
 * @since 2020/9/5 12:30
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {
    private final MusicRepository musicRepository;
    private final MusicMetaInfoService musicMetaInfoService;

    private final Map<Class<? extends DataSource>, DataSourceConfig.DataSourceWrapper> readDataSourceMap;

    @Autowired
    public FileServiceImpl(MusicRepository musicRepository,
                           MusicMetaInfoService musicMetaInfoService,
                           Map<Class<? extends DataSource>, DataSourceConfig.DataSourceWrapper> readDataSourceMap) {
        this.musicRepository = musicRepository;
        this.musicMetaInfoService = musicMetaInfoService;
        this.readDataSourceMap = readDataSourceMap;
    }

    @Override
    public void getOneMusic(String id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String mediaType = musicRepository.findByMusicId(id)
                .flatMap(music -> MusicType.getMediaType(music.getType()))
                .orElse(MusicType.MP3.getMediaType());
        log.debug("Media Type: {}", mediaType);
        DataSourceConfig.DataSourceWrapper dataSourceWrapper = readDataSourceMap.get(MusicDataSource.class);
        MultipartFileSender.fromPath(Paths.get(dataSourceWrapper.config().getMusicFileDir(), id))
                .setContentType(mediaType)
                .with(request)
                .with(response)
                .no304CodeReturn()
                .serveResource();
    }

    @Override
    public void getOneMusic(String musicName, String singer, MusicType type, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Optional<Music> result = musicRepository.findByNameAndSingerAndType(musicName, singer, type.getType());
        if (result.isEmpty()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        Music music = result.get();
        String mediaType = MusicType.getMediaType(music.getType()).orElse(MusicType.MP3.getMediaType());
        DataSourceConfig.DataSourceWrapper dataSourceWrapper = readDataSourceMap.get(MusicDataSource.class);
        MultipartFileSender.fromPath(Paths.get(dataSourceWrapper.config().getMusicFileDir(), music.getMusicId()))
                .setContentType(mediaType)
                .with(request)
                .with(response)
                .no304CodeReturn()
                .serveResource();
    }

    @Override
    public String getLyric(String id) throws IOException {
        DataSourceConfig.DataSourceWrapper dataSourceWrapper = readDataSourceMap.get(LyricDataSource.class);
        File file = new File(dataSourceWrapper.config().getLyricFileDir() + File.separator + id);
        if (file.exists()) {
            return FileUtils.readFileToString(new File(dataSourceWrapper.config().getLyricFileDir() + File.separator + id), StandardCharsets.UTF_8);
        } else {
            return "";
        }
    }

    @Override
    public MusicMetaInfo getMusicMetaInfo(String id) {
        MusicType musicType = musicRepository.findByMusicId(id)
                .flatMap(music -> MusicType.getMediaTypeEnum(music.getType()))
                .orElse(MusicType.MP3);
        try {
            DataSourceConfig.DataSourceWrapper dataSourceWrapper = readDataSourceMap.get(MusicDataSource.class);
            return musicMetaInfoService.metaInfo(new File(dataSourceWrapper.config().getMusicFileDir() + File.separator + id), musicType);
        } catch (Exception e) {
            log.error("获取音乐信息失败", e);
        }
        return null;
    }

    @Override
    public void getMusicCover(String id, String range, HttpServletRequest request, HttpServletResponse response) throws Exception {
        MusicMetaInfo metaInfo = this.getMusicMetaInfo(id);
        if (null == metaInfo || CollectionUtils.isEmpty(metaInfo.getCoverPictures())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        MusicMetaInfo.CoverPicture coverPicture = metaInfo.getCoverPictures().get(0);
        byte[] binaryData = coverPicture.getBinaryData();
        if (null == binaryData) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setHeader(CONTENT_TYPE, coverPicture.getMimeType() == null ? "image/png" : coverPicture.getMimeType());
        String origin = request.getHeader(ORIGIN);
        response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        response.setHeader(ACCESS_CONTROL_ALLOW_METHODS, "POST,GET,OPTIONS,DELETE,PUT,PATCH");
        response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS, request.getHeader(ACCESS_CONTROL_REQUEST_HEADERS));
        response.setIntHeader(ACCESS_CONTROL_MAX_AGE, 2592000);
        response.setIntHeader(CONTENT_LENGTH, binaryData.length);
        response.setStatus(HttpStatus.OK.value());
        try (ServletOutputStream output = response.getOutputStream()) {
            output.write(binaryData);
            output.flush();
        } catch (Exception e) {
            log.error("发送封面数据失败", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
