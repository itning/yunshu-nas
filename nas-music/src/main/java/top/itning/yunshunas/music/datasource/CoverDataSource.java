package top.itning.yunshunas.music.datasource;

import java.net.URI;

/**
 * 封面数据源
 *
 * @author itning
 * @since 2022/1/12 14:57
 */
public interface CoverDataSource extends DataSource {

    /**
     * 添加封面
     *
     * @param musicId    音乐ID
     * @param mimeType   MIME类型
     * @param binaryData 元数据
     * @throws Exception 添加异常
     */
    void addCover(String musicId, String mimeType, byte[] binaryData) throws Exception;

    /**
     * 获取封面URI
     *
     * @param musicId 音乐ID
     * @return 封面URI
     */
    URI getCover(String musicId);

    /**
     * 删除元数据
     *
     * @param musicId 音乐ID
     * @return 删除是否成功
     */
    boolean deleteCover(String musicId);
}
