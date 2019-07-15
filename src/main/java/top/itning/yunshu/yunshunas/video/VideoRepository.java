package top.itning.yunshu.yunshunas.video;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import javax.annotation.Nonnull;
import java.io.File;

/**
 * @author itning
 * @date 2019/7/15 11:25
 */
@Component
public class VideoRepository implements IVideoRepository {
    private static final Logger logger = LoggerFactory.getLogger(VideoRepository.class);

    public static final String TO_PATH = "C:\\Users\\Administrator\\Desktop\\a";

    private LoadingCache<String, String> locationMd5Cache;

    public VideoRepository() {
        this.locationMd5Cache = CacheBuilder.newBuilder()
                .softValues()
                .initialCapacity(1000)
                .maximumSize(1000)
                .build(new CacheLoader<String, String>() {
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
        File file = new File(TO_PATH + File.separator + getLocationMd5(location));
        boolean mkdirs = file.mkdirs();
        if (logger.isDebugEnabled()) {
            logger.debug("path: {} mkdirs: {}", file.getPath(), mkdirs);
        }
        return file.getPath();
    }

    @Override
    public String readM3U8File(String name) {
        return TO_PATH + File.separator + name + File.separator + name + ".m3u8";
    }

    @Override
    public String readTsFile(String name) {
        int i = name.lastIndexOf("-");
        String dir = name.substring(0, i);
        return TO_PATH + File.separator + dir + File.separator + name + ".ts";
    }
}
