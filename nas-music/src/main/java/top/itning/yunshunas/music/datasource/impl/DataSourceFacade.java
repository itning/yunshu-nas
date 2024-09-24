package top.itning.yunshunas.music.datasource.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.itning.yunshunas.music.config.DataSourceConfig;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.datasource.CoverDataSource;
import top.itning.yunshunas.music.datasource.DataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Map;

/**
 * @author itning
 * @since 2023/4/2 18:58
 */
@Slf4j
@Service
public class DataSourceFacade implements MusicDataSource, CoverDataSource, LyricDataSource {

    private final Map<String, DataSourceConfig.DataSourceWrapper> musicDataSourceMap;
    private final Map<String, DataSourceConfig.DataSourceWrapper> lyricDataSourceMap;
    private final Map<String, DataSourceConfig.DataSourceWrapper> coverDataSourceMap;
    private final Map<Class<? extends DataSource>, DataSourceConfig.DataSourceWrapper> readDataSourceMap;

    @Autowired
    public DataSourceFacade(Map<String, DataSourceConfig.DataSourceWrapper> musicDataSourceMap,
                            Map<String, DataSourceConfig.DataSourceWrapper> lyricDataSourceMap,
                            Map<String, DataSourceConfig.DataSourceWrapper> coverDataSourceMap,
                            Map<Class<? extends DataSource>, DataSourceConfig.DataSourceWrapper> readDataSourceMap) {
        this.musicDataSourceMap = musicDataSourceMap;
        this.lyricDataSourceMap = lyricDataSourceMap;
        this.coverDataSourceMap = coverDataSourceMap;
        this.readDataSourceMap = readDataSourceMap;
    }

    @Override
    public void addCover(String musicId, String mimeType, byte[] binaryData) throws Exception {
        for (Map.Entry<String, DataSourceConfig.DataSourceWrapper> item : coverDataSourceMap.entrySet()) {
            String name = item.getKey();
            DataSourceConfig.DataSourceWrapper wrapper = item.getValue();
            if (!wrapper.config().isCanWrite()) {
                continue;
            }
            log.info("addCover handler data source {}", name);
            CoverDataSource dataSource = (CoverDataSource) wrapper.dataSource();
            dataSource.addCover(musicId, mimeType, binaryData);
        }
    }

    @Override
    public URI getCover(String musicId) {
        return ((CoverDataSource) readDataSourceMap.get(CoverDataSource.class).dataSource()).getCover(musicId);
    }

    @Override
    public boolean deleteCover(String musicId) {
        boolean success = true;
        for (Map.Entry<String, DataSourceConfig.DataSourceWrapper> item : coverDataSourceMap.entrySet()) {
            String name = item.getKey();
            DataSourceConfig.DataSourceWrapper wrapper = item.getValue();
            if (!wrapper.config().isCanWrite()) {
                continue;
            }
            log.info("deleteCover handler data source {}", name);
            CoverDataSource dataSource = (CoverDataSource) wrapper.dataSource();
            boolean itemSuccess = dataSource.deleteCover(musicId);
            if (!itemSuccess) {
                success = false;
            }
        }
        return success;
    }

    @Override
    public void addLyric(InputStream lyricInputStream, long length, String lyricId) throws Exception {
        byte[] allBytes = lyricInputStream.readAllBytes();
        for (Map.Entry<String, DataSourceConfig.DataSourceWrapper> item : lyricDataSourceMap.entrySet()) {
            String name = item.getKey();
            DataSourceConfig.DataSourceWrapper wrapper = item.getValue();
            if (!wrapper.config().isCanWrite()) {
                continue;
            }
            log.info("addLyric handler data source {}", name);
            LyricDataSource dataSource = (LyricDataSource) wrapper.dataSource();
            dataSource.addLyric(new ByteArrayInputStream(allBytes), length, lyricId);
        }
    }

    @Override
    public boolean deleteLyric(String lyricId) {
        boolean success = true;
        for (Map.Entry<String, DataSourceConfig.DataSourceWrapper> item : lyricDataSourceMap.entrySet()) {
            String name = item.getKey();
            DataSourceConfig.DataSourceWrapper wrapper = item.getValue();
            if (!wrapper.config().isCanWrite()) {
                continue;
            }
            log.info("deleteLyric handler data source {}", name);
            LyricDataSource dataSource = (LyricDataSource) wrapper.dataSource();
            boolean itemSuccess = dataSource.deleteLyric(lyricId);
            if (!itemSuccess) {
                success = false;
            }
        }
        return success;
    }

    @Override
    public URI getLyric(String lyricId) {
        return ((LyricDataSource) readDataSourceMap.get(LyricDataSource.class).dataSource()).getLyric(lyricId);
    }

    @Override
    public void addMusic(File newMusicFile, MusicType musicType, String musicId) throws Exception {
        try {
            for (Map.Entry<String, DataSourceConfig.DataSourceWrapper> item : musicDataSourceMap.entrySet()) {
                String name = item.getKey();
                DataSourceConfig.DataSourceWrapper wrapper = item.getValue();
                if (!wrapper.config().isCanWrite()) {
                    continue;
                }
                log.info("addMusic handler data source {}", name);
                MusicDataSource dataSource = (MusicDataSource) wrapper.dataSource();
                dataSource.addMusic(newMusicFile, musicType, musicId);
            }
        } finally {
            log.info("拷贝完成，删除临时文件，结果：{}", deleteFile(newMusicFile.toPath()));
        }
    }

    @Override
    public boolean deleteMusic(String musicId) {
        boolean success = true;
        for (Map.Entry<String, DataSourceConfig.DataSourceWrapper> item : musicDataSourceMap.entrySet()) {
            String name = item.getKey();
            DataSourceConfig.DataSourceWrapper wrapper = item.getValue();
            if (!wrapper.config().isCanWrite()) {
                continue;
            }
            log.info("deleteMusic handler data source {}", name);
            MusicDataSource dataSource = (MusicDataSource) wrapper.dataSource();
            boolean itemSuccess = dataSource.deleteMusic(musicId);
            if (!itemSuccess) {
                success = false;
            }
        }
        return success;
    }

    @Override
    public URI getMusic(String musicId) {
        return ((MusicDataSource) readDataSourceMap.get(MusicDataSource.class).dataSource()).getMusic(musicId);
    }

    @Override
    public File getMusicFile(String musicId) throws Exception {
        return ((MusicDataSource) readDataSourceMap.get(MusicDataSource.class).dataSource()).getMusicFile(musicId);
    }

    @Override
    public long getFileSize(String musicId) {
        return ((MusicDataSource) readDataSourceMap.get(MusicDataSource.class).dataSource()).getFileSize(musicId);
    }

    private boolean deleteFile(Path path) {
        boolean success = false;
        try {
            Files.delete(path);
            success = true;
        } catch (NoSuchFileException e) {
            log.warn("文件不存在，无需删除：{}", path, e);
        } catch (Exception e) {
            log.error("删除文件失败，文件路径：{}", path, e);
        }
        return success;
    }
}
