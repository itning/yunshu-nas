package top.itning.yunshunas.music.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.common.util.MultipartFileSender;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.dto.MusicMetaInfo;
import top.itning.yunshunas.music.repository.MusicRepository;
import top.itning.yunshunas.music.service.FileService;
import top.itning.yunshunas.music.service.MusicMetaInfoService;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static org.springframework.http.HttpHeaders.*;

/**
 * @author itning
 * @date 2020/9/5 12:30
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {
    private final MusicRepository musicRepository;
    private final NasProperties nasProperties;
    private final MusicMetaInfoService musicMetaInfoService;

    @Autowired
    public FileServiceImpl(MusicRepository musicRepository, NasProperties nasProperties, MusicMetaInfoService musicMetaInfoService) {
        this.musicRepository = musicRepository;
        this.nasProperties = nasProperties;
        this.musicMetaInfoService = musicMetaInfoService;
    }

    @Override
    public void getOneMusic(String id, String range, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String mediaType = musicRepository.findByMusicId(id)
                .flatMap(music -> MusicType.getMediaType(music.getType()))
                .orElse(MusicType.MP3.getMediaType());
        log.debug("Media Type: {}", mediaType);
        MultipartFileSender.fromPath(Paths.get(nasProperties.getMusicFileDir(), id))
                .setContentType(mediaType)
                .with(request)
                .with(response)
                .serveResource();
    }

    @Override
    public String getLyric(String id) throws IOException {
        File file = new File(nasProperties.getLyricFileDir() + File.separator + id);
        if (file.exists()) {
            return FileUtils.readFileToString(new File(nasProperties.getLyricFileDir() + File.separator + id), StandardCharsets.UTF_8);
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
            return musicMetaInfoService.metaInfo(new File(nasProperties.getMusicFileDir() + File.separator + id), musicType);
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
        response.setHeader("Content-Type", coverPicture.getMimeType() == null ? "image/png" : coverPicture.getMimeType());
        String origin = request.getHeader(ORIGIN);
        response.setHeader(ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, origin);
        response.setHeader(ACCESS_CONTROL_ALLOW_METHODS, "POST,GET,OPTIONS,DELETE,PUT,PATCH");
        response.setHeader(ACCESS_CONTROL_ALLOW_HEADERS, request.getHeader(ACCESS_CONTROL_REQUEST_HEADERS));
        response.setIntHeader(ACCESS_CONTROL_MAX_AGE, 2592000);
        response.setHeader("Content-Length", String.valueOf(binaryData.length));
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
