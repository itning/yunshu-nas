package top.itning.yunshunas.music.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.Optional;

/**
 * @author itning
 * @since 2020/9/5 12:08
 */
@Getter
@AllArgsConstructor
public enum MusicType {

    /**
     * FLAC
     */
    FLAC(1, "audio/flac", "flac"),
    /**
     * MP3
     */
    MP3(2, "audio/mpeg", "mp3"),
    /**
     * WAV
     */
    WAV(3, "audio/wav", "wav"),
    /**
     * AAC
     */
    AAC(4, "audio/aac", "aac");

    private final int type;
    private final String mediaType;
    private final String ext;

    public static Optional<String> getMediaType(Integer type) {
        return getMediaTypeEnum(type).map(MusicType::getMediaType);
    }

    public static Optional<MusicType> getMediaTypeEnum(Integer type) {
        if (Objects.isNull(type)) {
            return Optional.empty();
        }
        for (MusicType musicType : MusicType.values()) {
            if (musicType.getType() == type) {
                return Optional.of(musicType);
            }
        }
        return Optional.empty();
    }

    public static Optional<MusicType> getFromExt(String ext) {
        if (null == ext) {
            return Optional.empty();
        }
        for (MusicType musicType : MusicType.values()) {
            if (musicType.getExt().equals(ext)) {
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
