package top.itning.yunshunas.music.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.itning.yunshunas.music.dto.RestModel;
import top.itning.yunshunas.music.service.MusicService;

import javax.validation.constraints.NotEmpty;

/**
 * @author itning
 * @date 2020/9/5 11:25
 */
@Validated
@RestController
@RequestMapping("/music")
public class MusicController {
    private final MusicService musicService;

    @Autowired
    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }

    @GetMapping
    public ResponseEntity<?> getAll(@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable page) {
        return RestModel.ok(musicService.findAll(page));
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable page,
                                    @NotEmpty(message = "关键字不能为空") String keyword) {
        return RestModel.ok(musicService.fuzzySearch(keyword, page));
    }

    @GetMapping("/search/name")
    public ResponseEntity<?> searchName(@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable page,
                                        @NotEmpty(message = "关键字不能为空") String keyword) {
        return RestModel.ok(musicService.fuzzySearchName(keyword, page));
    }

    @GetMapping("/search/singer")
    public ResponseEntity<?> searchSinger(@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable page,
                                          @NotEmpty(message = "关键字不能为空") String keyword) {
        return RestModel.ok(musicService.fuzzySearchSinger(keyword, page));
    }
}
