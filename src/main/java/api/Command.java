package api;

import java.io.Serializable;

public class Command implements Serializable {

    private CommandType type;
    private User user;
    private File file;

    public Command(CommandType type, User user, File file) {
        this.type = type;
        this.user = user;
        this.file = file;
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
}
