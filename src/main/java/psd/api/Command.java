package psd.api;

import java.io.Serializable;

public class Command implements Serializable {

    private CommandType type;
    private User user;
    private File file;
    private Role role;
    private String targetUserName;
    private String targetRoleName;

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

    public String getTargetUserName() {
        return targetUserName;
    }

    public String getTargetRoleName() {
        return targetRoleName;
    }


}
