package top.itning.yunshunas.music.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * 歌词
 *
 * @author itning
 * @since 2022/11/2 15:32
 */
@Data
@Document(indexName = "yunshu_music_lyric")
public class Lyric {

    /**
     * 音乐ID
     */
    @Id
    @Field(type = FieldType.Text)
    private String musicId;

    /**
     * 歌词ID
     */
    @Id
    @Field(type = FieldType.Text)
    private String lyricId;

    /**
     * 歌词
     */
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String content;
}
