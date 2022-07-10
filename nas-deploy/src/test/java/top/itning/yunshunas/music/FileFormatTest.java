package top.itning.yunshunas.music;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.dto.MusicMetaInfo;
import top.itning.yunshunas.music.entity.Music;
import top.itning.yunshunas.music.repository.MusicRepository;
import top.itning.yunshunas.music.service.MusicMetaInfoService;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Slf4j
class FileFormatTest {
    @Autowired
    private MusicMetaInfoService musicMetaInfoService;
    @Autowired
    private MusicRepository musicRepository;

    /**
     *
     */
    @Test
    void generateFile() throws IOException {
        File musicAndLyricDir = new File("E:\\music_yunshuV2");
        File[] allFiles = musicAndLyricDir.listFiles((dir, name) -> !name.endsWith(".lrc"));
        assert allFiles != null;
        FileWriter writer = new FileWriter("E:\\no.log");
        for (File itemFile : allFiles) {
            String musicId = UUID.randomUUID().toString().replaceAll("-", "");
            String ext = StringUtils.getFilenameExtension(itemFile.getName());
            String musicName = StringUtils.stripFilenameExtension(itemFile.getName());
            File lyricFile = new File("E:\\music_yunshuV2\\" + musicName + ".lrc");

            try {
                MusicType musicType = MusicType.valueOf(ext.toUpperCase());
                MusicMetaInfo metaInfo = musicMetaInfoService.metaInfo(itemFile, musicType);
                String name = metaInfo.getTitle().trim();
                String singer = metaInfo.getArtists().get(0);
                Music music = new Music();
                music.setMusicId(musicId);
                music.setName(name);
                music.setSinger(singer);
                music.setLyricId(musicId);
                music.setType(musicType.getType());
                musicRepository.save(music);
                copyFileUsingChannel(itemFile, new File("G:\\music\\" + musicId));
                if (lyricFile.exists()) {
                    copyFileUsingChannel(lyricFile, new File("G:\\lyric\\" + musicId));
                }
            } catch (Exception e) {
                writer.write(itemFile.getPath() + "  " + e.getMessage() + "\n");
            }
        }
        writer.flush();
        writer.close();
    }

    private static void copyFileUsingChannel(File source, File dest) throws IOException {
        try (FileInputStream in = new FileInputStream(source);
             FileChannel sourceChannel = in.getChannel();
             FileOutputStream out = new FileOutputStream(dest);
             FileChannel destChannel = out.getChannel()) {
            destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
        } finally {
            System.out.println("ok");
        }
    }

    @Test
    void testWhy() throws Exception {
        File file = new File("E:\\music_yunshuV2");
        System.out.println(file.listFiles((dir, name) -> !name.endsWith(".lrc")).length);
    }

    @Test
    void testWhy2() throws Exception {
        File file = new File("G:\\music");
        for (File listFile : file.listFiles()) {
            if (musicRepository.findByMusicId(listFile.getName()).isEmpty()) {
                System.out.println(listFile.getPath());
            }
        }

    }

    @Test
    void test3() throws Exception {
        File file = new File("C:\\Users\\wangn\\Desktop\\a");
        for (File itemFile : file.listFiles()) {
            String musicName = StringUtils.stripFilenameExtension(itemFile.getName());
            String ext = StringUtils.getFilenameExtension(itemFile.getName());
            MusicType musicType = MusicType.valueOf(ext.toUpperCase());
            MusicMetaInfo metaInfo = musicMetaInfoService.metaInfo(itemFile, musicType);
            String name = metaInfo.getTitle().trim();
            String singer = metaInfo.getArtists().get(0);
            Music music = new Music();
            music.setMusicId(musicName);
            music.setName(name);
            music.setSinger(singer);
            music.setLyricId(musicName);
            music.setType(musicType.getType());
            musicRepository.save(music);
        }
    }
}
