package top.itning.yunshunas.music.datasource;

import top.itning.yunshunas.music.constant.MusicType;

import java.io.File;
import java.net.URI;

/**
 * 音乐数据源，支持管理音乐数据
 *
 * @author itning
 * @since 2022/1/12 11:13
 */
public interface MusicDataSource extends DataSource {
    /**
     * 新增音乐
     *
     * @param newMusicFile 音乐文件
     * @param musicType    音乐类型
     * @param musicId      音乐ID
     * @throws Exception 新增异常
     */
    void addMusic(File newMusicFile, MusicType musicType, String musicId) throws Exception;

    /**
     * 删除音乐
     *
     * @param musicId 音乐ID
     * @return 删除是否成功
     */
    boolean deleteMusic(String musicId);

    /**
     * 获取音乐
     *
     * @param musicId 音乐ID
     * @return 音乐URI
     */
    URI getMusic(String musicId);
}
