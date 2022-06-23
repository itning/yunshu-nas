package top.itning.yunshunas.video.entity;

import lombok.Data;

/**
 * @author itning
 * @since 2019/7/15 11:51
 */
@Data
public class FileEntity {
    /**
     * 名
     */
    private String name;
    /**
     * 大小
     */
    private String size;
    /**
     * 是文件
     */
    private boolean file;
    /**
     * 能够在线播放
     */
    private boolean canPlay;
    /**
     * 地址
     */
    private String location;
}
