package psd.server;

import psd.api.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static psd.api.FilePermission.READ;
import static psd.api.FilePermission.WRITE;
import static psd.api.FileType.DIRECTORY;
import static psd.api.FileType.FILE;

public class RequestHandler implements Runnable {

    private UserRolesRepository repository;
    private Socket client;

    public RequestHandler(UserRolesRepository repository, Socket client) {
        this.repository = repository;
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
                    case ADD_RIGHTS:
                        out.writeObject(addRights(userCommand));
                        out.flush();
                        break;
                    case CREATE_ROLE:
                        out.writeObject(createRole(userCommand));
                        out.flush();
                        break;
                    case CREATE_USER:
                        out.writeObject(createUser(userCommand));
                        out.flush();
                        break;
                    case CHANGE_RIGHTS:
                        out.writeObject(changeRights(userCommand));
                        out.flush();
                        break;
                    case ASSIGN_ROLE:
                        out.writeObject(assignRole(userCommand));
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

        if (!isOwnerOnTheRootDirectory(userCommand) && !hasRights(userCommand.getUser(), userCommand.getFile(), READ))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (!Files.isDirectory(path)) {
            return new Response(ResponseType.OK, Files.readAllLines(path).stream().collect(Collectors.joining()));
        } else {
//            return new api.Response(api.ResponseType.OK, FileUtils.listFilesAndDirs(path.toFile(), FileFileFilter.FILE, DirectoryFileFilter.DIRECTORY).stream()
//                    .map(o -> o.getUsername()).collect(Collectors.joining(", ")));
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

        if (!isOwnerOnTheRootDirectory(userCommand) && !hasRights(userCommand.getUser(), userCommand.getFile(), WRITE))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (!Files.isDirectory(path))
            Files.write(path, userCommand.getFile().getValue().getBytes());

        return new Response(ResponseType.OK);
    }

    /**
     * a. Daca nu exista resursa, trebuie sa returneze eroare.
     * b. Politica de securitate trebuie analizata si sa se returneze eroare daca cererea nu este autorizata.
     */
    private Response addRights(Command userCommand) throws IOException {
        Path path = Paths.get("src/main/resources/workspace" + userCommand.getFile().getName());

        if (!Files.exists(path))
            return new Response(ResponseType.NOT_EXISTING);

        if (!isOwnerOnTheRootDirectory(userCommand))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (repository.getRole(userCommand.getRole().getRoleName()) == null) {
            return new Response(ResponseType.NOT_EXISTING);
        }

        Set<String> existingRoles = ServerRunner.fileSystem.get(userCommand.getFile().getName());
        existingRoles.add(userCommand.getRole().getRoleName());
        ServerRunner.fileSystem.put(userCommand.getFile().getName(), existingRoles);

        return new Response(ResponseType.OK);
    }

    private Response createRole(Command userCommand) {
        if (!isRoot(userCommand.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        repository.createRole(userCommand.getRole());

        return new Response(ResponseType.OK);
    }

    private Object createUser(Command userCommand) {
        if (!isRoot(userCommand.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        repository.createUser(userCommand.getNewUser());

        return new Response(ResponseType.OK);
    }

    private Object changeRights(Command userCommand) {
        if (!isRoot(userCommand.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        repository.updateRole(userCommand.getRole());

        return new Response(ResponseType.OK);
    }

    private Object assignRole(Command userCommand) {
        if (!isRoot(userCommand.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        repository.addRoleToUser(userCommand.getTargetUserName(), userCommand.getTargetRoleName());

        return new Response(ResponseType.OK);
    }

    private boolean isRoot(User user) {
        return user.equals(new User("root", "root"));
    }

    private boolean hasRights(User user, File file, FilePermission permission) {
        String fileName = file.getName();
        while (!fileName.isEmpty()) {
            Set<String> roleNames = ServerRunner.fileSystem.get(fileName);
            if (roleNames != null) {
                boolean hasRights = roleNames.stream()
                        .map(roleName -> repository.getRole(roleName))
                        .filter(role -> repository.getRolesForUser(user.getUsername()).contains(role))
                        .filter(role -> role.getRights().contains(permission.toString()))
                        .findAny().isPresent();
                if (hasRights)
                    return true;
            }

            fileName = fileName.substring(0, fileName.lastIndexOf("/"));
        }

        return false;
    }

    private boolean isOwnerOnTheRootDirectory(Command userCommand) {
        return userCommand.getFile().getName().toLowerCase().matches("^/" + userCommand.getUser().getUsername().toLowerCase() + "/.*$");
    }
}