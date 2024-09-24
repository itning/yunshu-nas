package top.itning.yunshunas.music.webdav;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

/**
 * @author itning
 * @since 2024/9/24 19:39
 */
@Data
public abstract class Entry {
    private String parent;
    private String path;
    private String name;


    boolean isRoot() {
        return this.getPath().equals("/");
    }

    boolean isFolder() {
        return this instanceof Folder;
    }

    boolean isFile() {
        return this instanceof File;
    }

    Date getCreationDate() {
        return new Date();
    }

    Date getLastModified() {
        return this.getCreationDate();
    }

    // The identifier or ETag are based on the build datetime of the
    // application, assuming the data must be incompatible after an update.
    // However, for use in scalable environments, the datetime must have a
    // common fixed point.

    String getIdentifier() {
        Date lastModified = this.getLastModified();
        if (Objects.isNull(lastModified)) {
            lastModified = new Date();
        }
        return (Long.toString(lastModified.getTime(), 16));
    }

    boolean isHidden() {
        return false;
    }

    boolean isReadOnly() {
        return true;
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class Folder extends Entry {

        private final Collection<Entry> collection = new ArrayList<>();

        @Override
        boolean isHidden() {
            for (final Entry entry : this.getCollection()) {
                if (!entry.isHidden()) {
                    return false;
                }
            }
            return !this.isRoot();
        }
    }

    @EqualsAndHashCode(callSuper = true)
    @Data
    public static class File extends Entry {
        private Long contentLength;
        private String contentType;
        private Date lastModified;
        private Date creationDate;
        private boolean readOnly;
        private boolean hidden;
        private boolean permitted;

        @Override
        boolean isReadOnly() {
            if (!this.isPermitted()) {
                return true;
            }
            return readOnly;
        }

        @Override
        boolean isHidden() {
            if (!this.isPermitted()) {
                return true;
            }
            return hidden;
        }
    }
}
