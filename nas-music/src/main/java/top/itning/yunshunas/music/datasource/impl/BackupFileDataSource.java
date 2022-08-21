package top.itning.yunshunas.music.datasource.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.music.constant.MusicType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * 文件系统歌曲与歌词数据源
 * 写时支持写两份
 *
 * @author itning
 * @since 2022/7/23 15:17
 */
@Slf4j
public class BackupFileDataSource extends FileDataSource {

    private final NasProperties.BackupFileDataSourceConfig backupFileDataSourceConfig;

    public BackupFileDataSource(NasProperties nasProperties, String port) {
        super(nasProperties, port);
        this.backupFileDataSourceConfig = nasProperties.getBackupFileDataSource();

        if (StringUtils.isAnyBlank(backupFileDataSourceConfig.getMusicFileDir(), backupFileDataSourceConfig.getLyricFileDir())) {
            throw new IllegalArgumentException("Backup MusicFileDir or LyricFileDir is null");
        }
    }

    @Override
    public void addMusic(File newMusicFile, MusicType musicType, String musicId) throws Exception {
        super.addMusic(newMusicFile, musicType, musicId);
        File sourceFile = new File(fileDataSourceConfig.getMusicFileDir() + File.separator + musicId);
        File targetFile = new File(backupFileDataSourceConfig.getMusicFileDir() + File.separator + musicId);
        try (FileInputStream in = new FileInputStream(sourceFile);
             FileChannel sourceChannel = in.getChannel();
             FileOutputStream out = new FileOutputStream(targetFile);
             FileChannel destChannel = out.getChannel()) {
            log.info("拷贝备份文件从{}到{}", newMusicFile.getPath(), targetFile.getPath());
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally {
            log.info("拷贝备份完成");
        }
    }

    @Override
    public void addLyric(InputStream lyricInputStream, long length, String lyricId) throws Exception {
        super.addLyric(lyricInputStream, length, lyricId);
        Files.copy(Paths.get(fileDataSourceConfig.getLyricFileDir() + File.separator + lyricId), Paths.get(backupFileDataSourceConfig.getLyricFileDir(), lyricId));
    }

    @Override
    public boolean deleteMusic(String musicId) {
        File dest = new File(backupFileDataSourceConfig.getMusicFileDir() + File.separator + musicId);
        if (!dest.exists() || !dest.isFile()) {
            return false;
        }
        deleteFile(dest.toPath());
        return super.deleteMusic(musicId);
    }

    @Override
    public boolean deleteLyric(String lyricId) {
        File lyricFile = new File(backupFileDataSourceConfig.getLyricFileDir() + File.separator + lyricId);
        if (!lyricFile.exists() || !lyricFile.isFile()) {
            return false;
        }
        deleteFile(lyricFile.toPath());
        return super.deleteLyric(lyricId);
    }
}