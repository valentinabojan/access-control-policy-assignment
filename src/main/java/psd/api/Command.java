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

    public Command(CommandType type, User user, String targetUserName, String targetRoleName) {
        this.type = type;
        this.user = user;
        this.targetUserName = targetUserName;
        this.targetRoleName = targetRoleName;
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
}
