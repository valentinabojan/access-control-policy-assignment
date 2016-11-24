package api;

import api.entities.Permission;
import api.entities.Role;
import api.entities.User;

import java.io.Serializable;

public class Command implements Serializable {

    private CommandType type;
    private File file;
    private Role role;
    private Role targetRole;
    private User user;
    private User targetUser;
    private Permission permission;

    public CommandType getType() {
        return type;
    }

    public File getFile() {
        return file;
    }

    public Role getRole() {
        return role;
    }

    public Role getTargetRole() {
        return targetRole;
    }

    public User getUser() {
        return user;
    }

    public User getTargetUser() {
        return targetUser;
    }

    public Permission getPermission() {
        return permission;
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

        public CommandBuilder withFile(File file) {
            command.file = file;
            return this;
        }

        public CommandBuilder withRole(Role role) {
            command.role = role;
            return this;
        }

        public CommandBuilder withTargetRole(Role targetRole) {
            command.targetRole = targetRole;
            return this;
        }

        public CommandBuilder withUser(User user) {
            command.user = user;
            return this;
        }

        public CommandBuilder withTargetUser(User targetUser) {
            command.targetUser = targetUser;
            return this;
        }

        public CommandBuilder withPermission(Permission permission) {
            command.permission = permission;
            return this;
        }

        public Command build() {
            return command;
        }
    }
}
