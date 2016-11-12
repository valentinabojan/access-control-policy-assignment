package client;

import api.Command;
import api.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Optional;

import static api.Command.CommandBuilder.command;
import static api.CommandType.*;
import static api.File.FileBuilder.file;
import static api.FileType.fromInteger;
import static api.Role.RoleBuilder.role;
import static api.User.UserBuilder.user;

public class Client {

    private ObjectOutputStream out;
    private ObjectInputStream in;

    public void connect(String hostName, Integer portNumber) throws IOException {
        Socket socket = new Socket(hostName, portNumber);

        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public Response createResource(String userName, String userPassword, String resourceName, int resourceType, Optional<String> resourceValue) {
        return sendCommand(
                command()
                        .withType(CREATE_RESOURCE)
                        .withUser(user().withUsername(userName).withPassword(userPassword).build())
                        .withFile(file().withName(resourceName).withValue(resourceValue.orElse(null)).withType(fromInteger(resourceType)).build())
                        .build()
        );
    }

    public Response readResource(String userName, String userPassword, String resourceName) {
        return sendCommand(
                command()
                        .withType(READ_RESOURCE)
                        .withUser(user().withUsername(userName).withPassword(userPassword).build())
                        .withFile(file().withName(resourceName).build())
                        .build()
        );
    }

    public Response writeResource(String userName, String userPassword, String resourceName, String resourceValue) {
        return sendCommand(
                command()
                        .withType(WRITE_RESOURCE)
                        .withUser(user().withUsername(userName).withPassword(userPassword).build())
                        .withFile(file().withName(resourceName).withValue(resourceValue).build())
                        .build()
        );
    }

    public Response createRole(String userName, String userPassword, String roleName) {
        return sendCommand(
                command()
                        .withType(CREATE_ROLE)
                        .withUser(user().withUsername(userName).withPassword(userPassword).build())
                        .withRole(role().withName(roleName).withRights("").build())
                        .build()
        );
    }

    public Response createUser(String userName, String userPassword, String newUserName, String newUserPassword) {
        return sendCommand(
                command()
                        .withType(CREATE_USER)
                        .withUser(user().withUsername(userName).withPassword(userPassword).build())
                        .withTargetUser(user().withUsername(newUserName).withPassword(newUserPassword).build())
                        .build()
        );
    }

    public Response changeRights(String userName, String userPassword, String roleName, String rights) {
        return sendCommand(
                command()
                        .withType(CHANGE_RIGHTS)
                        .withUser(user().withUsername(userName).withPassword(userPassword).build())
                        .withRole(role().withName(roleName).withRights(rights).build())
                        .build()
        );
    }

    public Response assignRole(String userName, String userPassword, String targetUserName, String targetRoleName) {
        return sendCommand(
                command()
                        .withType(ASSIGN_ROLE)
                        .withUser(user().withUsername(userName).withPassword(userPassword).build())
                        .withTargetUser(user().withUsername(targetUserName).build())
                        .withRole(role().withName(targetRoleName).build())
                        .build()
        );
    }

    public Response addRights(String userName, String userPassword, String resourceName, String roleName) {
        return sendCommand(
                command()
                        .withType(ADD_RIGHTS)
                        .withUser(user().withUsername(userName).withPassword(userPassword).build())
                        .withFile(file().withName(resourceName).build())
                        .withRole(role().withName(roleName).build())
                        .build()
        );
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
