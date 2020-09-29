package top.itning.yunshunas.music.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import top.itning.yunshunas.music.dto.MusicDTO;


/**
 * @author itning
 * @date 2020/9/5 11:25
 */
public interface MusicService {
    /**
     * 分页查找全部
     *
     * @param pageable 分页
     * @return 音乐信息
     */
    Page<MusicDTO> findAll(Pageable pageable);

    /**
     * 模糊搜索：搜索音乐名和歌手名，只要包含关键字就返回
     *
     * @param keyword  关键字
     * @param pageable 分页
     * @return 音乐信息
     */
    Page<MusicDTO> fuzzySearch(String keyword, Pageable pageable);

    /**
     * 模糊搜索：搜索音乐名，只要包含关键字就返回
     *
     * @param keyword  关键字
     * @param pageable 分页
     * @return 音乐信息
     */
    Page<MusicDTO> fuzzySearchName(String keyword, Pageable pageable);

    /**
     * 模糊搜索：搜索歌手名，只要包含关键字就返回
     *
     * @param keyword  关键字
     * @param pageable 分页
     * @return 音乐信息
     */
    Page<MusicDTO> fuzzySearchSinger(String keyword, Pageable pageable);
}
