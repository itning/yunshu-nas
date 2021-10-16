package top.itning.yunshunas.music.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author itning
 * @since 2021/10/16 11:33
 */
public interface UploadService {
    void uploadMusic(MultipartFile file) throws Exception;

    void uploadLyric(String musicId, MultipartFile file) throws Exception;
}
