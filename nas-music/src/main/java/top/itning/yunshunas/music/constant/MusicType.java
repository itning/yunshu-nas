package top.itning.yunshunas.music.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * @author itning
 * @date 2020/9/5 12:08
 */
@Getter
@AllArgsConstructor
public enum MusicType {

    /**
     * FLAC
     */
    FLAC(1, "audio/flac"),
    /**
     * MP3
     */
    MP3(2, "audio/mpeg"),
    /**
     * WAV
     */
    WAV(3, "audio/wav"),
    /**
     * AAC
     */
    AAC(4, "audio/aac");

    private final int type;
    private final String mediaType;

    public static Optional<String> getMediaType(int type) {
        return getMediaTypeEnum(type).map(MusicType::getMediaType);
    }

    public static Optional<MusicType> getMediaTypeEnum(int type) {
        for (MusicType musicType : MusicType.values()) {
            if (musicType.getType() == type) {
                return Optional.of(musicType);
            }
        }
        return Optional.empty();
    }

    /**
     * 根据文件名获取类型
     *
     * @param path 文件名
     * @return 如果不支持文件类型返回<code>Optional.empty()</code>
     */
    public static Optional<MusicType> getMusicTypeFromFilePath(String path) {
        String filenameExtension = StringUtils.getFilenameExtension(path);
        if (null == filenameExtension) {
            return Optional.empty();
        }
        for (MusicType musicType : MusicType.values()) {
            if (musicType.name().equals(filenameExtension.toUpperCase())) {
                return Optional.of(musicType);
            }
        }
        return Optional.empty();
    }
}
