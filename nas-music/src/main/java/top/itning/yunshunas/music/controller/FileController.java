package top.itning.yunshunas.music.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import top.itning.yunshunas.music.service.FileService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;

/**
 * @author itning
 * @date 2019/7/14 18:48
 */
@CrossOrigin
@Controller("musicFileController")
@RequestMapping("/file")
public class FileController {
    private final FileService fileService;

    @Autowired
    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @GetMapping
    public void getMusic(@RequestHeader(required = false) String range,
                         @NotNull(message = "ID不存在") String id,
                         HttpServletRequest request,
                         HttpServletResponse response) throws Exception {
        fileService.getOneMusic(id, range, request, response);
    }

    @ResponseBody
    @GetMapping(value = "/lyric", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getLyric(@NotNull(message = "ID不存在") String id) throws IOException {
        return fileService.getLyric(id);
    }

    @GetMapping("/cover")
    public void metaInfo(@RequestHeader(required = false) String range,
                         @NotNull(message = "ID不存在") String id,
                         HttpServletRequest request,
                         HttpServletResponse response) throws Exception {
        fileService.getMusicCover(id, range, request, response);
    }
}
