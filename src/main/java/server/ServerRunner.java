package server;

import api.FilePermission;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerRunner {

    public static final int NUMBER_OF_THREADS = 5;
    public static Map<String, Set<FilePermission>> fileSystem = new ConcurrentHashMap<>();

    public static void main(String... args) throws IOException {
        System.out.println("Server started...");

        if (args.length != 1) {
            System.err.println("Usage: java ServerRunner <port number>");
            System.exit(1);
        }

        int portNumber = Integer.parseInt(args[0]);
        ExecutorService executor = null;

        try (ServerSocket serverSocket = new ServerSocket(portNumber)) {
            executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

            System.out.println("Waiting for clients");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Runnable worker = new RequestHandler(clientSocket);
                executor.execute(worker);
            }
        } catch (IOException e) {
            System.out.println("Exception caught on server when trying to listen on port " + portNumber
                    + " or listening for a connection");
        } finally {
            if (executor != null) {
                executor.shutdown();
            }
        }
    }
}
