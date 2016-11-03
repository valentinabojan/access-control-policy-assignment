package psd.api;

import java.io.Serializable;
import java.util.Set;

public class File implements Serializable {

    private String name;
    private String value;
    private Set<FilePermission> permissions;
    private FileType type;

    public File(String name, String value, Set<FilePermission> permissions, FileType type) {
        this.name = name;
        this.value = value;
        this.permissions = permissions;
        this.type = type;
    }

    public File(String name, String value, FileType type) {
        this(name, value, null, type);
    }

    public File(String name) {
        this(name, null, null, null);
    }

    public File(String name, String value) {
        this(name, value, null, null);
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public FileType getType() {
        return type;
    }
}
