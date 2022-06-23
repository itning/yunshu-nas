package top.itning.yunshunas.music.dto;

import lombok.Data;

import java.io.Serializable;
import java.net.URI;

/**
 * @author itning
 * @since 2020/9/5 11:08
 */
@Data
public class MusicDTO implements Serializable {

    private String musicId;

    private String name;

    private String singer;

    private String lyricId;

    private Integer type;

    private URI musicUri;

    private URI lyricUri;

    private URI coverUri;
}
