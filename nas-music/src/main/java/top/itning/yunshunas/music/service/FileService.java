package top.itning.yunshunas.music.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.dto.MusicMetaInfo;

import java.io.IOException;

/**
 * @author itning
 * @since 2020/9/5 12:29
 */
public interface FileService {
    /**
     * 获取音乐
     *
     * @param id       音乐ID
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws Exception 失败
     */
    void getOneMusic(String id, HttpServletRequest request, HttpServletResponse response) throws Exception;


    void getOneMusic(String musicName, String singer, MusicType type, HttpServletRequest request, HttpServletResponse response) throws Exception;

    /**
     * 获取歌词
     *
     * @param id 歌词ID
     * @return 歌词
     * @throws IOException 失败
     */
    String getLyric(String id) throws IOException;

    /**
     * 获取音乐元信息
     *
     * @param id 音乐ID
     * @return 音乐元信息
     */
    MusicMetaInfo getMusicMetaInfo(String id);

    /**
     * 获取第一张封面，如果有的话
     *
     * @param id       音乐ID
     * @param range    请求头Range
     * @param request  HttpServletRequest
     * @param response HttpServletResponse
     * @throws Exception 失败
     */
    void getMusicCover(String id, String range, HttpServletRequest request, HttpServletResponse response) throws Exception;
}
