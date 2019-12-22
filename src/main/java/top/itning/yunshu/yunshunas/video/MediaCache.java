package top.itning.yunshu.yunshunas.video;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.itning.yunshu.yunshunas.repository.IVideoRepository;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * @author itning
 * @date 2019/7/14 11:15
 */
@Component
public class MediaCache {
    private static final Logger logger = LoggerFactory.getLogger(MediaCache.class);

    private final LoadingCache<String, byte[]> m3u8Cache;
    private final LoadingCache<String, byte[]> tsCache;

    public MediaCache(IVideoRepository iVideoRepository) {
        m3u8Cache = CacheBuilder.newBuilder()
                .softValues()
                .expireAfterAccess(60, TimeUnit.MINUTES)
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
                .expireAfterAccess(60, TimeUnit.MINUTES)
                .maximumSize(1000)
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
