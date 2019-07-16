package top.itning.yunshu.yunshunas.service.impl;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import top.itning.yunshu.yunshunas.service.VideoService;
import top.itning.yunshu.yunshunas.video.IVideoRepository;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author itning
 * @date 2019/7/16 14:14
 */
@Service
public class VideoServiceImpl implements VideoService {
    private final IVideoRepository iVideoRepository;

    public VideoServiceImpl(IVideoRepository iVideoRepository) {
        this.iVideoRepository = iVideoRepository;
    }

    @Override
    public void getM3u8File(String name, OutputStream outputStream) throws IOException {
        FileUtils.copyFile(new File(iVideoRepository.readM3U8File(name)), outputStream);
    }

    @Override
    public void getTsFile(String name, OutputStream outputStream) throws IOException {
        FileUtils.copyFile(new File(iVideoRepository.readTsFile(name)), outputStream);
    }
}
