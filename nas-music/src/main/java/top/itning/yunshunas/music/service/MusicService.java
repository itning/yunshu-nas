package top.itning.yunshunas.music.service;

import top.itning.yunshunas.music.dto.MusicDTO;

import java.util.List;


/**
 * @author itning
 * @since 2020/9/5 11:25
 */
public interface MusicService {
    /**
     * 分页查找全部
     *
     * @return 音乐信息
     */
    List<MusicDTO> findAll();

    /**
     * 模糊搜索：搜索音乐名和歌手名，只要包含关键字就返回
     *
     * @param keyword  关键字
     * @return 音乐信息
     */
    List<MusicDTO> fuzzySearch(String keyword);

    /**
     * 模糊搜索：搜索音乐名，只要包含关键字就返回
     *
     * @param keyword  关键字
     * @return 音乐信息
     */
    List<MusicDTO> fuzzySearchName(String keyword);

    /**
     * 模糊搜索：搜索歌手名，只要包含关键字就返回
     *
     * @param keyword  关键字
     * @return 音乐信息
     */
    List<MusicDTO> fuzzySearchSinger(String keyword);
}
