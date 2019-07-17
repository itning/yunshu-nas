package top.itning.yunshu.yunshunas.down.option;

import java.util.HashMap;

/**
 * @author itning
 * @date 2019/7/17 15:58
 */
public class Options extends HashMap<String, String> {
    public Options putOne(String key, String value) {
        this.put(key, value);
        return this;
    }

    public Options put(Enum optionsEnum, String value) {
        return putOne(optionsEnum.key, value);
    }

    public enum Enum {
        /**
         * 存储下载文件的目录。
         */
        dir("dir"),
        /**
         * 每次下载到一台服务器的最大连接数。默认：1
         */
        maxConnectionPerServer("max-connection-per-server"),
        /**
         * 下载文件的文件名
         */
        out("out");

        private String key;

        Enum(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }
}
