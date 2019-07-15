package top.itning.yunshu.yunshunas.video;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;

/**
 * @author itning
 * @date 2019/7/14 11:15
 */
@Component
public class VideoCache {
    private static final Logger logger = LoggerFactory.getLogger(VideoCache.class);

    private final LoadingCache<String, byte[]> m3u8Cache;
    private final LoadingCache<String, byte[]> tsCache;

    public VideoCache(IVideoRepository iVideoRepository) {
        m3u8Cache = CacheBuilder.newBuilder()
                .softValues()
                .initialCapacity(100)
                .maximumSize(100)
                .build(new CacheLoader<String, byte[]>() {
                    @Override
                    public byte[] load(@Nonnull String key) throws IOException {
                        if (logger.isDebugEnabled()) {
                            logger.debug("load m3u8: {}", key);
                        }

                        return FileUtils.readFileToByteArray(new File(iVideoRepository.readM3U8File(key)));
                    }
                });

        tsCache = CacheBuilder.newBuilder()
                .softValues()
                .initialCapacity(1000)
                .maximumSize(1000)
                .recordStats()
                .build(new CacheLoader<String, byte[]>() {
                    @Override
                    public byte[] load(@Nonnull String key) throws IOException {
                        if (logger.isDebugEnabled()) {
                            logger.debug("load ts: {}", key);
                        }
                        return FileUtils.readFileToByteArray(new File(iVideoRepository.readTsFile(key)));
                    }
                });
    }

    public byte[] getm3u8(String key) {
        return m3u8Cache.getUnchecked(key);
    }

    public byte[] getts(String key) {
        return tsCache.getUnchecked(key);
    }
}
