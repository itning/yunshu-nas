package top.itning.yunshunas.video.entity;

import lombok.Data;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * @author itning
 * @since 2019/7/16 23:46
 */
@Data
public class Link {
    private static final String WINDOWS_SYSTEM = "win";

    private String name;
    private String link;
    private static final String SPLIT_REGEX;

    static {
        String os = System.getProperty("os.name");
        if (os.toLowerCase().startsWith(WINDOWS_SYSTEM)) {
            SPLIT_REGEX = "\\\\";
        } else {
            SPLIT_REGEX = "/";
        }
    }

    public static List<Link> build(String location) throws UnsupportedEncodingException {
        byte[] decode = Base64.getUrlDecoder().decode(location.getBytes(StandardCharsets.UTF_8));
        String decodeLocation = new String(decode, StandardCharsets.UTF_8);
        String[] locationArray = decodeLocation.split(SPLIT_REGEX);
        List<Link> linkList = new ArrayList<>(locationArray.length);
        if (locationArray.length == 0) {
            Link link = new Link();
            link.setName(decodeLocation);
            link.setLink(location);
            linkList.add(link);
            return linkList;
        }
        StringBuilder last = new StringBuilder(locationArray[0]);
        for (int i = 0; i < locationArray.length; i++) {
            Link link = new Link();
            link.setName(locationArray[i]);
            link.setLink(Base64.getUrlEncoder().encodeToString(last.toString().getBytes(StandardCharsets.UTF_8)));
            if ((i + 1) != locationArray.length) {
                last.append(File.separator).append(locationArray[i + 1]);
            }
            linkList.add(link);
        }
        return linkList;
    }
}
