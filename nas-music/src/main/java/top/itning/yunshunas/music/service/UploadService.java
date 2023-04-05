package top.itning.yunshunas.music.service;

import org.springframework.web.multipart.MultipartFile;
import top.itning.yunshunas.music.dto.MusicDTO;
import top.itning.yunshunas.music.dto.MusicMetaInfo;

/**
 * @author itning
 * @since 2021/10/16 11:33
 */
public interface UploadService {
    /**
     * 上传音乐
     *
     * @param file          文件
     * @param musicMetaInfo 音乐元数据信息
     * @return 上传的音乐信息
     * @throws Exception 上传出错
     */
    MusicDTO uploadMusic(MultipartFile file, MusicMetaInfo musicMetaInfo) throws Exception;

    /**
     * 修改音乐
     *
     * @param musicId       音乐ID
     * @param file          文件
     * @param musicMetaInfo 音乐元数据信息
     * @return 修改的音乐信息
     * @throws Exception 修改出错
     */
    MusicDTO editMusic(String musicId, MultipartFile file, MusicMetaInfo musicMetaInfo) throws Exception;

    /**
     * 修改音乐元数据信息
     *
     * @param musicId       音乐ID
     * @param musicMetaInfo 音乐元数据信息
     * @throws Exception 修改出错
     */
    void editMetaInfo(String musicId, MusicMetaInfo musicMetaInfo) throws Exception;

    /**
     * 上传歌词
     *
     * @param musicId 歌词对应的歌曲ID
     * @param file    文件
     * @return 歌词内容
     * @throws Exception 上传出错
     */
    byte[] uploadLyric(String musicId, MultipartFile file) throws Exception;

    /**
     * 修改歌词
     *
     * @param musicId 歌词对应的歌曲ID
     * @param file    文件
     * @return 歌词内容
     * @throws Exception 修改出错
     */
    byte[] editLyric(String musicId, MultipartFile file) throws Exception;
}
