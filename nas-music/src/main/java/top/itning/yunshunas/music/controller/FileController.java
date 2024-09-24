package top.itning.yunshunas.music.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import top.itning.yunshunas.music.service.FileService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import java.io.IOException;

/**
 * 音乐文件相关接口
 *
 * @author itning
 * @since 2019/7/14 18:48
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

    /**
     * 获取音乐
     *
     * @param id       歌曲ID
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @throws Exception 获取失败
     */
    @GetMapping
    public void getMusic(@NotNull(message = "ID不存在") String id,
                         HttpServletRequest request,
                         HttpServletResponse response) throws Exception {
        fileService.getOneMusic(id, request, response);
    }

    /**
     * 获取歌词
     *
     * @param id 歌词ID
     * @return 歌词
     * @throws IOException 歌词获取失败
     */
    @ResponseBody
    @GetMapping(value = "/lyric", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getLyric(@NotNull(message = "ID不存在") String id) throws IOException {
        return fileService.getLyric(id);
    }

    /**
     * 获取封面
     *
     * @param range    {@link org.springframework.http.HttpHeaders#RANGE}
     * @param id       歌曲ID
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @throws Exception 获取失败
     */
    @GetMapping("/cover")
    public void metaInfo(@RequestHeader(required = false) String range,
                         @NotNull(message = "ID不存在") String id,
                         HttpServletRequest request,
                         HttpServletResponse response) throws Exception {
        fileService.getMusicCover(id, range, request, response);
    }
}
