package top.itning.yunshunas.music.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import top.itning.yunshunas.music.service.FileService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;

/**
 * @author itning
 * @date 2019/7/14 18:48
 */
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
}
