import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by valen_000 on 6/11/2016.
 */
public class Client {

    private ObjectOutputStream out;
    private ObjectInputStream in;

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java EchoClient <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        Client client = new Client();

        try {
            client.connect(hostName, portNumber);
        }catch (UnknownHostException e) {
            System.err.println("Don't know about host " + hostName);
            System.exit(1);
        }catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }

    public void connect (String hostName, Integer portNumber) throws IOException {
        Socket socket = new Socket(hostName, portNumber);
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    public Response createResource(String userName, String userPassword, String resourceName, int resourceType, String resourceValue) {
        User user = new User(userName, userPassword);
        File file = new File(resourceName, resourceValue, null, FileType.fromInteger(resourceType));

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
        File file = new File(resourceName, null, null, null);

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
        File file = new File(resourceName, resourceValue, null, null);

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

        List<FilePermission> filePermissions = new ArrayList<>();
        if (resourcePermissions.contains("r"))
            filePermissions.add(FilePermission.READ);
        if (resourcePermissions.contains("w"))
            filePermissions.add(FilePermission.WRITE);

        File file = new File(resourceName, null, filePermissions, null);

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
