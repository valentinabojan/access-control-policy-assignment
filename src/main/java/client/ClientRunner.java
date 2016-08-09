package client;

import java.io.IOException;
import java.net.UnknownHostException;

public class ClientRunner {

    public static void main(String... args) throws IOException {
        if (args.length != 2) {
            System.err.println("Usage: java ClientRunner <host name> <port number>");
            System.exit(1);
        }

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        Client client = new Client();

        try {
            client.connect(hostName, portNumber);
        }catch (UnknownHostException e) {
            System.err.println("Client doesn't know about host " + hostName);
            System.exit(1);
        }catch (IOException e) {
            System.err.println("Client couldn't get I/O for the connection to " + hostName);
            System.exit(1);
        }
    }
}
