package client;

import api.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import static api.Command.CommandBuilder.command;

public class Client {

    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void connect(String hostName, Integer portNumber) throws IOException {
        Socket socket = new Socket(hostName, portNumber);

        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public Response createResource(String userName, String userPassword, String resourceName, int resourceType, String resourceValue) {
        return sendCommand(
                command()
                        .withType(CommandType.CREATE_RESOURCE)
                        .withUser(new User(userName, userPassword))
                        .withFile(new File(resourceName, resourceValue, FileType.fromInteger(resourceType)))
                        .build()
        );
    }

    public Response readResource(String userName, String userPassword, String resourceName) {
        return sendCommand(
                command()
                        .withType(CommandType.READ_RESOURCE)
                        .withUser(new User(userName, userPassword))
                        .withFile(new File(resourceName))
                        .build()
        );
    }

    public Response writeResource(String userName, String userPassword, String resourceName, String resourceValue) {
        return sendCommand(
                command()
                        .withType(CommandType.WRITE_RESOURCE)
                        .withUser(new User(userName, userPassword))
                        .withFile(new File(resourceName, resourceValue)).build()
        );
    }

    public Response changeRights(String userName, String userPassword, String resourceName, String resourcePermissions) {
        Set<FilePermission> filePermissions = new HashSet<>();
        if (resourcePermissions.contains("r"))
            filePermissions.add(FilePermission.READ);
        if (resourcePermissions.contains("w"))
            filePermissions.add(FilePermission.WRITE);

        return sendCommand(
                command()
                        .withType(CommandType.CHANGE_RIGHTS)
                        .withUser(new User(userName, userPassword))
                        .withFile(new File(resourceName, filePermissions))
                        .build());
    }

    private Response sendCommand(Command command) {
        try {
            out.writeObject(command);
            out.flush();
            return (Response) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }
}
