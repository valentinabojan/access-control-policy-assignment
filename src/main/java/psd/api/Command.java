package psd.api;

import java.io.Serializable;

public class Command implements Serializable {

    private CommandType type;
    private User user;
    private File file;
    private Role role;
    private User targetUser;

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

    public User getTargetUser() {
        return targetUser;
    }

    public static class CommandBuilder {
        private Command command;

        private CommandBuilder() {
            command = new Command();
        }

        public static CommandBuilder command() {
            return new CommandBuilder();
        }

        public CommandBuilder withType(CommandType type) {
            command.type = type;
            return this;
        }

        public CommandBuilder withUser(User user) {
            command.user = user;
            return this;
        }

        public CommandBuilder withFile(File file) {
            command.file = file;
            return this;
        }

        public CommandBuilder withRole(Role role) {
            command.role = role;
            return this;
        }

        public CommandBuilder withTargetUser(User targetUser) {
            command.targetUser = targetUser;
            return this;
        }

        public Command build() {
            return command;
        }
    }
}
