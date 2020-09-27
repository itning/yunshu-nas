package top.itning.yunshu.yunshunas.video;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import top.itning.yunshu.yunshunas.config.NasProperties;
import top.itning.yunshu.yunshunas.repository.IVideoRepository;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * @author itning
 * @date 2019/7/15 11:25
 */
@Component
public class VideoRepository implements IVideoRepository {
    private static final Logger logger = LoggerFactory.getLogger(VideoRepository.class);

    private final LoadingCache<String, String> locationMd5Cache;
    private final NasProperties nasProperties;

    public VideoRepository(NasProperties nasProperties) {
        this.nasProperties = nasProperties;
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
        File file = new File(nasProperties.getOutDir() + File.separator + getLocationMd5(location));
        boolean mkdirs = file.mkdirs();
        if (logger.isDebugEnabled()) {
            logger.debug("path: {} mkdirs: {}", file.getPath(), mkdirs);
        }
        return file.getPath();
    }

    @Override
    public String readM3U8File(String name) {
        return nasProperties.getOutDir() + File.separator + name + File.separator + name + ".m3u8";
    }

    @Override
    public String readTsFile(String name) {
        int i = name.lastIndexOf("-");
        String dir = name.substring(0, i);
        return nasProperties.getOutDir() + File.separator + dir + File.separator + name + ".ts";
    }
}
