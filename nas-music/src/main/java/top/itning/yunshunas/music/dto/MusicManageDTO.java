package top.itning.yunshunas.music.dto;

import lombok.Data;

import java.io.Serializable;
import java.net.URI;
import java.util.Date;

/**
 * @author itning
 * @since 2022/7/10 14:36
 */
@Data
public class MusicManageDTO implements Serializable {
    private String musicId;

    private String name;

    private String singer;

    private String lyricId;

    private Integer type;

    private URI musicUri;

    private URI lyricUri;

    private URI coverUri;

    private Date gmtCreate;

    private Date gmtModified;
}
