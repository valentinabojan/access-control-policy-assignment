package server;

import api.Command;
import api.FilePermission;
import api.Response;
import api.ResponseType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static api.FilePermission.READ;
import static api.FilePermission.WRITE;
import static api.FileType.DIRECTORY;
import static api.FileType.FILE;

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

            Command userCommand;
            while ((userCommand = (Command) in.readObject()) != null) {
                switch (userCommand.getType()) {
                    case CREATE_RESOURCE:
                        out.writeObject(createResource(userCommand));
                        out.flush();
                        break;
                    case READ_RESOURCE:
                        out.writeObject(readResource(userCommand));
                        out.flush();
                        break;
                    case WRITE_RESOURCE:
                        out.writeObject(writeResource(userCommand));
                        out.flush();
                        break;
                    case CHANGE_RIGHTS:
                        out.writeObject(changeRights(userCommand));
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

    /**
     * a. Daca numeResursa exista deja, serverul trebuie sa returneze eroare.
     * b. Tip poate fi 0 (director) sau 1 (fisier). Daca este fisier, atunci valoarea va fi asignata acestei resurse.
     * c. Numai utilizatorul care este owner in acel director (de exemplu Bob pt orice din /bob) are voie sa faca asta
     */
    private Response createResource(Command userCommand) throws IOException {
        Path path = Paths.get("src/main/resources/workspace" + userCommand.getFile().getName());

        if (Files.exists(path))
            return new Response(ResponseType.ALREADY_EXISTING);

        if (!isOwnerOnTheRootDirectory(userCommand))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (DIRECTORY.equals(userCommand.getFile().getType()))
            Files.createDirectories(path);

        if (FILE.equals(userCommand.getFile().getType())) {
            Files.createFile(path);
            Files.write(path, userCommand.getFile().getValue().getBytes());
        }

        String n = userCommand.getFile().getName().substring(0, userCommand.getFile().getName().lastIndexOf("/"));
        while (!n.isEmpty()) {
            if (ServerRunner.fileSystem.containsKey(n)) {
                ServerRunner.fileSystem.put(userCommand.getFile().getName(), ServerRunner.fileSystem.get(n));
                return new Response(ResponseType.OK);
            }

            n = n.substring(0, n.lastIndexOf("/"));
        }

//        ServerRunner.fileSystem.put(userCommand.getFile().getName(), new HashSet<>());
        return new Response(ResponseType.OK);
    }

    /**
     * a. Daca nu exista resursa, trebuie sa returneze eroare.
     * b. Politica de securitate trebuie analizata si sa se returneze eroare daca cererea nu este autorizata.
     * c. Daca este director, trebuie sa returneze ce se gaseste in acel director.
     */
    private Response readResource(Command userCommand) throws IOException {
        Path path = Paths.get("src/main/resources/workspace" + userCommand.getFile().getName());

        if (!Files.exists(path))
            return new Response(ResponseType.NOT_EXISTING);

        if (!isOwnerOnTheRootDirectory(userCommand) && !hasRights(userCommand, READ))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (!Files.isDirectory(path)) {
            return new Response(ResponseType.OK, Files.readAllLines(path).stream().collect(Collectors.joining()));
        } else {
//            return new api.Response(api.ResponseType.OK, FileUtils.listFilesAndDirs(path.toFile(), FileFileFilter.FILE, DirectoryFileFilter.DIRECTORY).stream()
//                    .map(o -> o.getName()).collect(Collectors.joining(", ")));
            try (Stream<Path> entries = Files.list(path)) {
                return new Response(ResponseType.OK, entries
                        .map(Path::getFileName)
                        .map(o -> {
                            if (!Files.isDirectory(o))
                                return o.toString() + " - FILE";
                            return o.toString() + " - DIRECTORY";
                        }).collect(Collectors.joining()));
            }
        }
    }

    /**
     * a. Daca nu exista resursa, trebuie sa returneze eroare.
     * b. Politica de securitate trebuie analizata si sa se returneze eroare daca cererea nu este autorizata.
     */
    private Response writeResource(Command userCommand) throws IOException {
        Path path = Paths.get("src/main/resources/workspace" + userCommand.getFile().getName());

        if (!Files.exists(path))
            return new Response(ResponseType.NOT_EXISTING);

        if (!isOwnerOnTheRootDirectory(userCommand) && !hasRights(userCommand, WRITE))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (!Files.isDirectory(path))
            Files.write(path, userCommand.getFile().getValue().getBytes());

        return new Response(ResponseType.OK);
    }

    /**
     * a. Daca nu exista resursa, trebuie sa returneze eroare.
     * b. Politica de securitate trebuie analizata si sa se returneze eroare daca cererea nu este autorizata.
     */
    private Response changeRights(Command userCommand) throws IOException {
        Path path = Paths.get("src/main/resources/workspace" + userCommand.getFile().getName());

        if (!Files.exists(path))
            return new Response(ResponseType.NOT_EXISTING);

        if (!isOwnerOnTheRootDirectory(userCommand))
            return new Response(ResponseType.NOT_AUTHORIZED);

        ServerRunner.fileSystem.put(userCommand.getFile().getName(), userCommand.getFile().getPermission());

        return new Response(ResponseType.OK);
    }

    private boolean hasRights(Command userCommand, FilePermission permission) {
        return ServerRunner.fileSystem.containsKey(userCommand.getFile().getName())
                && ServerRunner.fileSystem.get(userCommand.getFile().getName()).contains(permission);
    }

    private boolean isOwnerOnTheRootDirectory(Command userCommand) {
        return userCommand.getFile().getName().toLowerCase().matches("^/" + userCommand.getUser().getName().toLowerCase() + "/.*$");
    }
}