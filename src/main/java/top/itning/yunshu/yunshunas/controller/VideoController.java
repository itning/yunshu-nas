package top.itning.yunshu.yunshunas.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import top.itning.yunshu.yunshunas.service.VideoService;
import top.itning.yunshu.yunshunas.video.VideoTransformHandler;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

/**
 * @author itning
 */
@Controller
public class VideoController {
    private static final Logger logger = LoggerFactory.getLogger(VideoController.class);

    private final VideoTransformHandler videoTransformHandler;
    private final VideoService videoService;

    public VideoController(VideoTransformHandler videoTransformHandler, VideoService videoService) {
        this.videoTransformHandler = videoTransformHandler;
        this.videoService = videoService;
    }

    /**
     * 请求m3u8
     *
     * @param name     文件名
     * @param response {@link HttpServletResponse}
     * @throws IOException IOException
     */
    @GetMapping("/hls/{name}.m3u8")
    public void m3u8(@PathVariable String name, HttpServletResponse response) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("request {}.m3u8", name);
        }
        videoService.getM3u8File(name, response.getOutputStream());
    }

    /**
     * 请求ts
     *
     * @param name     文件名
     * @param index    索引
     * @param response {@link HttpServletResponse}
     * @throws IOException IOException
     */
    @GetMapping("/hls/{name}-{index}.ts")
    public void ts(@PathVariable String name, @PathVariable String index, HttpServletResponse response) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("request {}-{}.ts", name, index);
        }
        videoService.getTsFile(String.format("%s-%s", name, index), response.getOutputStream());
    }


    /**
     * 转换或查看
     *
     * @param location 路径
     * @param model    {@link Model}
     * @return html
     */
    @GetMapping("/video")
    public String video(@RequestParam String location, Model model) {
        boolean put = videoTransformHandler.put(location);
        if (put) {
            return "progress";
        } else {
            String hex = DigestUtils.md5DigestAsHex(location.getBytes());
            int i = location.lastIndexOf(File.separator);
            model.addAttribute("file", location.substring(i + 1));
            model.addAttribute("name", hex);
            return "video";
        }
    }
}
