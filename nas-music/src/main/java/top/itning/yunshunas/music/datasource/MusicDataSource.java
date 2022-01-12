package top.itning.yunshunas.music.datasource;

import java.io.File;
import java.net.URI;

/**
 * 音乐数据源，支持管理音乐数据
 *
 * @author itning
 * @since 2022/1/12 11:13
 */
public interface MusicDataSource {
    /**
     * 新增音乐
     *
     * @param newMusicFile 音乐文件
     * @param musicId      音乐ID
     * @throws Exception 新增异常
     */
    void add(File newMusicFile, String musicId) throws Exception;

    /**
     * 删除音乐
     *
     * @param musicId 音乐ID
     * @return 删除是否成功
     */
    boolean delete(String musicId);

    /**
     * 获取音乐
     *
     * @param musicId 音乐ID
     * @return 音乐URI
     */
    URI get(String musicId);
}
