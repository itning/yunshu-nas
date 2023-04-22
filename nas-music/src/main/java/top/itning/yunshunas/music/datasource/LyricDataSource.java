package top.itning.yunshunas.music.datasource;

import java.io.InputStream;
import java.net.URI;

/**
 * 歌词数据源，提供对歌词数据的管理
 *
 * @author itning
 * @since 2022/1/12 11:39
 */
public interface LyricDataSource extends DataSource {

    /**
     * 添加歌词
     *
     * @param lyricInputStream 歌词内容
     * @param length           文件大小
     * @param lyricId          歌词ID
     * @throws Exception 添加异常
     */
    void addLyric(InputStream lyricInputStream, long length, String lyricId) throws Exception;

    /**
     * 删除歌词
     *
     * @param lyricId 歌词ID
     * @return 删除是否成功
     */
    boolean deleteLyric(String lyricId);

    /**
     * 获取歌词URI
     *
     * @param lyricId 歌词ID
     * @return 歌词URI
     */
    URI getLyric(String lyricId);
}
