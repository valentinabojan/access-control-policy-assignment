package psd.client;

import psd.api.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import static psd.api.Command.CommandBuilder.command;
import static psd.api.CommandType.*;
import static psd.api.File.FileBuilder.file;
import static psd.api.Role.RoleBuilder.role;
import static psd.api.User.UserBuilder.user;

public class Client {

    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void connect(String hostName, Integer portNumber) throws IOException {
        Socket socket = new Socket(hostName, portNumber);

        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public Response createResource(String userName, String userPassword, String resourceName, int resourceType, String resourceValue) {
        User user = user().withUsername(userName).withPassword(userPassword).build();
        File file = file().withName(resourceName).withValue(resourceValue).withType(FileType.fromInteger(resourceType)).build();

        try {
            out.writeObject(command().withType(CommandType.CREATE_RESOURCE).withUser(user).withFile(file).build());
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Response readResource(String userName, String userPassword, String resourceName) {
        User user = user().withUsername(userName).withPassword(userPassword).build();
        File file = file().withName(resourceName).build();

        try {
            out.writeObject(command().withType(CommandType.READ_RESOURCE).withUser(user).withFile(file).build());
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Response writeResource(String userName, String userPassword, String resourceName, String resourceValue) {
        User user = user().withUsername(userName).withPassword(userPassword).build();
        File file = file().withName(resourceName).withValue(resourceValue).build();

        try {
            out.writeObject(command().withType(CommandType.WRITE_RESOURCE).withUser(user).withFile(file).build());
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Response createRole(String userName, String userPassword, String roleName) {
        User user = user().withUsername(userName).withPassword(userPassword).build();
        Role role = role().withName(roleName).withRights("").build();

        try {
            out.writeObject(command().withType(CREATE_ROLE).withUser(user).withRole(role).build());
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Response createUser(String userName, String userPassword, String newUserName, String newUserPassword) {
        User user = user().withUsername(userName).withPassword(userPassword).build();
        User newUser = user().withUsername(newUserName).withPassword(newUserPassword).build();

        try {
            out.writeObject(command().withType(CREATE_USER).withUser(user).withTargetUser(newUser).build());
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Response changeRights(String userName, String userPassword, String roleName, String rights) {
        User user = user().withUsername(userName).withPassword(userPassword).build();
        Role role = role().withName(roleName).withRights(rights).build();

        try {
            out.writeObject(command().withType(CHANGE_RIGHTS).withUser(user).withRole(role).build());
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Response assignRole(String userName, String userPassword, String targetUserName, String targetRoleName) {
        User user = user().withUsername(userName).withPassword(userPassword).build();
        User targetUser = user().withUsername(targetUserName).build();
        Role newRole = role().withName(targetRoleName).build();

        try {
            out.writeObject(command().withType(ASSIGN_ROLE).withUser(user).withTargetUser(targetUser).withRole(newRole).build());
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Response addRights(String userName, String userPassword, String resourceName, String roleName) {
        User user = user().withUsername(userName).withPassword(userPassword).build();
        File file = file().withName(resourceName).build();
        Role role = role().withName(roleName).build();

        try {
            out.writeObject(command().withType(ADD_RIGHTS).withUser(user).withFile(file).withRole(role).build());
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
