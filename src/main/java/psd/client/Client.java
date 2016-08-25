package psd.client;

import psd.api.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

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

        return sendCommand(new Command(CommandType.CREATE_RESOURCE, user, file));
    }

    public Response readResource(String userName, String userPassword, String resourceName) {
        User user = new User(userName, userPassword);
        File file = new File(resourceName);

        return sendCommand(new Command(CommandType.READ_RESOURCE, user, file));
    }

    public Response writeResource(String userName, String userPassword, String resourceName, String resourceValue) {
        User user = new User(userName, userPassword);
        File file = new File(resourceName, resourceValue);

        return sendCommand(new Command(CommandType.WRITE_RESOURCE, user, file));
    }

    public Response createRole(String userName, String userPassword, String roleName) {
        User user = new User(userName, userPassword);
        Role role = new Role(roleName, "");

        return sendCommand(new Command(CREATE_ROLE, user, role));
    }

    public Response createUser(String userName, String userPassword, String newUserName, String newUserPassword) {
        User user = new User(userName, userPassword);
        User newUser = new User(newUserName, newUserPassword);

        return sendCommand(new Command(CREATE_USER, user, newUser));
    }

    public Response assignRole(String userName, String userPassword, String targetUserName, String targetRoleName) {
        User user = new User(userName, userPassword);

        return sendCommand(new Command(ASSIGN_ROLE, user, targetUserName, targetRoleName));
    }

    public Response revokeRole(String userName, String userPassword, String targetUserName, String targetRoleName) {
        User user = new User(userName, userPassword);

        return sendCommand(new Command(REVOKE_ROLE, user, targetUserName, targetRoleName));
    }

    public Response assignPermission(String userName, String userPassword, String resourceName, String permissionName) {
        User user = new User(userName, userPassword);
        File file = new File(resourceName);
        Permission permission = new Permission(permissionName);

        return sendCommand(new Command(ASSIGN_PERMISSION, user, file, permission));
    }

    public Response createPermission(String userName, String userPassword, String permissionName, String rights) {
        User user = new User(userName, userPassword);
        Permission permission = new Permission(permissionName, rights);

        return sendCommand(new Command(CREATE_PERMISSION, user, permission));
    }

    public Response addPermissionToRole(String userName, String userPassword, String roleName, String permissionName) {
        User user = new User(userName, userPassword);
        Role role = new Role(roleName);
        Permission permission = new Permission(permissionName);

        return sendCommand(new Command(ADD_PERMISSION_TO_ROLE, user, role, permission));
    }

    public Response createConstraint(String userName, String userPassword, String roleName1, String roleName2) {
        User user = new User(userName, userPassword);
        Role role1 = new Role(roleName1);
        Role role2 = new Role(roleName2);

        return sendCommand(new Command(CREATE_CONSTRAINT, user, role1, role2));
    }

    public Response createHierarchy(String userName, String userPassword, String roleName1, String roleName2) {
        User user = new User(userName, userPassword);
        Role role1 = new Role(roleName1);
        Role role2 = new Role(roleName2);

        return sendCommand(new Command(CREATE_HIERARCHY, user, role1, role2));
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
