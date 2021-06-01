package top.itning.yunshunas.music.dto;

import lombok.Data;

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
        private String base64;
        private byte[] binaryData;
        private String mimeType = "";
        private String description = "";
        private boolean isLinked = false;
        private String imageUrl = "";
        private int pictureType = -1;
    }
}
