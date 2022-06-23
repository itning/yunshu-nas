package top.itning.yunshunas.video.repository;

/**
 * @author itning
 * @since 2019/7/15 11:25
 */
public interface IVideoRepository {
    /**
     * 获取文件路径MD5
     *
     * @param location 文件路径
     * @return MD5
     */
    String getLocationMd5(String location);

    /**
     * 获取文件写入目录
     *
     * @param location 文件路径
     * @return 写入目录
     */
    String getWriteDir(String location);

    /**
     * 获取M3U8文件路径
     *
     * @param name 文件名（无扩展名）
     * @return 文件路径
     */
    String readM3U8File(String name);

    /**
     * 获取TS文件路径
     *
     * @param name 文件名（无扩展名）
     * @return 文件路径
     */
    String readTsFile(String name);
}
