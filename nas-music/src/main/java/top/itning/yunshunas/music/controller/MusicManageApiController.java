package top.itning.yunshunas.music.controller;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.itning.yunshunas.common.model.RestModel;
import top.itning.yunshunas.music.dto.MusicChangeDTO;
import top.itning.yunshunas.music.dto.MusicDTO;
import top.itning.yunshunas.music.dto.MusicManageDTO;
import top.itning.yunshunas.music.service.MusicManageService;

import java.util.List;

/**
 * 音乐管理接口
 *
 * @author itning
 * @since 2022/7/10 14:33
 */
@Validated
@RestController
@RequestMapping("/api/music")
public class MusicManageApiController {
    private final MusicManageService musicManageService;

    @Autowired
    public MusicManageApiController(MusicManageService musicManageService) {
        this.musicManageService = musicManageService;
    }

    /**
     * 获取全部音乐
     *
     * @return 音乐列表
     */
    @GetMapping("/list")
    public ResponseEntity<RestModel<List<MusicManageDTO>>> musicList() {
        return RestModel.ok(musicManageService.getMusicList());
    }

    /**
     * 搜索音乐和歌手
     *
     * @param keyword 关键字
     * @return 音乐列表
     */
    @GetMapping("/list/search")
    public ResponseEntity<RestModel<List<MusicManageDTO>>> search(@NotEmpty(message = "关键字不能为空") String keyword) {
        return RestModel.ok(musicManageService.fuzzySearch(keyword));
    }

    /**
     * 获取一个音乐
     *
     * @param id 音乐ID
     * @return 音乐
     */
    @GetMapping("/id/{id}")
    public ResponseEntity<RestModel<MusicManageDTO>> getOneMusic(@NotEmpty(message = "音乐ID不能为空") @PathVariable String id) {
        return RestModel.ok(musicManageService.getOneMusic(id));
    }

    /**
     * 修改音乐
     *
     * @param music 音乐信息
     * @return 修改结果
     */
    @PostMapping("/edit")
    public ResponseEntity<RestModel<MusicDTO>> editMusic(@ModelAttribute MusicChangeDTO music) throws Exception {
        return RestModel.ok(musicManageService.editMusic(music));
    }

    /**
     * 新增音乐
     *
     * @param music 音乐信息
     * @return 新增结果
     * @throws Exception 新增异常
     */
    @PostMapping("/add")
    public ResponseEntity<RestModel<MusicDTO>> addMusic(@ModelAttribute MusicChangeDTO music) throws Exception {
        return RestModel.created(musicManageService.addMusic(music));
    }

    /**
     * 删除音乐
     *
     * @param id 音乐ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<RestModel<String>> deleteMusic(@NotEmpty(message = "音乐ID不能为空") @PathVariable String id) {
        musicManageService.deleteMusic(id);
        return RestModel.ok("success");
    }
}
