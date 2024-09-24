package top.itning.yunshunas.music.datasource.impl;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.UploadResult;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.Download;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import com.qcloud.cos.transfer.Upload;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.music.config.NasMusicProperties;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.datasource.CoverDataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;

import java.io.*;
import java.net.URI;

/**
 * 腾讯云COS音乐与歌词数据源
 *
 * @author itning
 * @since 2022/1/12 11:37
 */
@Slf4j
public class TencentCosDataSource implements MusicDataSource, LyricDataSource, CoverDataSource {

    private static final String MUSIC_DIR_NAME = "music";
    private static final String LYRIC_DIR_NAME = "lyric";
    private static final String COVER_DIR_NAME = "cover";

    private final COSClient cosClient;
    private final NasProperties nasProperties;
    private final NasMusicProperties.MusicDataSourceConfig musicDataSourceConfig;
    private final TransferManager transferManager;

    public TencentCosDataSource(NasProperties nasProperties, NasMusicProperties.MusicDataSourceConfig musicDataSourceConfig) {
        this.nasProperties = nasProperties;
        this.musicDataSourceConfig = musicDataSourceConfig;
        if (StringUtils.isAnyBlank(musicDataSourceConfig.getSecretId(), musicDataSourceConfig.getSecretKey())) {
            throw new IllegalArgumentException("SecretId或SecretKey未配置");
        }
        if (StringUtils.isBlank(musicDataSourceConfig.getRegionName())) {
            throw new IllegalArgumentException("RegionName未配置");
        }
        COSCredentials cred = new BasicCOSCredentials(musicDataSourceConfig.getSecretId(), musicDataSourceConfig.getSecretKey());
        Region region = new Region(musicDataSourceConfig.getRegionName());
        ClientConfig clientConfig = new ClientConfig(region);
        clientConfig.setHttpProtocol(HttpProtocol.https);
        cosClient = new COSClient(cred, clientConfig);

        if (StringUtils.isBlank(musicDataSourceConfig.getBucketName())) {
            throw new IllegalArgumentException("BucketName未配置");
        }
        if (!cosClient.doesBucketExist(musicDataSourceConfig.getBucketName())) {
            throw new IllegalArgumentException("BucketName不存在");
        }

        // 传入一个 threadpool, 若不传入线程池，默认 TransferManager 中会生成一个单线程的线程池。
        transferManager = new TransferManager(cosClient);

        // 设置高级接口的配置项
        // 分块上传阈值和分块大小分别为 5MB 和 1MB
        TransferManagerConfiguration transferManagerConfiguration = new TransferManagerConfiguration();
        transferManagerConfiguration.setMultipartUploadThreshold(5 * 1024 * 1024);
        transferManagerConfiguration.setMinimumUploadPartSize(1024 * 1024);
        transferManager.setConfiguration(transferManagerConfiguration);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> transferManager.shutdownNow(true)));
    }

    @Override
    public void addMusic(File newMusicFile, MusicType musicType, String musicId) throws Exception {
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
        PutObjectRequest putObjectRequest = new PutObjectRequest(musicDataSourceConfig.getBucketName(), MUSIC_DIR_NAME + "/" + musicId, newMusicFile);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(change2mp3 ? MusicType.MP3.getMediaType() : musicType.getMediaType());
        putObjectRequest.setMetadata(objectMetadata);
        Upload upload = transferManager.upload(putObjectRequest);
        UploadResult uploadResult = upload.waitForUploadResult();
        log.info("腾讯云COS音乐数据源上传结果：{}", uploadResult.getKey());
        if (change2mp3) {
            log.info("删除转换后的文件：{} 结果：{}", newMusicFile, newMusicFile.delete());
        }
    }

    @Override
    public boolean deleteMusic(String musicId) {
        try {
            cosClient.deleteObject(musicDataSourceConfig.getBucketName(), MUSIC_DIR_NAME + "/" + musicId);
            return true;
        } catch (CosClientException e) {
            log.warn("腾讯云COS音乐数据源删除异常", e);
            return false;
        } catch (Exception e) {
            log.error("腾讯云COS音乐数据源删除异常", e);
            return false;
        }
    }

    @Override
    public URI getMusic(String musicId) {
        if (StringUtils.isNotBlank(musicDataSourceConfig.getCdnUrl())) {
            return URI.create(musicDataSourceConfig.getCdnUrl() + "/" + MUSIC_DIR_NAME + "/" + musicId);
        }
        return URI.create("https://" + musicDataSourceConfig.getBucketName() + ".cos." + musicDataSourceConfig.getRegionName() + ".myqcloud.com/" + MUSIC_DIR_NAME + "/" + musicId);
    }

    @Override
    public File getMusicFile(String musicId) throws InterruptedException {
        File tempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + musicId);
        if (tempFile.exists()) {
            log.warn("临时文件{}已存在，删除结果：{}", tempFile, tempFile.delete());
        }
        Download download = transferManager.download(musicDataSourceConfig.getBucketName(), musicDataSourceConfig.getRegionName(), tempFile);
        download.waitForCompletion();
        return tempFile;
    }

    @Override
    public long getFileSize(String musicId) {
        ObjectMetadata objectMetadata = cosClient.getObjectMetadata(musicDataSourceConfig.getBucketName(), musicId);
        return objectMetadata.getContentLength();
    }

    @Override
    public void addLyric(InputStream lyricInputStream, long length, String lyricId) throws Exception {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("text/plain; charset=utf-8");
        objectMetadata.setContentLength(length);
        PutObjectRequest putObjectRequest = new PutObjectRequest(musicDataSourceConfig.getBucketName(), LYRIC_DIR_NAME + "/" + lyricId, lyricInputStream, objectMetadata);
        Upload upload = transferManager.upload(putObjectRequest);
        UploadResult uploadResult = upload.waitForUploadResult();
        log.info("腾讯云COS歌词数据源上传结果：{}", uploadResult.getKey());
    }

    @Override
    public boolean deleteLyric(String lyricId) {
        try {
            cosClient.deleteObject(musicDataSourceConfig.getBucketName(), LYRIC_DIR_NAME + "/" + lyricId);
            return true;
        } catch (CosClientException e) {
            log.warn("腾讯云COS歌词数据源删除异常", e);
            return false;
        } catch (Exception e) {
            log.error("腾讯云COS歌词数据源删除异常", e);
            return false;
        }
    }

    @Override
    public URI getLyric(String lyricId) {
        if (StringUtils.isNotBlank(musicDataSourceConfig.getCdnUrl())) {
            return URI.create(musicDataSourceConfig.getCdnUrl() + "/" + LYRIC_DIR_NAME + "/" + lyricId);
        }
        return URI.create("https://" + musicDataSourceConfig.getBucketName() + ".cos." + musicDataSourceConfig.getRegionName() + ".myqcloud.com/" + LYRIC_DIR_NAME + "/" + lyricId);
    }

    @Override
    public void addCover(String musicId, String mimeType, byte[] binaryData) throws Exception {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(binaryData);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(mimeType);
        objectMetadata.setContentLength(binaryData.length);
        PutObjectRequest putObjectRequest = new PutObjectRequest(musicDataSourceConfig.getBucketName(), COVER_DIR_NAME + "/" + musicId, byteArrayInputStream, objectMetadata);
        Upload upload = transferManager.upload(putObjectRequest);
        UploadResult uploadResult = upload.waitForUploadResult();
        log.info("腾讯云COS封面数据源上传结果：{}", uploadResult.getKey());
    }

    @Override
    public URI getCover(String musicId) {
        if (StringUtils.isNotBlank(musicDataSourceConfig.getCdnUrl())) {
            return URI.create(musicDataSourceConfig.getCdnUrl() + "/" + COVER_DIR_NAME + "/" + musicId);
        }
        return URI.create("https://" + musicDataSourceConfig.getBucketName() + ".cos." + musicDataSourceConfig.getRegionName() + ".myqcloud.com/" + COVER_DIR_NAME + "/" + musicId);
    }

    @Override
    public boolean deleteCover(String musicId) {
        try {
            cosClient.deleteObject(musicDataSourceConfig.getBucketName(), COVER_DIR_NAME + "/" + musicId);
            return true;
        } catch (CosClientException e) {
            log.warn("腾讯云COS封面数据源删除异常", e);
            return false;
        } catch (Exception e) {
            log.error("腾讯云COS封面数据源删除异常", e);
            return false;
        }
    }
}
