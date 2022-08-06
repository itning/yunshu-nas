package top.itning.yunshunas.video.video;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.itning.yunshunas.common.socket.ProgressWebSocket;
import top.itning.yunshunas.video.repository.IVideoRepository;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author itning
 * @since 2019/7/14 16:41
 */
@Component
public class VideoTransformHandler {
    private static final Logger logger = LoggerFactory.getLogger(VideoTransformHandler.class);

    private static final Object NULL_VALUE = new Object();

    private final LinkedBlockingQueue<String> linkedBlockingQueue;

    private final Video2M3u8Helper video2M3u8Helper;
    private final ThreadPoolExecutor transformExecutorService;
    private final ThreadPoolExecutor synchronousBlockingSingleService;
    private final IVideoRepository iVideoRepository;
    private final Video2M3u8Helper.Progress progress;
    private final Map<String, Object> VIDEO_CURRENTLY_BEING_TRANSCODED = new ConcurrentHashMap<>();

    public VideoTransformHandler(Video2M3u8Helper video2M3u8Helper, IVideoRepository iVideoRepository) {
        this.video2M3u8Helper = video2M3u8Helper;
        this.iVideoRepository = iVideoRepository;
        int processors = Runtime.getRuntime().availableProcessors();

        //与CPU核数相同的线程
        this.transformExecutorService = new ThreadPoolExecutor(processors,
                processors,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("trans-pool-%d").build());
        this.synchronousBlockingSingleService = new ThreadPoolExecutor(1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new ThreadFactoryBuilder().setNameFormat("single-pool-%d").build());
        this.linkedBlockingQueue = new LinkedBlockingQueue<>();
        progress = new Video2M3u8Helper.Progress() {
            @Override
            public void onStart(String fromFile, String toPath, String fileName) {
                VIDEO_CURRENTLY_BEING_TRANSCODED.put(fromFile, NULL_VALUE);
            }

            @Override
            public void onLine(String line) {
                ProgressWebSocket.sendMessage(line);
            }

            @Override
            public void onFinish(String fromFile, String toPath, String fileName) {
                VIDEO_CURRENTLY_BEING_TRANSCODED.remove(fromFile);
                ProgressWebSocket.sendMessage(String.format("完成转换 文件：%s 目标路径：%s 文件名：%s", fromFile, toPath, fileName));
            }

            @Override
            public void onError(Exception e, String fromFile, String toPath, String fileName) {
                VIDEO_CURRENTLY_BEING_TRANSCODED.remove(fromFile);
                ProgressWebSocket.sendMessage(String.format("Exception In Video Convert: %s %s %s", fromFile, toPath, fileName));
                ProgressWebSocket.sendMessage(e.getMessage());
            }

            @Override
            public void onProgress(long frame, long totalFrames, String percentage, String line) {
                ProgressWebSocket.sendMessage(String.format("%d/%d %s", frame, totalFrames, percentage));
            }
        };
        start();
    }

    private void start() {
        synchronousBlockingSingleService.submit(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    //will blocking
                    final String location = linkedBlockingQueue.take();
                    transformExecutorService.submit(() -> video2M3u8Helper.videoConvert(
                            location,
                            iVideoRepository.getWriteDir(location),
                            iVideoRepository.getLocationMd5(location),
                            progress
                    ));
                } catch (InterruptedException e) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("get exception {}", e.getMessage());
                    }
                }
            }
        });
    }

    public boolean put(String location) {
        try {
            if (VIDEO_CURRENTLY_BEING_TRANSCODED.containsKey(location)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("currently being transcoded {}", location);
                }
                return true;
            }
            if (linkedBlockingQueue.contains(location)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("already in queue {}", location);
                }
                return true;
            }
            File m3u8File = new File(iVideoRepository.getWriteDir(location) + File.separator + iVideoRepository.getLocationMd5(location) + ".m3u8");
            if (m3u8File.exists()) {
                if (logger.isDebugEnabled()) {
                    logger.debug("already exist m3u8 file {}", location);
                }
                return false;
            }
            linkedBlockingQueue.put(location);
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Object> status() {
        Map<String, Object> statusMap = new HashMap<>(6);
        statusMap.put("activeCount", transformExecutorService.getActiveCount());
        statusMap.put("completedTaskCount", transformExecutorService.getCompletedTaskCount());
        statusMap.put("corePoolSize", transformExecutorService.getCorePoolSize());
        statusMap.put("poolSize", transformExecutorService.getPoolSize());
        statusMap.put("taskCount", transformExecutorService.getTaskCount());
        statusMap.put("queue", linkedBlockingQueue.toArray(new String[0]));
        return statusMap;
    }
}
