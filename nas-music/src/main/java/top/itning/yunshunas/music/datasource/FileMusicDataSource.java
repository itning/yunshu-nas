package top.itning.yunshunas.music.datasource;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import top.itning.yunshunas.common.config.NasProperties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URI;
import java.nio.channels.FileChannel;

/**
 * 文件系统歌曲数据源
 *
 * @author itning
 * @since 2022/1/12 11:16
 */
@Slf4j
@Component
public class FileMusicDataSource implements MusicDataSource {
    private final NasProperties nasProperties;

    @Autowired
    public FileMusicDataSource(NasProperties nasProperties) {
        this.nasProperties = nasProperties;
    }

    @Override
    public void add(File newMusicFile, String musicId) throws Exception {
        File dest = new File(nasProperties.getMusicFileDir() + File.separator + musicId);
        try (FileInputStream in = new FileInputStream(newMusicFile);
             FileChannel sourceChannel = in.getChannel();
             FileOutputStream out = new FileOutputStream(dest);
             FileChannel destChannel = out.getChannel()) {
            log.info("拷贝文件从{}到{}", newMusicFile.getPath(), dest.getPath());
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally {
            log.info("拷贝完成，删除临时文件，结果：{}", newMusicFile.delete());
        }
    }

    @Override
    public boolean delete(String musicId) {
        File dest = new File(nasProperties.getMusicFileDir() + File.separator + musicId);
        if (!dest.exists() && dest.isFile()) {
            return false;
        }
        return dest.delete();
    }

    @Override
    public URI get(String musicId) {
        return URI.create("https://127.0.0.1/file?id=" + musicId);
    }
}
