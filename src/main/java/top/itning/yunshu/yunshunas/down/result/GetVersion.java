package top.itning.yunshu.yunshunas.down.result;

import top.itning.yunshu.yunshunas.down.RpcResultType;

import java.util.List;

/**
 * 此方法返回aria2的版本和已启用功能的列表。
 *
 * @author itning
 * @date 2019/7/17 14:42
 */
public class GetVersion implements RpcResultType {
    /**
     * 已启用的功能列表。每个功能都以字符串形式给出。
     */
    private List<String> enabledFeatures;
    /**
     * aria2的版本号为字符串。
     */
    private String version;

    public List<String> getEnabledFeatures() {
        return enabledFeatures;
    }

    public void setEnabledFeatures(List<String> enabledFeatures) {
        this.enabledFeatures = enabledFeatures;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return "GetVersion{" +
                "enabledFeatures=" + enabledFeatures +
                ", version='" + version + '\'' +
                '}';
    }
}
