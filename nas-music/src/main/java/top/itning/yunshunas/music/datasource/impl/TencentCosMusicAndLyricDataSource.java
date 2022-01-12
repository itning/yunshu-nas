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
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.TransferManagerConfiguration;
import com.qcloud.cos.transfer.Upload;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.InputStream;
import java.net.URI;

/**
 * 腾讯云COS音乐与歌词数据源
 *
 * @author itning
 * @since 2022/1/12 11:37
 */
@Slf4j
public class TencentCosMusicAndLyricDataSource implements MusicDataSource, LyricDataSource {
    private final NasProperties nasProperties;
    private final COSClient cosClient;
    private TransferManager transferManager;

    public TencentCosMusicAndLyricDataSource(NasProperties nasProperties) {
        this.nasProperties = nasProperties;
        if (StringUtils.isAnyBlank(nasProperties.getTencentCosSecretId(), nasProperties.getTencentCosSecretKey())) {
            throw new IllegalArgumentException("SecretId或SecretKey未配置");
        }
        if (StringUtils.isBlank(nasProperties.getTencentCosRegionName())) {
            throw new IllegalArgumentException("RegionName未配置");
        }
        COSCredentials cred = new BasicCOSCredentials(nasProperties.getTencentCosSecretId(), nasProperties.getTencentCosSecretKey());
        Region region = new Region(nasProperties.getTencentCosRegionName());
        ClientConfig clientConfig = new ClientConfig(region);
        clientConfig.setHttpProtocol(HttpProtocol.https);
        cosClient = new COSClient(cred, clientConfig);
    }

    @PostConstruct
    public void init() {
        if (StringUtils.isBlank(nasProperties.getTencentCosMusicBucketName())) {
            throw new IllegalArgumentException("Music BucketName未配置");
        }
        if (!cosClient.doesBucketExist(nasProperties.getTencentCosMusicBucketName())) {
            throw new IllegalArgumentException("Music BucketName不存在");
        }
        if (StringUtils.isBlank(nasProperties.getTencentCosLyricBucketName())) {
            throw new IllegalArgumentException("Lyric BucketName未配置");
        }
        if (!cosClient.doesBucketExist(nasProperties.getTencentCosLyricBucketName())) {
            throw new IllegalArgumentException("Lyric BucketName不存在");
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
        PutObjectRequest putObjectRequest = new PutObjectRequest(nasProperties.getTencentCosMusicBucketName(), musicId, newMusicFile);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(musicType.getMediaType());
        putObjectRequest.setMetadata(objectMetadata);
        Upload upload = transferManager.upload(putObjectRequest);
        UploadResult uploadResult = upload.waitForUploadResult();
        log.info("腾讯云COS音乐数据源上传结果：{}", uploadResult.getKey());
    }

    @Override
    public boolean deleteMusic(String musicId) {
        try {
            cosClient.deleteObject(nasProperties.getTencentCosMusicBucketName(), musicId);
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
        return URI.create("https://" + nasProperties.getTencentCosMusicBucketName() + ".cos." + nasProperties.getTencentCosRegionName() + ".myqcloud.com/" + musicId);
    }

    @Override
    public void addLyric(InputStream lyricInputStream, String lyricId) throws Exception {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("text/plain");
        PutObjectRequest putObjectRequest = new PutObjectRequest(nasProperties.getTencentCosLyricBucketName(), lyricId, lyricInputStream, objectMetadata);
        Upload upload = transferManager.upload(putObjectRequest);
        UploadResult uploadResult = upload.waitForUploadResult();
        log.info("腾讯云COS歌词数据源上传结果：{}", uploadResult.getKey());
    }

    @Override
    public boolean deleteLyric(String lyricId) {
        try {
            cosClient.deleteObject(nasProperties.getTencentCosLyricBucketName(), lyricId);
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
        return URI.create("https://" + nasProperties.getTencentCosLyricBucketName() + ".cos." + nasProperties.getTencentCosRegionName() + ".myqcloud.com/" + lyricId);
    }
}
