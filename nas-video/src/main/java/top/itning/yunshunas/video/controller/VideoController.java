package top.itning.yunshunas.video.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import top.itning.yunshunas.common.util.MultipartFileSender;
import top.itning.yunshunas.video.service.VideoService;
import top.itning.yunshunas.video.video.VideoTransformHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

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
        videoService.getTsFile(name + "-" + index, response.getOutputStream());
    }

    /**
     * 视频直接返回
     *
     * @param path     视频文件路径
     * @param request  {@link HttpServletRequest}
     * @param response {@link HttpServletResponse}
     * @throws Exception 文件发送出错
     */
    @GetMapping("/video/{path}")
    public void videoForPath(@PathVariable String path, HttpServletRequest request, HttpServletResponse response) throws Exception {
        byte[] decode = Base64.getUrlDecoder().decode(path.getBytes(StandardCharsets.UTF_8));
        Path p = Paths.get(new String(decode, StandardCharsets.UTF_8));
        MultipartFileSender.fromPath(p)
                .with(request)
                .with(response)
                .no304CodeReturn()
                .setContentType(Files.probeContentType(p))
                .serveResource();
    }
}
