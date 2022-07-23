package top.itning.yunshunas.music.datasource.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.datasource.CoverDataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
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

    protected final NasProperties nasProperties;
    protected final NasProperties.FileDataSourceConfig fileDataSourceConfig;

    public FileDataSource(NasProperties nasProperties, String port) {
        this.nasProperties = nasProperties;
        this.fileDataSourceConfig = nasProperties.getFileDataSource();

        if (StringUtils.isBlank(fileDataSourceConfig.getUrlPrefix())) {
            fileDataSourceConfig.setUrlPrefix("http://127.0.0.1:" + port);
        }
        if (fileDataSourceConfig.getUrlPrefix().endsWith("/")) {
            fileDataSourceConfig.setUrlPrefix(fileDataSourceConfig.getUrlPrefix().substring(0, fileDataSourceConfig.getUrlPrefix().length() - 1));
        }
    }

    @Override
    public void addMusic(File newMusicFile, MusicType musicType, String musicId) throws Exception {
        File dest = new File(fileDataSourceConfig.getMusicFileDir() + File.separator + musicId);
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
        File dest = new File(fileDataSourceConfig.getMusicFileDir() + File.separator + musicId);
        if (!dest.exists() || !dest.isFile()) {
            return false;
        }
        return deleteFile(dest.toPath());
    }

    @Override
    public URI getMusic(String musicId) {
        return URI.create(fileDataSourceConfig.getUrlPrefix() + "/file?id=" + musicId);
    }

    @Override
    public void addLyric(InputStream lyricInputStream, long length, String lyricId) throws Exception {
        File lyricFile = new File(fileDataSourceConfig.getLyricFileDir() + File.separator + lyricId);
        if (lyricFile.exists()) {
            throw new IllegalArgumentException("歌词已经存在了");
        }
        Files.copy(lyricInputStream, Paths.get(fileDataSourceConfig.getLyricFileDir(), lyricId));
    }

    @Override
    public boolean deleteLyric(String lyricId) {
        File lyricFile = new File(fileDataSourceConfig.getLyricFileDir() + File.separator + lyricId);
        if (!lyricFile.exists() || !lyricFile.isFile()) {
            return false;
        }
        return deleteFile(lyricFile.toPath());
    }

    @Override
    public URI getLyric(String lyricId) {
        return URI.create(fileDataSourceConfig.getUrlPrefix() + "/file/lyric?id=" + lyricId);
    }

    @Override
    public void addCover(String musicId, String mimeType, byte[] binaryData) throws Exception {
        // do nothing
    }

    @Override
    public URI getCover(String musicId) {
        return URI.create(fileDataSourceConfig.getUrlPrefix() + "/file/cover?id=" + musicId);
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
