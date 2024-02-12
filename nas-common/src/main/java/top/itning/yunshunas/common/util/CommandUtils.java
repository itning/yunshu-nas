package top.itning.yunshunas.common.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.function.Consumer;

/**
 * 命令行工具类
 *
 * @author itning
 * @since 2019/7/17 20:56
 */
public class CommandUtils {
    private CommandUtils() {
        throw new UnsupportedOperationException();
    }

    /**
     * 执行命令
     *
     * @param command     命令
     * @param commandInfo 输出信息
     * @throws IOException IOException
     */
    public static void process(List<String> command, Consumer<String> commandInfo) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();

        InputStream inputStream = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr);

        try (inputStream; isr; br) {
            String line;
            while ((line = br.readLine()) != null) {
                commandInfo.accept(line);
            }
        }
    }

    public static String process(List<String> command) throws IOException {
        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();

        InputStream inputStream = process.getInputStream();
        InputStreamReader isr = new InputStreamReader(inputStream);
        BufferedReader br = new BufferedReader(isr);

        StringBuilder sb = new StringBuilder();
        try (inputStream; isr; br) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}
