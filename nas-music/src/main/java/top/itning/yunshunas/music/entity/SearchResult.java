package top.itning.yunshunas.music.entity;

import lombok.Data;

import java.net.URI;
import java.util.List;

/**
 * @author itning
 * @since 2022/11/12 19:36
 */
@Data
public class SearchResult {
    private String musicId;

    private String name;

    private String singer;

    private String lyricId;

    private Integer type;

    private URI musicUri;

    private URI lyricUri;

    private URI coverUri;

    private List<String> highlightFields;
}
