package top.itning.yunshunas.music.datasource.impl;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.music.config.NasMusicProperties;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.datasource.CoverDataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;

import java.io.*;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.Optional;

/**
 * 文件系统歌曲与歌词数据源
 *
 * @author itning
 * @since 2022/1/12 11:16
 */
public class FileDataSource implements MusicDataSource, LyricDataSource, CoverDataSource {
    private final Logger log;
    private final NasMusicProperties.MusicDataSourceConfig musicDataSourceConfig;
    private final NasProperties nasProperties;

    public FileDataSource(NasMusicProperties.MusicDataSourceConfig musicDataSourceConfig,
                          NasProperties nasProperties) {
        this.musicDataSourceConfig = musicDataSourceConfig;
        this.nasProperties = nasProperties;
        this.log = LoggerFactory.getLogger(FileDataSource.class.getName() + "(" + musicDataSourceConfig.getName() + ")");

        if (StringUtils.isBlank(musicDataSourceConfig.getMusicFileDir())) {
            log.warn("datasource config music file dir is blank");
        }

        if (StringUtils.isBlank(musicDataSourceConfig.getLyricFileDir())) {
            log.warn("datasource config lyric file dir is blank");
        }
    }

    @Override
    public void addMusic(File newMusicFile, MusicType musicType, String musicId) throws Exception {
        if (StringUtils.isBlank(musicDataSourceConfig.getMusicFileDir())) {
            log.info("歌曲目录未配置，跳过上传歌曲");
            return;
        }

        boolean change2mp3 = musicDataSourceConfig.isConvertAudioToMp3BeforeUploading() && musicType != MusicType.MP3;
        if (change2mp3) {
            //TODO itning 转换后和数据库里的音乐类型不匹配
            log.info("上传前将音频文件转成MP3 原始音频大小：{} 文件类型：{}", newMusicFile.length(), musicType);
            long start = System.currentTimeMillis();
            if (StringUtils.isBlank(nasProperties.getFfmpegBinDir())) {
                throw new IllegalStateException("无法转换：ffmpeg bin目录未配置");
            }
            File resultFile = new File(System.getProperty("java.io.tmpdir") + File.separator + musicId + ".mp3");
            ProcessBuilder pb = new ProcessBuilder(nasProperties.getFfmpegBinDir() + File.separatorChar + "ffmpeg", "-i", newMusicFile.getPath(), resultFile.getPath());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            InputStream inputStream = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);

            try (inputStream; isr; br) {
                String line;
                while ((line = br.readLine()) != null) {
                    log.debug(line);
                }
            }
            process.waitFor();
            log.info("转换完成 耗时：{}ms", System.currentTimeMillis() - start);
            if (!resultFile.exists()) {
                throw new IllegalStateException("无法转换：转换后检查文件不存在");
            }
            newMusicFile = resultFile;
        }

        File dest = new File(musicDataSourceConfig.getMusicFileDir() + File.separator + musicId);
        try (FileInputStream in = new FileInputStream(newMusicFile);
             FileChannel sourceChannel = in.getChannel();
             FileOutputStream out = new FileOutputStream(dest);
             FileChannel destChannel = out.getChannel()) {
            log.info("拷贝文件从{}到{}", newMusicFile.getPath(), dest.getPath());
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally {
            if (change2mp3) {
                deleteFile(newMusicFile.toPath());
            }
        }
    }

    @Override
    public boolean deleteMusic(String musicId) {
        if (StringUtils.isBlank(musicDataSourceConfig.getMusicFileDir())) {
            log.info("歌曲目录未配置，跳过删除歌曲");
            return true;
        }
        File dest = new File(musicDataSourceConfig.getMusicFileDir() + File.separator + musicId);
        if (!dest.exists() || !dest.isFile()) {
            return false;
        }
        return deleteFile(dest.toPath());
    }

    @Override
    public URI getMusic(String musicId) {
        return URI.create(getOrigin() + "file?id=" + musicId);
    }

    @Override
    public File getMusicFile(String musicId) throws IOException {
        Path path = Paths.get(System.getProperty("java.io.tmpdir"), musicId);
        Files.copy(new File(musicDataSourceConfig.getMusicFileDir() + File.separator + musicId).toPath(), path, StandardCopyOption.REPLACE_EXISTING);
        return path.toFile();
    }

    @Override
    public long getFileSize(String musicId) {
        return new File(musicDataSourceConfig.getMusicFileDir() + File.separator + musicId).length();
    }

    @Override
    public void addLyric(InputStream lyricInputStream, long length, String lyricId) throws Exception {
        if (StringUtils.isBlank(musicDataSourceConfig.getLyricFileDir())) {
            log.info("歌词目录未配置，跳过添加歌词");
            return;
        }
        File lyricFile = new File(musicDataSourceConfig.getLyricFileDir() + File.separator + lyricId);
        if (lyricFile.exists()) {
            throw new IllegalArgumentException("歌词已经存在了");
        }
        Files.copy(lyricInputStream, Paths.get(musicDataSourceConfig.getLyricFileDir(), lyricId));
    }

    @Override
    public boolean deleteLyric(String lyricId) {
        if (StringUtils.isBlank(musicDataSourceConfig.getLyricFileDir())) {
            log.info("歌词目录未配置，跳过删除歌词");
            return true;
        }
        File lyricFile = new File(musicDataSourceConfig.getLyricFileDir() + File.separator + lyricId);
        if (!lyricFile.exists() || !lyricFile.isFile()) {
            return false;
        }
        return deleteFile(lyricFile.toPath());
    }

    @Override
    public URI getLyric(String lyricId) {
        return URI.create(getOrigin() + "file/lyric?id=" + lyricId);
    }

    @Override
    public void addCover(String musicId, String mimeType, byte[] binaryData) throws Exception {
        // do nothing
    }

    @Override
    public URI getCover(String musicId) {
        return URI.create(getOrigin() + "file/cover?id=" + musicId);
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
        } catch (NoSuchFileException e) {
            log.warn("文件不存在，无需删除：{}", path, e);
        } catch (Exception e) {
            log.error("删除文件失败，文件路径：{}", path, e);
        }
        return success;
    }

    private String getOrigin() {
        // 优先级：数据源配置 > 服务端地址 > 访问的地址
        String origin = Optional.ofNullable(musicDataSourceConfig.getUrlPrefix()).filter(StringUtils::isNotBlank).orElseGet(() -> {
            if (null == nasProperties.getServerUrl()) {
                HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
                URI uri = URI.create(request.getRequestURL().toString()).resolve("/");
                return uri.toString();
            } else {
                return nasProperties.getServerUrl().toString();
            }
        });
        return origin.endsWith("/") ? origin : origin + "/";
    }
}
