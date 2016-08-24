package psd.api;

import java.io.Serializable;

public class Command implements Serializable {

    private CommandType type;
    private User user;
    private File file;
    private Role role;
    private User newUser;
    private String targetUserName;
    private String targetRoleName;
    private Permission permission;

    public Command(CommandType type, User user, File file) {
        this.type = type;
        this.user = user;
        this.file = file;
    }

    public Command(CommandType type, User user, Role role) {
        this.type = type;
        this.user = user;
        this.role = role;
    }

    public Command(CommandType type, User user, User newUser) {
        this.type = type;
        this.user = user;
        this.newUser = newUser;
    }

    public Command(CommandType type, User user, File file, Role role) {
        this.type = type;
        this.user = user;
        this.file = file;
        this.role = role;
    }

    public Command(CommandType type, User user, String targetUserName, String targetRoleName) {
        this.type = type;
        this.user = user;
        this.targetUserName = targetUserName;
        this.targetRoleName = targetRoleName;
    }

    public Command(CommandType type, User user, Permission permission) {
        this.type = type;
        this.user = user;
        this.permission = permission;
    }

    public Command(CommandType type, User user, Role role, Permission permission) {
        this.type = type;
        this.user = user;
        this.role = role;
        this.permission = permission;
    }

    public User getUser() {
        return user;
    }

    public File getFile() {
        return file;
    }

    public CommandType getType() {
        return type;
    }

    public Role getRole() {
        return role;
    }

    public User getNewUser() {
        return newUser;
    }

    public String getTargetUserName() {
        return targetUserName;
    }

    public String getTargetRoleName() {
        return targetRoleName;
    }

    public Permission getPermission() {
        return permission;
    }
}
