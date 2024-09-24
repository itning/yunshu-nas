package top.itning.yunshunas.common.util;

import java.util.Arrays;
import java.util.List;

/**
 * @author itning
 * @since 2024/9/24 22:26
 */
public class FileNameValidator {

    // Windows 不允许的字符
    private static final String WINDOWS_FORBIDDEN_CHARS = "<>:\"/\\|?*";

    // Windows 保留名称
    private static final List<String> WINDOWS_RESERVED_NAMES = Arrays.asList(
            "CON", "PRN", "AUX", "NUL",
            "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9",
            "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"
    );

    // 文件名长度限制
    private static final int MAX_LENGTH = 255;

    public static boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        // 长度限制
        if (fileName.length() > MAX_LENGTH) {
            return false;
        }

        return isValidForWindows(fileName) && isValidForUnix(fileName);
    }

    private static boolean isValidForWindows(String fileName) {
        // 检查是否为 Windows 保留名称
        String upperFileName = fileName.toUpperCase();
        if (WINDOWS_RESERVED_NAMES.contains(upperFileName)) {
            return false;
        }

        // 检查不允许的字符
        for (char c : WINDOWS_FORBIDDEN_CHARS.toCharArray()) {
            if (fileName.indexOf(c) >= 0) {
                return false;
            }
        }

        // 文件名不能以空格或句号结尾
        if (fileName.endsWith(" ") || fileName.endsWith(".")) {
            return false;
        }

        return true;
    }

    private static boolean isValidForUnix(String fileName) {
        // Unix-like 系统 (macOS, Linux) 不允许的字符是 '/'
        return !fileName.contains("/");
    }

}
