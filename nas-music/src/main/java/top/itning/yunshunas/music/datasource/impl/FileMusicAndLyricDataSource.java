package top.itning.yunshunas.music.datasource.impl;

import lombok.extern.slf4j.Slf4j;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 文件系统歌曲与歌词数据源
 *
 * @author itning
 * @since 2022/1/12 11:16
 */
@Slf4j
public class FileMusicAndLyricDataSource implements MusicDataSource, LyricDataSource {
    private final NasProperties nasProperties;

    public FileMusicAndLyricDataSource(NasProperties nasProperties) {
        this.nasProperties = nasProperties;
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
            log.info("拷贝完成，删除临时文件，结果：{}", newMusicFile.delete());
        }
    }

    @Override
    public boolean deleteMusic(String musicId) {
        File dest = new File(nasProperties.getMusicFileDir() + File.separator + musicId);
        if (!dest.exists() || !dest.isFile()) {
            return false;
        }
        return dest.delete();
    }

    @Override
    public URI getMusic(String musicId) {
        return URI.create("https://127.0.0.1/file?id=" + musicId);
    }

    @Override
    public void addLyric(InputStream lyricInputStream, String lyricId) throws Exception {
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
        return lyricFile.delete();
    }

    @Override
    public URI getLyric(String lyricId) {
        return URI.create("https://127.0.0.1/file/lyric?id=" + lyricId);
    }
}
