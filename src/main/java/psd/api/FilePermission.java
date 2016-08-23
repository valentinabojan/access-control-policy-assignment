package psd.api;

public enum FilePermission {
    READ("r"),
    WRITE("w"),
    READ_WRITE("rw");

    private final String permission;

    FilePermission(String permission) {
        this.permission = permission;
    }

    public String getPermission() {
        return permission;
    }
}
