package top.itning.yunshunas.music.datasource.impl;

import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.datasource.CoverDataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;

/**
 * 混合数据源
 * <p>
 * 双写：歌词，封面到{@link TencentCosDataSource}和{@link FileDataSource}
 * <p>
 * 只写：歌曲数据到{@link FileDataSource}
 *
 * @author itning
 * @since 2022/1/21 10:53
 */
public class MixedDataSource implements MusicDataSource, LyricDataSource, CoverDataSource {

    private final FileDataSource fileDataSource;
    private final TencentCosDataSource tencentCosDataSource;

    public MixedDataSource(NasProperties nasProperties) {
        this.fileDataSource = new FileDataSource(nasProperties);
        this.tencentCosDataSource = new TencentCosDataSource(nasProperties);
    }

    @PostConstruct
    public void init() {
        this.fileDataSource.init();
        this.tencentCosDataSource.init();
    }

    @Override
    public void addCover(String musicId, String mimeType, byte[] binaryData) throws Exception {
        try {
            fileDataSource.addCover(musicId, mimeType, binaryData);
            tencentCosDataSource.addCover(musicId, mimeType, binaryData);
        } catch (Exception e) {
            this.deleteCover(musicId);
            throw e;
        }
    }

    @Override
    public URI getCover(String musicId) {
        return tencentCosDataSource.getCover(musicId);
    }

    @Override
    public boolean deleteCover(String musicId) {
        boolean a = fileDataSource.deleteCover(musicId);
        boolean b = tencentCosDataSource.deleteCover(musicId);
        return a && b;
    }

    @Override
    public void addLyric(InputStream lyricInputStream, long length, String lyricId) throws Exception {
        try {
            byte[] allBytes = lyricInputStream.readAllBytes();
            fileDataSource.addLyric(new ByteArrayInputStream(allBytes), length, lyricId);
            tencentCosDataSource.addLyric(new ByteArrayInputStream(allBytes), length, lyricId);
        } catch (Exception e) {
            this.deleteLyric(lyricId);
            throw e;
        }
    }

    @Override
    public boolean deleteLyric(String lyricId) {
        boolean a = fileDataSource.deleteLyric(lyricId);
        boolean b = tencentCosDataSource.deleteLyric(lyricId);
        return a && b;
    }

    @Override
    public URI getLyric(String lyricId) {
        return tencentCosDataSource.getLyric(lyricId);
    }

    @Override
    public void addMusic(File newMusicFile, MusicType musicType, String musicId) throws Exception {
        fileDataSource.addMusic(newMusicFile, musicType, musicId);
    }

    @Override
    public boolean deleteMusic(String musicId) {
        return fileDataSource.deleteMusic(musicId);
    }

    @Override
    public URI getMusic(String musicId) {
        return fileDataSource.getMusic(musicId);
    }
}
