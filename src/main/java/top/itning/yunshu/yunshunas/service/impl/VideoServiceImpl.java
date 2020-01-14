package top.itning.yunshu.yunshunas.service.impl;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import top.itning.yunshu.yunshunas.entity.FileEntity;
import top.itning.yunshu.yunshunas.repository.IVideoRepository;
import top.itning.yunshu.yunshunas.service.VideoService;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author itning
 * @date 2019/7/16 14:14
 */
@Service
public class VideoServiceImpl implements VideoService {
    private static final String[] VIDEO_SUFFIX = new String[]{"mp4", "avi", "3gp", "wmv", "mkv", "mpeg", "rmvb"};

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

    @Override
    public List<FileEntity> getFileEntities(String location) {
        File[] files;
        if (location == null) {
            files = File.listRoots();
        } else {
            File file = new File(location);
            files = file.listFiles();
        }
        List<FileEntity> fileEntities;
        if (files != null) {
            fileEntities = new ArrayList<>(files.length);
            for (File f : files) {
                FileEntity fileEntity = new FileEntity();
                fileEntity.setName(f.getName());
                fileEntity.setSize(FileUtils.byteCountToDisplaySize(f.length()));
                fileEntity.setFile(f.isFile());
                fileEntity.setCanPlay(isVideoFile(f.getName()));
                fileEntity.setLocation(f.getPath());
                fileEntities.add(fileEntity);
            }
        } else {
            fileEntities = Collections.emptyList();
        }
        return fileEntities
                .parallelStream()
                .sorted((o1, o2) -> {
                    if (o1.isFile() && !o2.isFile()) {
                        return 1;
                    } else if (!o1.isFile() && o2.isFile()) {
                        return -1;
                    } else {
                        return o1.getName().compareTo(o2.getName());
                    }
                })
                .collect(Collectors.toList());
    }

    private boolean isVideoFile(String name) {
        String suffix = name.substring(name.lastIndexOf(".") + 1).toLowerCase();
        for (String s : VIDEO_SUFFIX) {
            if (s.equals(suffix)) {
                return true;
            }
        }
        return false;
    }
}
