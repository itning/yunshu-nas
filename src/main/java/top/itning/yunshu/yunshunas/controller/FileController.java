package top.itning.yunshu.yunshunas.controller;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import top.itning.yunshu.yunshunas.entity.FileEntity;
import top.itning.yunshu.yunshunas.entity.Link;
import top.itning.yunshu.yunshunas.repository.IVideoRepository;
import top.itning.yunshu.yunshunas.service.VideoService;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author itning
 * @date 2019/7/14 18:48
 */
@Controller
public class FileController {
    private final VideoService videoService;
    private final IVideoRepository iVideoRepository;

    @Autowired
    public FileController(VideoService videoService, IVideoRepository iVideoRepository) {
        this.videoService = videoService;
        this.iVideoRepository = iVideoRepository;
    }

    @GetMapping("/")
    public String index(Model model, String location) throws UnsupportedEncodingException {
        if (location != null) {
            model.addAttribute("links", Link.build(location));
        }
        List<FileEntity> fileEntityList = videoService.getFileEntities(location);
        model.addAttribute("files", fileEntityList);
        return "index";
    }

    @PostMapping("/del")
    @ResponseBody
    public void delFile(@RequestParam String location) throws IOException {
        String writeDir = iVideoRepository.getWriteDir(location);
        FileUtils.deleteDirectory(new File(writeDir));
        File file = new File(location);
        if (!file.exists()) {
            throw new RuntimeException("文件不存在");
        }
        if (!file.delete()) {
            throw new RuntimeException("文件删除失败");
        }
    }
}
