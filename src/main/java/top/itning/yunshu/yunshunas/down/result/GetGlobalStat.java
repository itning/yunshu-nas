package top.itning.yunshu.yunshunas.down.result;

import top.itning.yunshu.yunshunas.down.RpcResultType;

/**
 * 此方法返回全局统计信息，例如整体下载和上载速度。
 *
 * @author itning
 * @date 2019/7/17 15:13
 */
public class GetGlobalStat implements RpcResultType {
    /**
     * 总体下载速度（字节/秒）。
     */
    private long downloadSpeed;
    /**
     * 活动下载次数。
     */
    private long numActive;
    /**
     * 当前会话中停止下载的次数。该值受--max-download-result选项限制。
     */
    private long numStopped;
    /**
     * 当前会话中已停止下载的数量，并且不受 该--max-download-result选项的限制。
     */
    private long numStoppedTotal;
    /**
     * 等待下载的次数。
     */
    private long numWaiting;
    /**
     * 总上传速度（字节/秒）。
     */
    private long uploadSpeed;

    public long getDownloadSpeed() {
        return downloadSpeed;
    }

    public void setDownloadSpeed(long downloadSpeed) {
        this.downloadSpeed = downloadSpeed;
    }

    public long getNumActive() {
        return numActive;
    }

    public void setNumActive(long numActive) {
        this.numActive = numActive;
    }

    public long getNumStopped() {
        return numStopped;
    }

    public void setNumStopped(long numStopped) {
        this.numStopped = numStopped;
    }

    public long getNumStoppedTotal() {
        return numStoppedTotal;
    }

    public void setNumStoppedTotal(long numStoppedTotal) {
        this.numStoppedTotal = numStoppedTotal;
    }

    public long getNumWaiting() {
        return numWaiting;
    }

    public void setNumWaiting(long numWaiting) {
        this.numWaiting = numWaiting;
    }

    public long getUploadSpeed() {
        return uploadSpeed;
    }

    public void setUploadSpeed(long uploadSpeed) {
        this.uploadSpeed = uploadSpeed;
    }

    @Override
    public String toString() {
        return "GetGlobalStat{" +
                "downloadSpeed=" + downloadSpeed +
                ", numActive=" + numActive +
                ", numStopped=" + numStopped +
                ", numStoppedTotal=" + numStoppedTotal +
                ", numWaiting=" + numWaiting +
                ", uploadSpeed=" + uploadSpeed +
                '}';
    }
}
