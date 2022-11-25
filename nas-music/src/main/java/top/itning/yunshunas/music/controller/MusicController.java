package top.itning.yunshunas.music.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.itning.yunshunas.common.model.RestModel;
import top.itning.yunshunas.music.converter.SearchConverter;
import top.itning.yunshunas.music.dto.MusicDTO;
import top.itning.yunshunas.music.dto.MusicMetaInfo;
import top.itning.yunshunas.music.dto.SearchResultDTO;
import top.itning.yunshunas.music.entity.SearchResult;
import top.itning.yunshunas.music.service.FileService;
import top.itning.yunshunas.music.service.MusicService;
import top.itning.yunshunas.music.service.SearchService;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 音乐相关接口
 *
 * @author itning
 * @since 2020/9/5 11:25
 */
@Validated
@RestController
@RequestMapping("/music")
public class MusicController {
    private final MusicService musicService;
    private final FileService fileService;
    private final SearchService searchService;

    @Autowired
    public MusicController(MusicService musicService, FileService fileService, SearchService searchService) {
        this.musicService = musicService;
        this.fileService = fileService;
        this.searchService = searchService;
    }

    /**
     * 分页获取音乐列表
     *
     * @param page 分页信息
     * @return 音乐列表
     */
    @GetMapping
    public ResponseEntity<RestModel<Page<MusicDTO>>> getAll(@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable page) {
        return RestModel.ok(musicService.findAll(page));
    }

    /**
     * 搜索音乐和歌手
     *
     * @param page    分页信息
     * @param keyword 关键词
     * @return 搜索结果
     */
    @GetMapping("/search")
    public ResponseEntity<RestModel<Page<MusicDTO>>> search(@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable page,
                                                            @NotEmpty(message = "关键字不能为空") String keyword) {
        return RestModel.ok(musicService.fuzzySearch(keyword, page));
    }

    /**
     * 搜索音乐和歌手
     *
     * @param page    分页信息
     * @param keyword 关键词
     * @return 搜索结果
     */
    @GetMapping("/search_v2")
    public ResponseEntity<RestModel<List<SearchResultDTO>>> searchV2(@PageableDefault(size = 20) Pageable page,
                                                                     @NotEmpty(message = "关键字不能为空") String keyword) {
        List<SearchResult> searchResults = searchService.searchLyric(keyword, page);
        List<SearchResultDTO> result = SearchConverter.INSTANCE.entity2dto(searchResults);
        return RestModel.ok(result);
    }

    /**
     * 搜索歌名
     *
     * @param page    分页信息
     * @param keyword 关键词
     * @return 搜索结果
     */
    @GetMapping("/search/name")
    public ResponseEntity<RestModel<Page<MusicDTO>>> searchName(@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable page,
                                                                @NotEmpty(message = "关键字不能为空") String keyword) {
        return RestModel.ok(musicService.fuzzySearchName(keyword, page));
    }

    /**
     * 搜索歌手
     *
     * @param page    分页信息
     * @param keyword 关键词
     * @return 搜索结果
     */
    @GetMapping("/search/singer")
    public ResponseEntity<RestModel<Page<MusicDTO>>> searchSinger(@PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable page,
                                                                  @NotEmpty(message = "关键字不能为空") String keyword) {
        return RestModel.ok(musicService.fuzzySearchSinger(keyword, page));
    }

    /**
     * 获取歌曲元信息，包括歌手歌名和封面
     *
     * @param id 歌曲ID
     * @return 元信息
     */
    @GetMapping("/metaInfo")
    public ResponseEntity<RestModel<MusicMetaInfo>> metaInfo(@NotNull(message = "ID不存在") String id) {
        MusicMetaInfo metaInfo = fileService.getMusicMetaInfo(id);
        if (null != metaInfo && !CollectionUtils.isEmpty(metaInfo.getCoverPictures())) {
            metaInfo.getCoverPictures().forEach(item -> item.setBinaryData(null));
        }
        return RestModel.ok(metaInfo);
    }
}
