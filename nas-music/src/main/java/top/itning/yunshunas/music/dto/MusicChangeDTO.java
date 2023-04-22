package top.itning.yunshunas.music.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author itning
 * @since 2022/7/23 17:25
 */
@Data
public class MusicChangeDTO {
    /**
     * 音乐文件
     */
    private MultipartFile musicFile;

    /**
     * 歌词文件
     */
    private MultipartFile lyricFile;

    /**
     * 封面文件
     */
    private MultipartFile coverFile;

    /**
     * 音乐名
     */
    private String name;

    /**
     * 音乐歌手
     */
    private String singer;

    /**
     * 音乐ID
     */
    private String musicId;
}
