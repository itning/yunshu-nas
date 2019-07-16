package top.itning.yunshu.yunshunas.service;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author itning
 * @date 2019/7/16 14:12
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
}
