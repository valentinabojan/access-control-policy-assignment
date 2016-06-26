package api;

import java.io.Serializable;
import java.util.List;

/**
 * Created by valen_000 on 6/11/2016.
 */
public class File implements Serializable {
    private String name;
    private String value;
    private List<FilePermission> permissions;
    private FileType type;

    public File(String name, String value, List<FilePermission> permissions, FileType type) {
        this.name = name;
        this.value = value;
        this.permissions = permissions;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<FilePermission> getPermission() {
        return permissions;
    }

    public void setPermission(List<FilePermission> permissions) {
        this.permissions = permissions;
    }

    public FileType getType() {
        return type;
    }

    public void setType(FileType type) {
        this.type = type;
    }
}
