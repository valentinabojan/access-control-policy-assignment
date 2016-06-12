import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by valen_000 on 6/11/2016.
 */
public class Server {

    public static Map<String, List<FilePermission>> fileSystem = new HashMap<>();

    public static void main(String[] args) throws IOException {
        System.out.println("Start of main");
        if (args.length < 1) {
            System.err.println("Usage: java EchoServer <port number>");
            System.exit(1);
        }
        int portNumber = Integer.parseInt(args[0]);
        ExecutorService executor = null;
        try (ServerSocket serverSocket = new ServerSocket(portNumber)
        ) {
            executor = Executors.newFixedThreadPool(5);
            System.out.println("Waiting for clients");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Runnable worker = new RequestHandler(clientSocket);
                executor.execute(worker);
            }
        }catch (IOException e) {
            System.out.println("Exception caught when trying to listen on port "
                    + portNumber + " or listening for a connection");
            System.out.println(e.getMessage());
        } finally {
            if (executor != null) {
                executor.shutdown();
            }
        }
    }
}
