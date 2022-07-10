package top.itning.yunshunas.music;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.datasource.MusicDataSource;

import java.io.File;
import java.util.UUID;

/**
 * @author itning
 * @since 2022/1/12 13:51
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
class TencentCosDataSourceTest {
    @Autowired
    private MusicDataSource musicDataSource;

    @Test
    void testAdd() throws Exception {
        musicDataSource.addMusic(new File("C:\\Users\\wangn\\Music\\陈奕迅 - 孤勇者hires.flac"), MusicType.FLAC, UUID.randomUUID().toString().replaceAll("-", ""));
    }

    @Test
    void testDelete() {
        musicDataSource.deleteMusic("03b6bdca3b4444f7a17068949a9733bd");
    }

    @Test
    void testGet() {
        System.out.println(musicDataSource.getMusic("03b6bdca3b4444f7a17068949a9733bd"));
    }
}
