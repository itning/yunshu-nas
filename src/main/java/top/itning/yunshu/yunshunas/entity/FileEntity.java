package top.itning.yunshu.yunshunas.entity;

/**
 * @author itning
 * @date 2019/7/15 11:51
 */
public class FileEntity {
    /**
     * 名
     */
    private String name;
    /**
     * 大小
     */
    private String size;
    /**
     * 是文件
     */
    private boolean file;
    /**
     * 地址
     */
    private String location;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public boolean isFile() {
        return file;
    }

    public void setFile(boolean file) {
        this.file = file;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
