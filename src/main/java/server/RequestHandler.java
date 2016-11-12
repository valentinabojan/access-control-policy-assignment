package server;

import api.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static api.FilePermission.READ;
import static api.FilePermission.WRITE;
import static api.FileType.DIRECTORY;
import static api.FileType.FILE;
import static api.ResponseType.LOGIN_FAILED;
import static api.User.UserBuilder.user;

public class RequestHandler implements Runnable {

    public static final String ROOT_PATH = "src/main/resources/workspace";

    private EntityRepository repository;
    private Socket client;

    public RequestHandler(EntityRepository repository, Socket client) {
        this.repository = repository;
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
                    case ADD_RIGHTS:
                        out.writeObject(addRights(command));
                        out.flush();
                        break;
                    case CREATE_ROLE:
                        out.writeObject(createRole(command));
                        out.flush();
                        break;
                    case CREATE_USER:
                        out.writeObject(createUser(command));
                        out.flush();
                        break;
                    case CHANGE_RIGHTS:
                        out.writeObject(changeRights(command));
                        out.flush();
                        break;
                    case ASSIGN_ROLE:
                        out.writeObject(assignRole(command));
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
        String username = command.getUser().getUsername();
        return password.equals(username);
    }

    private synchronized Response createResource(Command command) throws IOException {
        Path path = Paths.get(ROOT_PATH + command.getFile().getName());

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
        Path path = Paths.get(ROOT_PATH + command.getFile().getName());

        if (!Files.exists(path))
            return new Response(ResponseType.NOT_EXISTING);

        if (!isOwnerOnTheRootDirectory(command) && !hasRights(command.getUser(), command.getFile(), READ))
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
        Path path = Paths.get(ROOT_PATH + command.getFile().getName());

        if (!Files.exists(path))
            return new Response(ResponseType.NOT_EXISTING);

        if (!isOwnerOnTheRootDirectory(command) && !hasRights(command.getUser(), command.getFile(), WRITE))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (!Files.isDirectory(path))
            Files.write(path, command.getFile().getValue().getBytes());

        return new Response(ResponseType.OK);
    }

    private synchronized Response addRights(Command command) throws IOException {
        Path path = Paths.get(ROOT_PATH + command.getFile().getName());

        if (!Files.exists(path))
            return new Response(ResponseType.NOT_EXISTING);

        if (!isOwnerOnTheRootDirectory(command))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (repository.getRole(command.getRole().getName()) == null) {
            return new Response(ResponseType.NOT_EXISTING);
        }

        Set<String> existingRoles = ServerRunner.fileSystem.get(command.getFile().getName());
        if (existingRoles == null)
            existingRoles = new HashSet<>();
        existingRoles.add(command.getRole().getName());
        ServerRunner.fileSystem.put(command.getFile().getName(), existingRoles);

        return new Response(ResponseType.OK);
    }

    private synchronized Response createRole(Command command) {
        if (!isRoot(command.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        repository.createEntity(command.getRole());

        return new Response(ResponseType.OK);
    }

    private synchronized Response createUser(Command command) {
        if (!isRoot(command.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        repository.createEntity(command.getTargetUser());

        return new Response(ResponseType.OK);
    }

    private synchronized Response changeRights(Command command) {
        if (!isRoot(command.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        repository.updateRole(command.getRole());

        return new Response(ResponseType.OK);
    }

    private synchronized Response assignRole(Command command) {
        if (!isRoot(command.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        repository.addRoleToUser(command.getTargetUser().getUsername(), command.getRole().getName());

        return new Response(ResponseType.OK);
    }

    private boolean isRoot(User user) {
        return user.equals(user().withUsername("root").withPassword("root").build());
    }

    private boolean hasRights(User user, File file, FilePermission permission) {
        String fileName = file.getName();
        while (!fileName.isEmpty()) {
            Set<String> roleNames = ServerRunner.fileSystem.get(fileName);
            if (roleNames != null) {
                boolean hasRights = roleNames.stream()
                        .map(repository::getRole)
                        .filter(role -> role.getUsers().contains(user))
                        .filter(role -> role.getRights().contains(permission.getPermission()))
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