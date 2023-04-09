package top.itning.yunshunas.music.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author itning
 * @since 2020/9/5 11:08
 */
@Data
public class Music {
    /**
     * 数据库主键ID
     */
    private long id;
    /**
     * 音乐ID
     */
    private String musicId;
    /**
     * 音乐名
     */
    private String name;
    /**
     * 歌手
     */
    private String singer;
    /**
     * 歌词ID
     */
    private String lyricId;
    /**
     * 音乐类型
     * {@link top.itning.yunshunas.music.constant.MusicType}
     */
    private Integer type;
    /**
     * 创建时间
     */
    private Date gmtCreate;
    /**
     * 更新时间
     */
    private Date gmtModified;

}
