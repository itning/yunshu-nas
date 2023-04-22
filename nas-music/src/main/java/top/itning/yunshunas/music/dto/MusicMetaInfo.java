package top.itning.yunshunas.music.dto;

import lombok.Data;
import lombok.ToString;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.List;

/**
 * @author itning
 * @since 2021/6/1 16:35
 */
@Data
public class MusicMetaInfo {
    /**
     * 标题
     */
    private String title;
    /**
     * 艺术家
     */
    private List<String> artists;
    /**
     * 专辑
     */
    private String album;
    /**
     * 封面图
     */
    private List<CoverPicture> coverPictures;

    @Data
    public static class CoverPicture {
        private File file;
        @ToString.Exclude
        private String base64;
        @ToString.Exclude
        private byte[] binaryData;
        private String mimeType = "";
        private String description = "";
        private boolean isLinked = false;
        private String imageUrl = "";
        private int pictureType = -1;
    }

    public boolean needModify() {
        return StringUtils.isNotBlank(title) || StringUtils.isNotBlank(album) || !CollectionUtils.isEmpty(artists) || !CollectionUtils.isEmpty(coverPictures);
    }
}
