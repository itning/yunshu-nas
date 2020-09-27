package top.itning.yunshu.yunshunas.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @author itning
 * @date 2020/9/5 11:08
 */
@Data
public class MusicDTO implements Serializable {

    private String musicId;

    private String name;

    private String singer;

    private String lyricId;

    private Integer type;
}
