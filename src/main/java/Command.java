import java.io.Serializable;

/**
 * Created by valen_000 on 6/12/2016.
 */
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

    public void setUser(User user) {
        this.user = user;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public CommandType getType() {
        return type;
    }

    public void setType(CommandType type) {
        this.type = type;
    }
}
