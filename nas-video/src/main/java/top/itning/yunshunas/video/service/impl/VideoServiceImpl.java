package top.itning.yunshunas.video.service.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import top.itning.yunshunas.video.entity.FileEntity;
import top.itning.yunshunas.video.repository.IVideoRepository;
import top.itning.yunshunas.video.service.VideoService;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author itning
 * @since 2019/7/16 14:14
 */
@Service
public class VideoServiceImpl implements VideoService {
    private static final Set<String> VIDEO_SUFFIX = Set.of("mp4", "avi", "3gp", "wmv", "mkv", "mpeg", "rmvb");

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
        if (StringUtils.isBlank(location)) {
            files = File.listRoots();
        } else {
            byte[] decode = Base64.getUrlDecoder().decode(location);
            File file = new File(new String(decode, StandardCharsets.UTF_8));
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
                fileEntity.setLocation(Base64.getUrlEncoder().encodeToString(f.getPath().getBytes(StandardCharsets.UTF_8)));
                fileEntities.add(fileEntity);
            }
        } else {
            fileEntities = Collections.emptyList();
        }
        return fileEntities
                .stream()
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
        return VIDEO_SUFFIX.contains(suffix);
    }
}
