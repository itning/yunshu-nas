package top.itning.yunshunas.music.service;

import top.itning.yunshunas.music.entity.SearchResult;

import java.util.List;

/**
 * 搜索服务
 *
 * @author itning
 * @since 2022/11/2 15:39
 */
public interface SearchService {
    /**
     * 添加歌词信息
     *
     * @param musicId 音乐ID
     * @param lyricId 歌词ID
     * @param content 歌词
     */
    void saveOrUpdateLyric(String musicId, String lyricId, String content);

    /**
     * 删除歌词信息
     *
     * @param lyricId 歌词ID
     */
    void deleteLyric(String lyricId);

    /**
     * 搜索歌词
     *
     * @param keyword  关键字
     * @return 搜索结果
     */
    List<SearchResult> searchLyric(String keyword);

    /**
     * 重新初始化歌词es
     */
    void reInit();
}
