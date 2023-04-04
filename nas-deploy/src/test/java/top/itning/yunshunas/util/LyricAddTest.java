package top.itning.yunshunas.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import top.itning.yunshunas.music.entity.Music;
import top.itning.yunshunas.music.entity.SearchResult;
import top.itning.yunshunas.music.repository.MusicRepository;
import top.itning.yunshunas.music.service.FileService;
import top.itning.yunshunas.music.service.MusicService;
import top.itning.yunshunas.music.service.SearchService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author itning
 * @since 2022/11/2 21:40
 */
@ActiveProfiles("local")
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LyricAddTest {

    @Autowired
    private SearchService searchService;

    @Autowired
    private FileService fileService;

    @Autowired
    private MusicService musicService;

    @Autowired
    private MusicRepository musicRepository;

    @Test
    public void testAdd() {
        File mp3Dir = new File("E:\\app\\Audio\\music_yunshu_mp3");
        Set<String> mp3 = Arrays.stream(mp3Dir.listFiles()).map(File::getName).collect(Collectors.toSet());
        List<Music> all = musicRepository.findAll();
        int i = 0;
        all.removeIf(m -> mp3.contains(m.getMusicId()));
        for (Music music : all) {
            File file = new File("E:\\app\\Audio\\music_yunshu", music.getMusicId());
            if (!file.exists()) {
                log.error("文件不存在 {}", file);
                continue;
            }
            try {
                Files.copy(file.toPath(), Paths.get("E:\\app\\Audio\\music_yunshu_mp3", music.getMusicId()));
            } catch (IOException e) {
                log.error("catch error", e);
            }
        }
    }

    @Test
    public void testSearch() {
        List<SearchResult> result = searchService.searchLyric("我嫉妒", Pageable.unpaged());

        result.forEach(System.out::println);
    }
}
