package top.itning.yunshu.yunshunas.video;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import top.itning.yunshu.yunshunas.repository.IVideoRepository;
import top.itning.yunshu.yunshunas.socket.ProgressWebSocket;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author itning
 * @date 2019/7/14 16:41
 */
@Component
public class VideoTransformHandler {
    private static final Logger logger = LoggerFactory.getLogger(VideoTransformHandler.class);

    private final LinkedBlockingQueue<String> linkedBlockingQueue;

    private final Video2M3u8Helper video2M3u8Helper;
    private final ThreadPoolExecutor transformExecutorService;
    private final ThreadPoolExecutor synchronousBlockingSingleService;
    private final IVideoRepository iVideoRepository;
    private final Video2M3u8Helper.Progress progress;

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
            public void onLine(String line) {
                ProgressWebSocket.sendMessage(line);
            }

            @Override
            public void onFinish(String fromFile, String toPath, String fileName) {
                ProgressWebSocket.sendMessage(String.format("完成转换 文件：%s 目标路径：%s 文件名：%s", fromFile, toPath, fileName));
            }

            @Override
            public void onError(Exception e, String fromFile, String toPath, String fileName) {
                ProgressWebSocket.sendMessage(String.format("Exception In Video Convert: %s %s %s", fromFile, toPath, fileName));
                ProgressWebSocket.sendMessage(e.getMessage());
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
            if (linkedBlockingQueue.contains(location)) {
                return false;
            }

            File m3u8File = new File(iVideoRepository.getWriteDir(location) + File.separator + iVideoRepository.getLocationMd5(location) + ".m3u8");
            if (m3u8File.exists()) {
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
