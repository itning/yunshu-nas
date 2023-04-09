package top.itning.yunshunas.video.video;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import top.itning.yunshunas.common.config.NasProperties;
import top.itning.yunshunas.common.db.ApplicationConfig;
import top.itning.yunshunas.video.repository.IVideoRepository;

import java.io.File;

/**
 * @author itning
 * @since 2019/7/15 11:25
 */
@Component
public class VideoRepository implements IVideoRepository {
    private static final Logger logger = LoggerFactory.getLogger(VideoRepository.class);

    private final LoadingCache<String, String> locationMd5Cache;
    private final ApplicationConfig applicationConfig;

    public VideoRepository(ApplicationConfig applicationConfig) {
        this.applicationConfig = applicationConfig;
        this.locationMd5Cache = CacheBuilder.newBuilder()
                .softValues()
                .initialCapacity(100)
                .maximumSize(100)
                .build(new CacheLoader<>() {
                    @Override
                    public String load(@Nonnull String key) {
                        return DigestUtils.md5DigestAsHex(key.getBytes());
                    }
                });
    }

    @Override
    public String getLocationMd5(String location) {
        return locationMd5Cache.getUnchecked(location);
    }

    @Override
    public String getWriteDir(String location) {
        NasProperties nasProperties = applicationConfig.getSetting(NasProperties.class);
        File file = new File(nasProperties.getOutDir() + File.separator + getLocationMd5(location));
        boolean mkdirs = file.mkdirs();
        if (logger.isDebugEnabled()) {
            logger.debug("path: {} mkdirs: {}", file.getPath(), mkdirs);
        }
        return file.getPath();
    }

    @Override
    public String readM3U8File(String name) {
        NasProperties nasProperties = applicationConfig.getSetting(NasProperties.class);
        return nasProperties.getOutDir() + File.separator + name + File.separator + name + ".m3u8";
    }

    @Override
    public String readTsFile(String name) {
        int i = name.lastIndexOf("-");
        String dir = name.substring(0, i);
        NasProperties nasProperties = applicationConfig.getSetting(NasProperties.class);
        return nasProperties.getOutDir() + File.separator + dir + File.separator + name + ".ts";
    }
}
