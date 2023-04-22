package top.itning.yunshunas.music.service;

import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.dto.MusicMetaInfo;

import java.io.File;

/**
 * @author itning
 * @since 2021/6/1 16:33
 */
public interface MusicMetaInfoService {
    /**
     * 音乐元信息
     *
     * @param musicFile 音乐文件
     * @param musicType 类型
     * @return 音乐元信息
     * @throws Exception 解析出现错误
     */
    MusicMetaInfo metaInfo(File musicFile, MusicType musicType) throws Exception;

    /**
     * 修改音乐元信息
     *
     * @param musicFile     音乐文件
     * @param musicType     类型
     * @param musicMetaInfo 音乐元信息
     * @throws Exception 修改出现错误
     */
    void editMetaInfo(File musicFile, MusicType musicType, MusicMetaInfo musicMetaInfo) throws Exception;
}
