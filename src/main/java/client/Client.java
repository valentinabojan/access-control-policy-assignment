package client;

import api.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            return (Response)in.readObject();
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
            return (Response)in.readObject();
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
            return (Response)in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Response changeRights(String userName, String userPassword, String resourceName, String resourcePermissions) {
        User user = new User(userName, userPassword);

        Set<FilePermission> filePermissions = new HashSet<>();
        if (resourcePermissions.contains("r"))
            filePermissions.add(FilePermission.READ);
        if (resourcePermissions.contains("w"))
            filePermissions.add(FilePermission.WRITE);

        File file = new File(resourceName, filePermissions);

        try {
            out.writeObject(new Command(CommandType.CHANGE_RIGHTS, user, file));
            out.flush();
            return (Response)in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
