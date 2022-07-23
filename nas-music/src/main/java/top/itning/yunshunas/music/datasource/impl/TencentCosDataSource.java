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
import top.itning.yunshunas.music.datasource.CoverDataSource;
import top.itning.yunshunas.music.datasource.LyricDataSource;
import top.itning.yunshunas.music.datasource.MusicDataSource;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
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
public class TencentCosDataSource implements MusicDataSource, LyricDataSource, CoverDataSource {

    private static final String MUSIC_DIR_NAME = "music";
    private static final String LYRIC_DIR_NAME = "lyric";
    private static final String COVER_DIR_NAME = "cover";

    private final COSClient cosClient;
    private final NasProperties.TencentCosDataSourceConfig tencentCosDataSourceConfig;
    private TransferManager transferManager;

    public TencentCosDataSource(NasProperties nasProperties) {
        this.tencentCosDataSourceConfig = nasProperties.getTencentCosDataSource();
        if (StringUtils.isAnyBlank(tencentCosDataSourceConfig.getSecretId(), tencentCosDataSourceConfig.getSecretKey())) {
            throw new IllegalArgumentException("SecretId或SecretKey未配置");
        }
        if (StringUtils.isBlank(tencentCosDataSourceConfig.getRegionName())) {
            throw new IllegalArgumentException("RegionName未配置");
        }
        COSCredentials cred = new BasicCOSCredentials(tencentCosDataSourceConfig.getSecretId(), tencentCosDataSourceConfig.getSecretKey());
        Region region = new Region(tencentCosDataSourceConfig.getRegionName());
        ClientConfig clientConfig = new ClientConfig(region);
        clientConfig.setHttpProtocol(HttpProtocol.https);
        cosClient = new COSClient(cred, clientConfig);
    }

    @PostConstruct
    public void init() {
        if (StringUtils.isBlank(tencentCosDataSourceConfig.getBucketName())) {
            throw new IllegalArgumentException("BucketName未配置");
        }
        if (!cosClient.doesBucketExist(tencentCosDataSourceConfig.getBucketName())) {
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
        PutObjectRequest putObjectRequest = new PutObjectRequest(tencentCosDataSourceConfig.getBucketName(), MUSIC_DIR_NAME + "/" + musicId, newMusicFile);
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
            cosClient.deleteObject(tencentCosDataSourceConfig.getBucketName(), MUSIC_DIR_NAME + "/" + musicId);
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
        if (StringUtils.isNotBlank(tencentCosDataSourceConfig.getCdnUrl())) {
            return URI.create(tencentCosDataSourceConfig.getCdnUrl() + "/" + MUSIC_DIR_NAME + "/" + musicId);
        }
        return URI.create("https://" + tencentCosDataSourceConfig.getBucketName() + ".cos." + tencentCosDataSourceConfig.getRegionName() + ".myqcloud.com/" + MUSIC_DIR_NAME + "/" + musicId);
    }

    @Override
    public void addLyric(InputStream lyricInputStream, long length, String lyricId) throws Exception {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType("text/plain; charset=utf-8");
        objectMetadata.setContentLength(length);
        PutObjectRequest putObjectRequest = new PutObjectRequest(tencentCosDataSourceConfig.getBucketName(), LYRIC_DIR_NAME + "/" + lyricId, lyricInputStream, objectMetadata);
        Upload upload = transferManager.upload(putObjectRequest);
        UploadResult uploadResult = upload.waitForUploadResult();
        log.info("腾讯云COS歌词数据源上传结果：{}", uploadResult.getKey());
    }

    @Override
    public boolean deleteLyric(String lyricId) {
        try {
            cosClient.deleteObject(tencentCosDataSourceConfig.getBucketName(), LYRIC_DIR_NAME + "/" + lyricId);
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
        if (StringUtils.isNotBlank(tencentCosDataSourceConfig.getCdnUrl())) {
            return URI.create(tencentCosDataSourceConfig.getCdnUrl() + "/" + LYRIC_DIR_NAME + "/" + lyricId);
        }
        return URI.create("https://" + tencentCosDataSourceConfig.getBucketName() + ".cos." + tencentCosDataSourceConfig.getRegionName() + ".myqcloud.com/" + LYRIC_DIR_NAME + "/" + lyricId);
    }

    @Override
    public void addCover(String musicId, String mimeType, byte[] binaryData) throws Exception {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(binaryData);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentType(mimeType);
        objectMetadata.setContentLength(binaryData.length);
        PutObjectRequest putObjectRequest = new PutObjectRequest(tencentCosDataSourceConfig.getBucketName(), COVER_DIR_NAME + "/" + musicId, byteArrayInputStream, objectMetadata);
        Upload upload = transferManager.upload(putObjectRequest);
        UploadResult uploadResult = upload.waitForUploadResult();
        log.info("腾讯云COS封面数据源上传结果：{}", uploadResult.getKey());
    }

    @Override
    public URI getCover(String musicId) {
        if (StringUtils.isNotBlank(tencentCosDataSourceConfig.getCdnUrl())) {
            return URI.create(tencentCosDataSourceConfig.getCdnUrl() + "/" + COVER_DIR_NAME + "/" + musicId);
        }
        return URI.create("https://" + tencentCosDataSourceConfig.getBucketName() + ".cos." + tencentCosDataSourceConfig.getRegionName() + ".myqcloud.com/" + COVER_DIR_NAME + "/" + musicId);
    }

    @Override
    public boolean deleteCover(String musicId) {
        try {
            cosClient.deleteObject(tencentCosDataSourceConfig.getBucketName(), COVER_DIR_NAME + "/" + musicId);
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
