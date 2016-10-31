package server;

import api.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static api.FilePermission.READ;
import static api.FilePermission.WRITE;
import static api.FileType.DIRECTORY;
import static api.FileType.FILE;
import static api.ResponseType.LOGIN_FAILED;

public class RequestHandler implements Runnable {

    private Socket client;

    public RequestHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(client.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream())) {
            System.out.println("Worker started with name:" + Thread.currentThread().getName());

            Command command;
            while ((command = (Command) in.readObject()) != null) {
                if (!loginSucceeds(command)) {
                    out.writeObject(new Response(LOGIN_FAILED));
                    out.flush();
                    continue;
                }

                switch (command.getType()) {
                    case CREATE_RESOURCE:
                        out.writeObject(createResource(command));
                        out.flush();
                        break;
                    case READ_RESOURCE:
                        out.writeObject(readResource(command));
                        out.flush();
                        break;
                    case WRITE_RESOURCE:
                        out.writeObject(writeResource(command));
                        out.flush();
                        break;
                    case CHANGE_RIGHTS:
                        out.writeObject(changeRights(command));
                        out.flush();
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("I/O exception: " + e);
        } catch (Exception ex) {
            System.out.println("Exception in Worker Run. Exception : " + ex);
        }
    }

    private boolean loginSucceeds(Command command) {
        String password = command.getUser().getPassword();
        String username = command.getUser().getName();
        return password.equals(username);
    }

    private synchronized Response createResource(Command command) throws IOException {
        Path path = Paths.get("src/main/resources" + command.getFile().getName());

        if (Files.exists(path))
            return new Response(ResponseType.ALREADY_EXISTING);

        if (!isOwnerOnTheRootDirectory(command))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (DIRECTORY.equals(command.getFile().getType()))
            Files.createDirectories(path);

        if (FILE.equals(command.getFile().getType())) {
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            Files.createFile(path);
            Files.write(path, command.getFile().getValue().getBytes());
        }

        return new Response(ResponseType.OK);
    }

    private synchronized Response readResource(Command command) throws IOException {
        Path path = Paths.get("src/main/resources" + command.getFile().getName());

        if (!Files.exists(path))
            return new Response(ResponseType.NOT_EXISTING);

        if (!isOwnerOnTheRootDirectory(command) && !hasRights(command.getFile(), READ))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (!Files.isDirectory(path)) {
            return new Response(ResponseType.OK, Files.readAllLines(path).stream().collect(Collectors.joining()));
        } else {
            try (Stream<Path> entries = Files.list(path)) {
                return new Response(ResponseType.OK, entries.map(Path::getFileName).map(Path::toString).collect(Collectors.joining("\n")));
            }
        }
    }

    private synchronized Response writeResource(Command command) throws IOException {
        Path path = Paths.get("src/main/resources" + command.getFile().getName());

        if (!Files.exists(path))
            return new Response(ResponseType.NOT_EXISTING);

        if (!isOwnerOnTheRootDirectory(command) && !hasRights(command.getFile(), WRITE))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (!Files.isDirectory(path))
            Files.write(path, command.getFile().getValue().getBytes());

        return new Response(ResponseType.OK);
    }

    private synchronized Response changeRights(Command command) throws IOException {
        Path path = Paths.get("src/main/resources" + command.getFile().getName());

        if (!Files.exists(path))
            return new Response(ResponseType.NOT_EXISTING);

        if (!isOwnerOnTheRootDirectory(command))
            return new Response(ResponseType.NOT_AUTHORIZED);

        ServerRunner.fileSystem.put(command.getFile().getName(), command.getFile().getPermission());

        return new Response(ResponseType.OK);
    }

    private boolean hasRights(File file, FilePermission permission) {
        String fileName = file.getName();
        while (!fileName.isEmpty()) {
            Set<FilePermission> rights = ServerRunner.fileSystem.get(fileName);
            if (rights != null) {
                return rights.contains(permission);
            }

            fileName = fileName.substring(0, fileName.lastIndexOf("/"));
        }

        return false;
    }

    private boolean isOwnerOnTheRootDirectory(Command command) {
        String path = command.getFile().getName().toLowerCase();
        String rootPathRegex = "^/" + command.getUser().getName().toLowerCase() + "/.*$";

        return path.matches(rootPathRegex);
    }
}