package top.itning.yunshunas.music.webdav;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.service.FileService;

import java.util.Optional;

/**
 * @author itning
 * @since 2024/9/24 20:27
 */
@Slf4j
@Controller
public class WebDavMusicController {
    public static final String NAME_SINGLE_EXT = "/webdav/{name} - {single}.{ext}";
    public static final String ID_ID_EXT = "/webdav/id_{id}.{ext}";

    private final FileService fileService;

    public WebDavMusicController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping(NAME_SINGLE_EXT)
    public void webDavIndex(@PathVariable String name, @PathVariable String single, @PathVariable String ext, HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("webdav request info. name: [{}] single: [{}] ext: [{}]", name, single, ext);
        Optional<MusicType> musicTypeOptional = MusicType.getFromExt(ext);
        if (musicTypeOptional.isEmpty()) {
            response.setStatus(HttpStatus.NOT_FOUND.value());
            return;
        }
        fileService.getOneMusic(name, single, musicTypeOptional.get(), request, response);
    }

    @GetMapping(ID_ID_EXT)
    public void webDavIndexUseId(@PathVariable String id, @PathVariable String ext, HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("webdav request info. id: [{}] ext: [{}]", id, ext);
        fileService.getOneMusic(id, request, response);
    }
}
