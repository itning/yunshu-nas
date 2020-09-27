package top.itning.yunshu.yunshunas.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.itning.yunshu.yunshunas.config.MusicConfigProperties;
import top.itning.yunshu.yunshunas.constant.MusicType;
import top.itning.yunshu.yunshunas.repository.MusicRepository;
import top.itning.yunshu.yunshunas.service.FileService;
import top.itning.yunshu.yunshunas.util.MultipartFileSender;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.file.Paths;

/**
 * @author itning
 * @date 2020/9/5 12:30
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {
    private final MusicRepository musicRepository;
    private final MusicConfigProperties musicConfigProperties;

    @Autowired
    public FileServiceImpl(MusicRepository musicRepository, MusicConfigProperties musicConfigProperties) {
        this.musicRepository = musicRepository;
        this.musicConfigProperties = musicConfigProperties;
    }

    @Override
    public void getOneMusic(String id, String range, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String mediaType = musicRepository.findByMusicId(id)
                .flatMap(music -> MusicType.getMediaType(music.getType()))
                .orElse(MusicType.MP3.getMediaType());
        log.debug("Media Type: {}", mediaType);
        MultipartFileSender.fromPath(Paths.get(musicConfigProperties.getFilePath(), id))
                .setContentType(mediaType)
                .with(request)
                .with(response)
                .serveResource();
    }
}
