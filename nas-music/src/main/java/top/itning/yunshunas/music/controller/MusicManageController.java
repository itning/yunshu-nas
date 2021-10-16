package top.itning.yunshunas.music.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import top.itning.yunshunas.music.dto.MusicDTO;
import top.itning.yunshunas.music.service.MusicService;
import top.itning.yunshunas.music.service.UploadService;

/**
 * 音乐管理接口
 *
 * @author itning
 * @since 2021/10/16 9:47
 */
@Controller
@RequestMapping("/musicManage")
public class MusicManageController {
    private final MusicService musicService;
    private final UploadService uploadService;

    @Autowired
    public MusicManageController(MusicService musicService, UploadService uploadService) {
        this.musicService = musicService;
        this.uploadService = uploadService;
    }

    /**
     * 音乐列表页
     *
     * @param model {@link Model}
     * @return 音乐列表
     */
    @GetMapping("/musicList")
    public String musicList(Model model, @PageableDefault(size = 20, sort = "gmtModified", direction = Sort.Direction.DESC) Pageable page) {
        Page<MusicDTO> musicPage = musicService.findAll(page);
        model.addAttribute("musicPage", musicPage);
        return "music_list";
    }

    @GetMapping("/musicUpload")
    public String musicUpload() {
        return "music_upload";
    }

    @PostMapping("/musicUpload")
    public String doMusicUpload(@RequestParam("file") MultipartFile file) throws Exception {
        uploadService.uploadMusic(file);
        return "music_upload";
    }

    @PostMapping("/lyricUpload")
    public String doLyricUpload(@RequestParam("musicId") String musicId, @RequestParam("file") MultipartFile file) throws Exception {
        uploadService.uploadLyric(musicId, file);
        return "music_upload";
    }
}
