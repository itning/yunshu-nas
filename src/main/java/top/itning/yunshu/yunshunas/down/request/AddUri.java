package top.itning.yunshu.yunshunas.down.request;

import top.itning.yunshu.yunshunas.down.option.Options;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 此方法添加了新的下载。
 * uris是指向同一资源的HTTP / FTP / SFTP / BitTorrent URI（字符串）数组。
 * 如果混合指向不同资源的URI，则下载可能会失败或在没有aria2抱怨的情况下被破坏。
 * 添加BitTorrent Magnet URI时，uris必须只有一个元素，它应该是BitTorrent Magnet URI。
 * options是一个结构，它的成员是选项名称和值的对。
 * 请参阅 下面的选项以获取更多详 如果给定位置，则它必须是从0开始的整数。新下载将插入 到等待队列中的位置。
 * 如果位置被省略或 位置如果大于队列的当前大小，则新下载将附加到队列的末尾。此方法返回新注册的下载的GID。
 *
 * @author itning
 * @date 2019/7/17 16:08
 */
public class AddUri {
    private final List<Object> list;

    public AddUri(String uri, Options options) {
        this.list = new ArrayList<>();
        this.list.add(Collections.singletonList(uri));
        this.list.add(options);
    }

    public AddUri(String uri) {
        this.list = new ArrayList<>();
        this.list.add(Collections.singletonList(uri));
    }

    public List<Object> build() {
        return list;
    }

    @Override
    public String toString() {
        return "AddUri{" +
                "list=" + list +
                '}';
    }
}
