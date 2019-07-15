package top.itning.yunshu.yunshunas.video;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author itning
 * @date 2019/7/14 16:41
 */
@Component
public class VideoTransformQueue {
    private final LinkedBlockingQueue<String> linkedBlockingQueue;

    private final Video2M3u8Helper video2M3u8Helper;
    private final ExecutorService transformExecutorService;
    private final ExecutorService synchronousBlockingSingleService;
    private final IVideoRepository iVideoRepository;
    private Video2M3u8Helper.Progress progress;
    private SseEmitter sseEmitter;

    public VideoTransformQueue(Video2M3u8Helper video2M3u8Helper, IVideoRepository iVideoRepository) {
        this.video2M3u8Helper = video2M3u8Helper;
        this.iVideoRepository = iVideoRepository;
        int processors = Runtime.getRuntime().availableProcessors();
        //与CPU核数相同的线程
        this.transformExecutorService = new ThreadPoolExecutor(processors,
                processors,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new DefaultThreadFactory("video-transform-pool-"));
        this.synchronousBlockingSingleService = new ThreadPoolExecutor(1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                new DefaultThreadFactory("single-blocking-pool-"));
        this.linkedBlockingQueue = new LinkedBlockingQueue<>();
        progress = (frame, totalFrames, percentage) -> {
            if (sseEmitter != null) {
                try {
                    sseEmitter.send(frame + "/" + totalFrames + " " + percentage, MediaType.TEXT_PLAIN);
                } catch (IOException e) {
                    sseEmitter.completeWithError(e);
                }
            }
        };
        start();
    }

    public void setProgress(SseEmitter sseEmitter) {
        this.sseEmitter = sseEmitter;
    }

    private void start() {
        synchronousBlockingSingleService.submit(() -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                try {
                    //will blocking
                    final String location = linkedBlockingQueue.take();
                    transformExecutorService.submit(() -> {
                        try {
                            video2M3u8Helper.videoConvert(
                                    location,
                                    iVideoRepository.getWriteDir(location),
                                    iVideoRepository.getLocationMd5(location),
                                    progress
                            );
                            if (sseEmitter != null) {
                                sseEmitter.send("OK", MediaType.TEXT_PLAIN);
                                sseEmitter.complete();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
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

    private static class DefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        DefaultThreadFactory(String poolName) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = poolName + POOL_NUMBER.getAndIncrement() + "-thread-";
        }

        @Override
        public Thread newThread(@Nonnull Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.NORM_PRIORITY) {
                t.setPriority(Thread.NORM_PRIORITY);
            }
            return t;
        }
    }
}
