package top.itning.yunshunas.music.entity;

import lombok.Data;

/**
 * 歌词
 *
 * @author itning
 * @since 2022/11/2 15:32
 */
@Data
public class Lyric {

    /**
     * 歌词ID
     */
    private String lyricId;

    /**
     * 音乐ID
     */
    private String musicId;

    /**
     * 歌词
     */
    private String content;
}
