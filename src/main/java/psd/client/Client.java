package psd.client;

import psd.api.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import static psd.api.CommandType.*;

public class Client {

    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void connect(String hostName, Integer portNumber) throws IOException {
        Socket socket = new Socket(hostName, portNumber);

        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public Response createResource(String userName, String userPassword, String resourceName, int resourceType, String resourceValue) {
        User user = new User(userName, userPassword);
        File file = new File(resourceName, resourceValue, FileType.fromInteger(resourceType));

        try {
            out.writeObject(new Command(CommandType.CREATE_RESOURCE, user, file));
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Response readResource(String userName, String userPassword, String resourceName) {
        User user = new User(userName, userPassword);
        File file = new File(resourceName);

        try {
            out.writeObject(new Command(CommandType.READ_RESOURCE, user, file));
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Response writeResource(String userName, String userPassword, String resourceName, String resourceValue) {
        User user = new User(userName, userPassword);
        File file = new File(resourceName, resourceValue);

        try {
            out.writeObject(new Command(CommandType.WRITE_RESOURCE, user, file));
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

//    public Response changeRights(String userName, String userPassword, String resourceName, String resourcePermissions) {
//        User user = new User(userName, userPassword);
//
//        Set<FilePermission> filePermissions = new HashSet<>();
//        if (resourcePermissions.contains("r"))
//            filePermissions.add(FilePermission.READ);
//        if (resourcePermissions.contains("w"))
//            filePermissions.add(FilePermission.WRITE);
//
//        File file = new File(resourceName, filePermissions);
//
//        try {
//            out.writeObject(new Command(CHANGE_RIGHTS, user, file));
//            out.flush();
//            return (Response) in.readObject();
//        } catch (IOException | ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        return null;
//    }

    public Response createRole(String userName, String userPassword, String roleName) {
        User user = new User(userName, userPassword);
        Role role = new Role(roleName, "");

        try {
            out.writeObject(new Command(CREATE_ROLE, user, role));
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Response createUser(String userName, String userPassword, String newUserName, String newUserPassword) {
        User user = new User(userName, userPassword);
        User newUser = new User(newUserName, newUserPassword);

        try {
            out.writeObject(new Command(CREATE_USER, user, newUser));
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Response changeRights(String userName, String userPassword, String roleName, String rights) {
        User user = new User(userName, userPassword);
        Role role = new Role(roleName, rights);

        try {
            out.writeObject(new Command(CHANGE_RIGHTS, user, role));
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Response assignRole(String userName, String userPassword, String targetUserName, String targetRoleName) {
        User user = new User(userName, userPassword);

        try {
            out.writeObject(new Command(ASSIGN_ROLE, user, targetUserName, targetRoleName));
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Response addRights(String userName, String userPassword, String resourceName, String roleName) {
        User user = new User(userName, userPassword);
        File file = new File(resourceName);
        Role role = new Role(roleName);

        try {
            out.writeObject(new Command(ADD_RIGHTS, user, file, role));
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}