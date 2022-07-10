package top.itning.yunshunas.music.datasource.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.datasource.CoverDataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件系统歌曲与歌词数据源
 *
 * @author itning
 * @since 2022/1/12 11:16
 */
@Slf4j
public class FileDataSource implements MusicDataSource, LyricDataSource, CoverDataSource {

    @Value("${server.port}")
    private String port;

    private final NasProperties nasProperties;

    public FileDataSource(NasProperties nasProperties) {
        this.nasProperties = nasProperties;
    }

    @PostConstruct
    public void init() {
        if (StringUtils.isBlank(nasProperties.getFileDataSourceUrlPrefix())) {
            nasProperties.setFileDataSourceUrlPrefix("http://127.0.0.1:" + port);
        }
    }

    @Override
    public void addMusic(File newMusicFile, MusicType musicType, String musicId) throws Exception {
        File dest = new File(nasProperties.getMusicFileDir() + File.separator + musicId);
        try (FileInputStream in = new FileInputStream(newMusicFile);
             FileChannel sourceChannel = in.getChannel();
             FileOutputStream out = new FileOutputStream(dest);
             FileChannel destChannel = out.getChannel()) {
            log.info("拷贝文件从{}到{}", newMusicFile.getPath(), dest.getPath());
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally {
            log.info("拷贝完成，删除临时文件，结果：{}", deleteFile(newMusicFile.toPath()));
        }
    }

    @Override
    public boolean deleteMusic(String musicId) {
        File dest = new File(nasProperties.getMusicFileDir() + File.separator + musicId);
        if (!dest.exists() || !dest.isFile()) {
            return false;
        }
        return deleteFile(dest.toPath());
    }

    @Override
    public URI getMusic(String musicId) {
        return URI.create(nasProperties.getFileDataSourceUrlPrefix() + "/file?id=" + musicId);
    }

    @Override
    public void addLyric(InputStream lyricInputStream, long length, String lyricId) throws Exception {
        File lyricFile = new File(nasProperties.getLyricFileDir() + File.separator + lyricId);
        if (lyricFile.exists()) {
            throw new IllegalArgumentException("歌词已经存在了");
        }
        Files.copy(lyricInputStream, Paths.get(nasProperties.getLyricFileDir(), lyricId));
    }

    @Override
    public boolean deleteLyric(String lyricId) {
        File lyricFile = new File(nasProperties.getLyricFileDir() + File.separator + lyricId);
        if (!lyricFile.exists() || !lyricFile.isFile()) {
            return false;
        }
        return deleteFile(lyricFile.toPath());
    }

    @Override
    public URI getLyric(String lyricId) {
        return URI.create(nasProperties.getFileDataSourceUrlPrefix() + "/file/lyric?id=" + lyricId);
    }

    @Override
    public void addCover(String musicId, String mimeType, byte[] binaryData) throws Exception {
        // do nothing
    }

    @Override
    public URI getCover(String musicId) {
        return URI.create(nasProperties.getFileDataSourceUrlPrefix() + "/file/cover?id=" + musicId);
    }

    @Override
    public boolean deleteCover(String musicId) {
        return true;
    }

    private boolean deleteFile(Path path) {
        boolean success = false;
        try {
            Files.delete(path);
            success = true;
        } catch (Exception e) {
            log.error("删除文件失败，文件路径：{}", path, e);
        }
        return success;
    }
}
