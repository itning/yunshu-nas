package top.itning.yunshunas.music.webdav;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import top.itning.yunshunas.music.service.FileService;

/**
 * @author itning
 * @since 2024/9/24 20:27
 */
@Slf4j
@Controller
public class WebDavMusicController {
    private final FileService fileService;

    public WebDavMusicController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping("{name}.{ext}")
    public void webDavIndex(@PathVariable String name, @PathVariable String ext, @RequestHeader(required = false) String range, HttpServletRequest request, HttpServletResponse response) throws Exception {
        log.info("get name: {} ext: {}", name, ext);
        fileService.getOneMusic("277d5e1275a04e0299b635dba7c0ecb7", range, request, response);
    }
}
