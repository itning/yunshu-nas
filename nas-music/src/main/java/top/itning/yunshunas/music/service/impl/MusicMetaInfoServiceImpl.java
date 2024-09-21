package top.itning.yunshunas.music.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.flac.FlacFileReader;
import org.jaudiotagger.audio.generic.AudioFileReader;
import org.jaudiotagger.audio.mp3.MP3FileReader;
import org.jaudiotagger.audio.wav.WavFileReader;
import org.jaudiotagger.tag.*;
import org.jaudiotagger.tag.datatype.Artwork;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import top.itning.yunshunas.music.constant.MusicType;
import top.itning.yunshunas.music.dto.MusicMetaInfo;
import top.itning.yunshunas.music.service.MusicMetaInfoService;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.List;

/**
 * @author itning
 * @since 2021/6/1 16:38
 */
@Slf4j
@Service
public class MusicMetaInfoServiceImpl implements MusicMetaInfoService {

    @Override
    public MusicMetaInfo metaInfo(File musicFile, MusicType musicType) throws Exception {
        AudioFile read = getAudioFile(musicFile, musicType);
        if (read == null) return null;
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
                                if (it instanceof TagTextField tagTextField) {
                                    return tagTextField.getContent();
                                }
                                return it.toString();
                            })
                            .toList()
            );
        }
        String album = tag.getFirst(FieldKey.ALBUM);
        musicMetaInfo.setTitle(title);
        musicMetaInfo.setAlbum(album);

        List<MusicMetaInfo.CoverPicture> coverPictures = tag.getArtworkList()
                .stream()
                .map(item -> {
                    MusicMetaInfo.CoverPicture coverPicture = new MusicMetaInfo.CoverPicture();
                    byte[] encoded = Base64.getEncoder().encode(item.getBinaryData());
                    coverPicture.setBinaryData(item.getBinaryData());
                    coverPicture.setBase64(new String(encoded));
                    coverPicture.setMimeType(item.getMimeType());
                    coverPicture.setDescription(item.getDescription());
                    coverPicture.setLinked(item.isLinked());
                    coverPicture.setImageUrl(item.getImageUrl());
                    coverPicture.setPictureType(item.getPictureType());
                    return coverPicture;
                })
                .toList();
        musicMetaInfo.setCoverPictures(coverPictures);
        return musicMetaInfo;
    }

    private static AudioFile getAudioFile(File musicFile, MusicType musicType) throws CannotReadException, IOException, TagException, ReadOnlyFileException, InvalidAudioFrameException {
        AudioFileReader fileReader;
        switch (musicType) {
            case FLAC -> fileReader = new FlacFileReader();
            case MP3 -> fileReader = new MP3FileReader();
            case WAV -> fileReader = new WavFileReader();
            default -> {
                log.warn("不支持的音乐类型：{}", musicType);
                return null;
            }
        }
        return fileReader.read(musicFile);
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

    public void editMetaInfo(File musicFile, MusicType musicType, MusicMetaInfo musicMetaInfo) throws Exception {
        boolean needRemoveExt = false;
        String filenameExtension = org.springframework.util.StringUtils.getFilenameExtension(musicFile.getPath());
        if (null == filenameExtension) {
            needRemoveExt = true;
            File tempFile = new File(musicFile.getPath() + "." + musicType.getExt());
            if (tempFile.exists()) {
                log.warn("临时文件{}已存在，删除结果：{}", tempFile, tempFile.delete());
            }
            if (!musicFile.renameTo(tempFile)) {
                throw new RuntimeException("重命名文件失败 源文件：" + musicFile + " 目标：" + tempFile);
            }
            musicFile = tempFile;
        }
        AudioFile audioFile = getAudioFile(musicFile, musicType);
        if (null == audioFile) {
            return;
        }
        Tag tag = audioFile.getTag();

        if (StringUtils.isNotBlank(musicMetaInfo.getTitle())) {
            tag.setField(FieldKey.TITLE, musicMetaInfo.getTitle());
        }

        if (!CollectionUtils.isEmpty(musicMetaInfo.getArtists())) {
            tag.deleteField(FieldKey.ARTIST);
            for (String artist : musicMetaInfo.getArtists()) {
                tag.addField(FieldKey.ARTIST, artist);
            }
        }

        if (StringUtils.isNotBlank(musicMetaInfo.getAlbum())) {
            tag.setField(FieldKey.ALBUM, musicMetaInfo.getAlbum());
        }

        if (!CollectionUtils.isEmpty(musicMetaInfo.getCoverPictures())) {
            tag.deleteArtworkField();
            for (MusicMetaInfo.CoverPicture coverPicture : musicMetaInfo.getCoverPictures()) {
                Artwork artwork = Artwork.createArtworkFromFile(coverPicture.getFile());
                if (StringUtils.isNotBlank(coverPicture.getMimeType())) {
                    artwork.setMimeType(coverPicture.getMimeType());
                }
                tag.addField(artwork);
            }
        }

        audioFile.commit();

        if (needRemoveExt) {
            File target = new File(org.springframework.util.StringUtils.stripFilenameExtension(musicFile.getPath()));
            if (!musicFile.renameTo(target)) {
                throw new RuntimeException("重命名文件失败 源文件：" + musicFile + " 目标：" + target);
            }
        }
    }
}
