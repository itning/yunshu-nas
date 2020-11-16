package top.itning.yunshunas.music.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.common.util.MultipartFileSender;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.repository.MusicRepository;
import top.itning.yunshunas.music.service.FileService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

/**
 * @author itning
 * @date 2020/9/5 12:30
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {
    private final MusicRepository musicRepository;
    private final NasProperties nasProperties;

    @Autowired
    public FileServiceImpl(MusicRepository musicRepository, NasProperties nasProperties) {
        this.musicRepository = musicRepository;
        this.nasProperties = nasProperties;
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
        return FileUtils.readFileToString(new File(nasProperties.getLyricFileDir() + File.separator + id), StandardCharsets.UTF_8);
    }
}
