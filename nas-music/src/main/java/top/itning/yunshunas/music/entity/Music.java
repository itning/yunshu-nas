package top.itning.yunshunas.music.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.util.Date;

/**
 * @author itning
 * @date 2020/9/5 11:08
 */
@Data
@Entity
@Table(name = "music", indexes = {
        @Index(name = "index_music_id", columnList = "music_id")
})
public class Music {
    /**
     * 数据库主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    /**
     * 音乐ID
     */
    @Column(name = "music_id", nullable = false, unique = true)
    private String musicId;
    /**
     * 音乐名
     */
    @Column(name = "name", nullable = false)
    private String name;
    /**
     * 歌手
     */
    @Column(name = "singer", nullable = false)
    private String singer;
    /**
     * 歌词ID
     */
    @Column(name = "lyric_id")
    private String lyricId;
    /**
     * 音乐类型
     * {@link top.itning.yunshunas.music.constant.MusicType}
     */
    @Column(name = "type", nullable = false)
    private int type;
    /**
     * 创建时间
     */
    @Column(nullable = false)
    @CreationTimestamp
    private Date gmtCreate;
    /**
     * 更新时间
     */
    @Column(nullable = false)
    @UpdateTimestamp
    private Date gmtModified;
}
