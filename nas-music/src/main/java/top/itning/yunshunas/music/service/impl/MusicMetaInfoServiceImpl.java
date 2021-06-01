package top.itning.yunshunas.music.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.flac.FlacFileReader;
import org.jaudiotagger.audio.generic.AudioFileReader;
import org.jaudiotagger.audio.mp3.MP3FileReader;
import org.jaudiotagger.audio.wav.WavFileReader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.TagTextField;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.dto.MusicMetaInfo;
import top.itning.yunshunas.music.service.MusicMetaInfoService;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author itning
 * @since 2021/6/1 16:38
 */
@Slf4j
@Service
public class MusicMetaInfoServiceImpl implements MusicMetaInfoService {

    @Override
    public MusicMetaInfo metaInfo(File musicFile, MusicType musicType) throws Exception {
        AudioFileReader fileReader;
        switch (musicType) {
            case FLAC:
                fileReader = new FlacFileReader();
                break;
            case MP3:
                fileReader = new MP3FileReader();
                break;
            case WAV:
                fileReader = new WavFileReader();
                break;
            case AAC:
            default:
                log.warn("不支持的音乐类型：{}", musicType);
                return null;
        }
        AudioFile read = fileReader.read(musicFile);
        Tag tag = read.getTag();

        if (null == tag) {
            return null;
        }

        MusicMetaInfo musicMetaInfo = new MusicMetaInfo();
        String title = tag.getFirst(FieldKey.TITLE);
        List<TagField> artists = tag.getFields(FieldKey.ARTIST);
        if (null != artists) {
            musicMetaInfo.setArtists(
                    artists.stream()
                            .map(it -> {
                                if (it instanceof TagTextField) {
                                    return ((TagTextField) it).getContent();
                                }
                                return it.toString();
                            })
                            .collect(Collectors.toList())
            );
        }
        String album = tag.getFirst(FieldKey.ALBUM);
        musicMetaInfo.setTitle(title);
        musicMetaInfo.setAlbum(album);

        List<MusicMetaInfo.CoverPicture> coverPictures = tag.getArtworkList()
                .stream()
                .map(item -> {
                    MusicMetaInfo.CoverPicture coverPicture = new MusicMetaInfo.CoverPicture();
                    byte[] encoded = Base64Utils.encode(item.getBinaryData());
                    coverPicture.setBinaryData(null);
                    coverPicture.setBase64(new String(encoded));
                    coverPicture.setMimeType(item.getMimeType());
                    coverPicture.setDescription(item.getDescription());
                    coverPicture.setLinked(item.isLinked());
                    coverPicture.setImageUrl(item.getImageUrl());
                    coverPicture.setPictureType(item.getPictureType());
                    return coverPicture;
                })
                .collect(Collectors.toList());
        musicMetaInfo.setCoverPictures(coverPictures);
        return musicMetaInfo;
    }

//    public static Image getFlacPicture(String flacpath) {
//        try {
//
//            AudioFileReader fileReader = new MP3FileReader();
//
//
//
//            AudioFile read = fileReader.read(new File(flacpath));
//            org.jaudiotagger.tag.Tag tag = read.getTag();
//            String first = tag.getFirst(FieldKey.TITLE);
//            System.out.println(first);
//            Iterator<TagField> fields = tag.getFields();
//            while (fields.hasNext()) {
//                TagField next = fields.next();
//                System.out.println(next.getId() + "::" + next);
//            }
//            Artwork firstArtwork = tag.getFirstArtwork();
//            byte[] imageData = firstArtwork.getBinaryData();
//            Image image = Toolkit.getDefaultToolkit().createImage(imageData, 0, imageData.length);
//            ImageIcon icon = new ImageIcon(image);
//            String storePath = flacpath;
//            storePath = storePath.substring(0, storePath.length() - 4);
//            storePath += "jpg";
//            System.out.println(storePath);
//            FileOutputStream fos = new FileOutputStream(storePath);
//            fos.write(imageData);
//            fos.close();
//            return image;
//        } catch (Exception e) {
//            e.printStackTrace();
//            throw new RuntimeException("读取Flac图片时出错！");
//        }
//    }
}
