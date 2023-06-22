package top.itning.yunshunas.music.controller;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import top.itning.yunshunas.common.model.RestModel;
import top.itning.yunshunas.music.config.DataSourceConfig;
import top.itning.yunshunas.music.converter.SearchConverter;
import top.itning.yunshunas.music.datasource.impl.TencentCosDataSource;
import top.itning.yunshunas.music.dto.MusicDTO;
import top.itning.yunshunas.music.dto.MusicMetaInfo;
import top.itning.yunshunas.music.dto.SearchResultDTO;
import top.itning.yunshunas.music.entity.SearchResult;
import top.itning.yunshunas.music.service.FileService;
import top.itning.yunshunas.music.service.MusicService;
import top.itning.yunshunas.music.service.SearchService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final Map<String, DataSourceConfig.DataSourceWrapper> musicDataSourceMap;
    private final Map<String, DataSourceConfig.DataSourceWrapper> lyricDataSourceMap;
    private final Map<String, DataSourceConfig.DataSourceWrapper> coverDataSourceMap;

    @Autowired
    public MusicController(MusicService musicService,
                           FileService fileService,
                           SearchService searchService,
                           Map<String, DataSourceConfig.DataSourceWrapper> musicDataSourceMap,
                           Map<String, DataSourceConfig.DataSourceWrapper> lyricDataSourceMap,
                           Map<String, DataSourceConfig.DataSourceWrapper> coverDataSourceMap) {
        this.musicService = musicService;
        this.fileService = fileService;
        this.searchService = searchService;
        this.musicDataSourceMap = musicDataSourceMap;
        this.lyricDataSourceMap = lyricDataSourceMap;
        this.coverDataSourceMap = coverDataSourceMap;
    }

    /**
     * 分页获取音乐列表
     *
     * @return 音乐列表
     */
    @GetMapping
    public ResponseEntity<RestModel<List<MusicDTO>>> getAll() {
        return RestModel.ok(musicService.findAll());
    }

    /**
     * 搜索音乐和歌手
     *
     * @param keyword 关键词
     * @return 搜索结果
     */
    @GetMapping("/search")
    public ResponseEntity<RestModel<List<MusicDTO>>> search(@NotEmpty(message = "关键字不能为空") String keyword) {
        return RestModel.ok(musicService.fuzzySearch(keyword));
    }

    /**
     * 搜索音乐和歌手
     *
     * @param keyword 关键词
     * @return 搜索结果
     */
    @GetMapping("/search_v2")
    public ResponseEntity<RestModel<List<SearchResultDTO>>> searchV2(@NotEmpty(message = "关键字不能为空") String keyword) {
        List<SearchResult> searchResults = searchService.searchLyric(keyword);
        List<SearchResultDTO> result = SearchConverter.INSTANCE.entity2dto(searchResults);
        return RestModel.ok(result);
    }

    /**
     * 搜索歌名
     *
     * @param keyword 关键词
     * @return 搜索结果
     */
    @GetMapping("/search/name")
    public ResponseEntity<RestModel<List<MusicDTO>>> searchName(@NotEmpty(message = "关键字不能为空") String keyword) {
        return RestModel.ok(musicService.fuzzySearchName(keyword));
    }

    /**
     * 搜索歌手
     *
     * @param keyword 关键词
     * @return 搜索结果
     */
    @GetMapping("/search/singer")
    public ResponseEntity<RestModel<List<MusicDTO>>> searchSinger(@NotEmpty(message = "关键字不能为空") String keyword) {
        return RestModel.ok(musicService.fuzzySearchSinger(keyword));
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

    @GetMapping("/test")
    public ResponseEntity<RestModel<List<MusicDTO>>> test() {
        Optional<DataSourceConfig.DataSourceWrapper> musicOpt = musicDataSourceMap.values().stream().filter(it -> it.dataSource() instanceof TencentCosDataSource).findFirst();
        if (musicOpt.isPresent()) {
            TencentCosDataSource music = (TencentCosDataSource) musicOpt.get().dataSource();
            List<MusicDTO> list = musicService.findAll().stream().peek(item -> {
                item.setMusicUri(music.getMusic(item.getMusicId()));
                item.setLyricUri(music.getLyric(item.getLyricId()));
                item.setCoverUri(music.getCover(item.getMusicId()));
            }).toList();
            return RestModel.ok(list);
        }
        return RestModel.ok(null);
    }

    @GetMapping("/reInitLyric")
    public ResponseEntity<RestModel<Void>> reInitLyric() {
        searchService.reInit();
        return RestModel.created();
    }
}
