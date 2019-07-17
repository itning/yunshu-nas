package top.itning.yunshu.yunshunas.down;

import com.google.gson.annotations.SerializedName;

/**
 * <a href="https://aria2.github.io/manual/en/html/aria2c.html#methods">doc</a>
 *
 * @author itning
 * @date 2019/7/17 13:58
 */
public enum RpcMethods {
    /**
     * 添加新的下载
     * 此方法添加了新的下载。
     * uris是指向同一资源的HTTP / FTP / SFTP / BitTorrent URI（字符串）数组。
     * 如果混合指向不同资源的URI，则下载可能会失败或在没有aria2抱怨的情况下被破坏。
     * 添加BitTorrent Magnet URI时，uris必须只有一个元素，它应该是BitTorrent Magnet URI。
     * options是一个结构，它的成员是选项名称和值的对。
     * 请参阅 下面的选项以获取更多详
     * 如果给定位置，则它必须是从0开始的整数。
     * 新下载将插入 到等待队列中的位置。
     * 如果位置被省略或 位置如果大于队列的当前大小，则新下载将附加到队列的末尾。
     * 此方法返回新注册的下载的GID
     */
    @SerializedName("aria2.addUri")
    addUri("aria2.addUri"),
    /**
     * 此方法通过上载“.torrent”文件添加BitTorrent下载。
     */
    @SerializedName("aria2.addTorrent")
    addTorrent("aria2.addTorrent"),
    /**
     * 此方法通过上载“.metalink”文件添加Metalink下载。
     * metalink是base64编码的字符串，其中包含“.metalink”文件的内容。
     */
    @SerializedName("aria2.addMetalink")
    addMetalink("aria2.addMetalink"),
    /**
     * 此方法删除由gid（字符串）表示的下载。
     * 如果指定的下载正在进行中，则首先停止。删除下载的状态变为removed。此方法返回已删除下载的GID。
     */
    @SerializedName("aria2.remove")
    remove("aria2.remove"),
    /**
     * 此方法删除gid表示的下载。此方法的行为与此方法类似，
     * aria2.remove()只是此方法删除下载而不执行任何需要花费时间的操作，例如联系BitTorrent跟踪器以首先取消注册下载。
     */
    @SerializedName("aria2.forceRemove")
    forceRemove("aria2.forceRemove"),
    /**
     * 此方法暂停由gid（字符串）表示的下载。暂停下载的状态变为paused。如果下载处于活动状态，则下载将置于等待队列的前面。
     * 状态是paused，下载未启动。要将状态更改为waiting，请使用该 aria2.unpause()方法。此方法返回暂停下载的GID。
     */
    @SerializedName("aria2.pause")
    pause("aria2.pause"),
    /**
     * 此方法等于调用aria2.pause()每个活动/等待下载。这种方法返回OK。
     */
    @SerializedName("aria2.pauseAll")
    pauseAll("aria2.pauseAll"),
    /**
     * 此方法暂停gid表示的下载。此方法的行为与此方法类似，
     * aria2.pause()只是此方法暂停下载而不执行任何需要时间的操作，例如联系BitTorrent跟踪器以首先取消注册下载。
     */
    @SerializedName("aria2.forcePause")
    forcePause("aria2.forcePause"),
    /**
     * 此方法等于调用aria2.forcePause()每个活动/等待下载。这种方法返回OK。
     */
    @SerializedName("aria2.forcePauseAll")
    forcePauseAll("aria2.forcePauseAll"),
    /**
     * 此方法将gid（字符串） 表示的下载状态更改paused为waiting，使下载符合重新启动条件。此方法返回未暂停下载的GID。
     */
    @SerializedName("aria2.unpause")
    unpause("aria2.unpause"),
    /**
     * 此方法等于调用aria2.unpause()每个暂停的下载。这种方法返回OK。
     */
    @SerializedName("aria2.unpauseAll")
    unpauseAll("aria2.unpauseAll"),
    /**
     * 此方法返回由gid（字符串）表示的下载进度。
     */
    @SerializedName("aria2.tellStatus")
    tellStatus("aria2.tellStatus"),
    /**
     * 此方法返回由gid（字符串）表示的下载中使用的URI 。响应是一个结构数组，它包含以下键。值是字符串。
     */
    @SerializedName("aria2.getUris")
    getUris("aria2.getUris"),
    /**
     * 此方法返回由gid（字符串）表示的下载文件列表。响应是包含以下键的结构数组。值是字符串。
     */
    @SerializedName("aria2.getFiles")
    getFiles("aria2.getFiles"),
    /**
     * 此方法返回由gid（字符串）表示的下载的列表对等项。此方法仅适用于BitTorrent。
     * 响应是一个结构数组，包含以下键。值是字符串。
     */
    @SerializedName("aria2.getPeers")
    getPeers("aria2.getPeers"),
    /**
     * 此方法返回由gid（字符串）表示的下载的当前连接的HTTP（S）/ FTP / SFTP服务器。
     * 响应是一个结构数组，包含以下键。值是字符串。
     */
    @SerializedName("aria2.getServers")
    getServers("aria2.getServers"),
    /**
     * 此方法返回活动下载列表。响应是由aria2.tellStatus()方法返回的相同结构的数组。
     * 有关keys参数，请参阅aria2.tellStatus()方法。
     */
    @SerializedName("aria2.tellActive")
    tellActive("aria2.tellActive"),
    /**
     * 此方法返回等待下载列表，包括暂停的下载列表。 offset是一个整数，指定从前面等待下载的偏移量。
     * num是一个整数，指定最大值。要返回的下载次数。有关keys参数，请参阅aria2.tellStatus()方法。
     */
    @SerializedName("aria2.tellWaiting")
    tellWaiting("aria2.tellWaiting"),
    /**
     * 此方法返回已停止下载的列表。
     */
    @SerializedName("aria2.tellStopped")
    tellStopped("aria2.tellStopped"),
    /**
     * 此方法更改队列中gid表示的下载位置 。
     */
    @SerializedName("aria2.changePosition")
    changePosition("aria2.changePosition"),
    /**
     * 此方法删除的URI的delUris从与附加在URI的 addUris下载由表示GID。
     */
    @SerializedName("aria2.changeUri")
    changeUri("aria2.changeUri"),
    /**
     * 此方法返回gid表示的下载选项。
     */
    @SerializedName("aria2.getOption")
    getOption("aria2.getOption"),
    /**
     * 此方法动态更改由gid（字符串）表示的下载选项。
     */
    @SerializedName("aria2.changeOption")
    changeOption("aria2.changeOption"),
    /**
     * 此方法返回全局选项。
     */
    @SerializedName("aria2.getGlobalOption")
    getGlobalOption("aria2.getGlobalOption"),
    /**
     * 此方法动态更改全局选项。
     */
    @SerializedName("aria2.changeGlobalOption")
    changeGlobalOption("aria2.changeGlobalOption"),
    /**
     * 此方法返回全局统计信息，例如整体下载和上载速度。响应是一个结构，包含以下键。值是字符串。
     */
    @SerializedName("aria2.getGlobalStat")
    getGlobalStat("aria2.getGlobalStat"),
    /**
     * 此方法清除已完成/错误/删除的下载以释放内存。此方法返回OK。
     */
    @SerializedName("aria2.purgeDownloadResult")
    purgeDownloadResult("aria2.purgeDownloadResult"),
    /**
     * 此方法 从内存中删除由gid表示的已完成/错误/删除的下载。此方法返回OK成功。
     */
    @SerializedName("aria2.removeDownloadResult")
    removeDownloadResult("aria2.removeDownloadResult"),
    /**
     * 此方法返回aria2的版本和已启用功能的列表。
     */
    @SerializedName("aria2.getVersion")
    getVersion("aria2.getVersion"),
    /**
     * 此方法返回会话信息。
     */
    @SerializedName("aria2.getSessionInfo")
    getSessionInfo("aria2.getSessionInfo"),
    /**
     * 这种方法关闭了aria2。此方法返回OK。
     */
    @SerializedName("aria2.shutdown")
    shutdown("aria2.shutdown"),
    /**
     * 这种方法关闭了aria2()。此方法的行为类似于：func：'aria2.shutdown`，
     * 不执行任何需要时间的操作，例如联系BitTorrent跟踪器以首先取消注册下载。此方法返回OK。
     */
    @SerializedName("aria2.forceShutdown")
    forceShutdown("aria2.forceShutdown"),
    /**
     * 此方法将当前会话保存到该--save-session选项指定的文件中 。OK如果成功，则返回此方法。
     */
    @SerializedName("aria2.saveSession")
    saveSession("aria2.saveSession"),
    /**
     * 此方法在单个请求中封装多个方法调用。
     */
    @SerializedName("system.multicall")
    multicall("system.multicall"),
    /**
     * 此方法返回字符串数组中的所有可用RPC方法。
     */
    @SerializedName("system.listMethods")
    listMethods("system.listMethods"),
    /**
     * 此方法返回字符串数组中的所有可用RPC通知。
     */
    @SerializedName("system.listNotifications")
    listNotifications("system.listNotifications");

    private String method;

    RpcMethods(String method) {
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
