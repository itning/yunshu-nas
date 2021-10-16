package top.itning.yunshunas.music.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author itning
 * @since 2021/10/16 11:33
 */
public interface UploadService {
    /**
     * 上传音乐
     *
     * @param file 文件
     * @throws Exception 上传出错
     */
    void uploadMusic(MultipartFile file) throws Exception;

    /**
     * 上传歌词
     *
     * @param musicId 歌词对应的歌曲ID
     * @param file    文件
     * @throws Exception 上传出错
     */
    void uploadLyric(String musicId, MultipartFile file) throws Exception;
}
