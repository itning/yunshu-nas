package top.itning.yunshunas.music.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.itning.yunshunas.common.model.RestModel;
import top.itning.yunshunas.music.dto.MusicChangeDTO;
import top.itning.yunshunas.music.dto.MusicManageDTO;
import top.itning.yunshunas.music.service.MusicManageService;

import javax.validation.constraints.NotEmpty;

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
     * @param page 分页信息
     * @return 音乐列表
     */
    @GetMapping("/list")
    public ResponseEntity<RestModel<Page<MusicManageDTO>>> musicList(@PageableDefault(size = 20, sort = "gmtCreate", direction = Sort.Direction.DESC) Pageable page) {
        return RestModel.ok(musicManageService.getMusicList(page));
    }

    /**
     * 搜索音乐和歌手
     *
     * @param keyword 关键字
     * @param page    分页信息
     * @return 音乐列表
     */
    @GetMapping("/list/search")
    public ResponseEntity<RestModel<Page<MusicManageDTO>>> search(@PageableDefault(size = 20, sort = "gmtCreate", direction = Sort.Direction.DESC) Pageable page,
                                                                  @NotEmpty(message = "关键字不能为空") String keyword) {
        return RestModel.ok(musicManageService.fuzzySearch(keyword, page));
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
    public ResponseEntity<RestModel<String>> editMusic(@ModelAttribute MusicChangeDTO music) throws Exception {
        musicManageService.editMusic(music);
        return RestModel.ok("success");
    }
}
