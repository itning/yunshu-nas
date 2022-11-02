package top.itning.yunshunas.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import top.itning.yunshunas.music.dto.MusicDTO;
import top.itning.yunshunas.music.entity.Lyric;
import top.itning.yunshunas.music.service.FileService;
import top.itning.yunshunas.music.service.MusicService;
import top.itning.yunshunas.music.service.SearchService;

import java.io.IOException;

/**
 * @author itning
 * @since 2022/11/2 21:40
 */
@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LyricAddTest {

    @Autowired
    private SearchService searchService;

    @Autowired
    private FileService fileService;

    @Autowired
    private MusicService musicService;

    //@Test
    public void testAdd() {
        Page<MusicDTO> all = musicService.findAll(Pageable.unpaged());
        for (MusicDTO musicDTO : all) {
            try {
                String lyric = fileService.getLyric(musicDTO.getLyricId());
                if(StringUtils.isBlank(lyric)){
                    continue;
                }
                searchService.addLyric(musicDTO.getMusicId(), musicDTO.getLyricId(), lyric);
            } catch (IOException e) {
                log.error("获取歌词出错", e);
            }
        }
    }

    @Test
    public void testSearch(){
        Page<Lyric> lyricPage = searchService.searchLyric("\"我嫉妒\"",Pageable.unpaged());

        lyricPage.forEach(System.out::println);
    }
}
