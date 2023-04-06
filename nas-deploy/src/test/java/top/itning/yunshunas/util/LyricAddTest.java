package top.itning.yunshunas.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import top.itning.yunshunas.music.entity.SearchResult;
import top.itning.yunshunas.music.repository.MusicRepository;
import top.itning.yunshunas.music.service.FileService;
import top.itning.yunshunas.music.service.MusicMetaInfoService;
import top.itning.yunshunas.music.service.MusicService;
import top.itning.yunshunas.music.service.SearchService;

import java.util.List;

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

    @Autowired
    private MusicMetaInfoService musicMetaInfoService;

    @Test
    public void testAdd() throws Exception {

    }

    @Test
    public void testSearch() {
        List<SearchResult> result = searchService.searchLyric("我嫉妒");

        result.forEach(System.out::println);
    }
}
