package top.itning.yunshunas.music.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import top.itning.yunshunas.common.model.RestModel;
import top.itning.yunshunas.common.util.JsonUtils;
import top.itning.yunshunas.music.config.DataSourceConfig;
import top.itning.yunshunas.music.datasource.impl.TencentCosDataSource;
import top.itning.yunshunas.music.dto.MusicChangeDTO;
import top.itning.yunshunas.music.dto.MusicDTO;
import top.itning.yunshunas.music.dto.MusicManageDTO;
import top.itning.yunshunas.music.service.MusicManageService;
import top.itning.yunshunas.music.service.MusicService;
import top.itning.yunshunas.music.service.SearchService;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private final MusicService musicService;
    private final SearchService searchService;
    private final Map<String, DataSourceConfig.DataSourceWrapper> musicDataSourceMap;

    @Autowired
    public MusicManageApiController(MusicManageService musicManageService,
                                    MusicService musicService,
                                    SearchService searchService,
                                    Map<String, DataSourceConfig.DataSourceWrapper> musicDataSourceMap) {
        this.musicManageService = musicManageService;
        this.musicService = musicService;
        this.searchService = searchService;
        this.musicDataSourceMap = musicDataSourceMap;
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

    @GetMapping("/reInitLyric")
    public ResponseEntity<RestModel<Void>> reInitLyric() {
        searchService.reInit();
        return RestModel.created();
    }

    @GetMapping("/test")
    public ResponseEntity<RestModel<List<MusicDTO>>> test() throws JsonProcessingException, InterruptedException {
        Optional<DataSourceConfig.DataSourceWrapper> musicOpt = musicDataSourceMap.values().stream().filter(it -> it.dataSource() instanceof TencentCosDataSource).findFirst();
        if (musicOpt.isPresent()) {
            TencentCosDataSource music = (TencentCosDataSource) musicOpt.get().dataSource();
            List<MusicDTO> list = musicService.findAll().stream().peek(item -> {
                item.setMusicUri(music.getMusic(item.getMusicId()));
                item.setLyricUri(music.getLyric(item.getLyricId()));
                item.setCoverUri(music.getCover(item.getMusicId()));
                item.setMusicDownloadUri(music.getMusicDownloadURI(item.getMusicId()));
            }).toList();
            RestModel<List<MusicDTO>> up = new RestModel<>();
            up.setCode(200);
            up.setMsg("查询成功");
            up.setData(list);
            music.uploadMusicList(new ByteArrayInputStream(JsonUtils.OBJECT_MAPPER.writeValueAsBytes(up)));
            return RestModel.ok(list);
        }
        return RestModel.ok(null);
    }
}
