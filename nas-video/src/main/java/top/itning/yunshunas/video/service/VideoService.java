package top.itning.yunshunas.video.service;


import top.itning.yunshunas.video.entity.FileEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * @author itning
 * @since 2019/7/16 14:12
 */
public interface VideoService {
    /**
     * 获取M3U8文件
     *
     * @param name         文件名
     * @param outputStream OutputStream
     * @throws IOException IOException
     */
    void getM3u8File(String name, OutputStream outputStream) throws IOException;

    /**
     * 获取TS文件
     *
     * @param name         文件名
     * @param outputStream OutputStream
     * @throws IOException IOException
     */
    void getTsFile(String name, OutputStream outputStream) throws IOException;

    /**
     * 获取文件列表
     *
     * @param location 路径
     * @return 文件列表
     */
    List<FileEntity> getFileEntities(String location);
}
