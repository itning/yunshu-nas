package top.itning.yunshunas.music.dto;

import lombok.Data;
import org.jaudiotagger.tag.datatype.Artwork;

import java.util.List;

/**
 * @author itning
 * @since 2021/6/1 16:35
 */
@Data
public class MusicMetaInfo {
    private String name;
    private String singer;
    private List<Artwork> coverPictures;
}
