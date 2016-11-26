package server;

import api.*;
import api.entities.Role;
import api.entities.User;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static api.entities.Constraint.ConstraintBuilder.constraint;
import static api.entities.User.UserBuilder.user;

public class RequestHandler implements Runnable {

    private static final String ROOT_PATH = "src/main/resources/workspace";
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
                    case ASSIGN_PERMISSION:
                        out.writeObject(assignPermission(userCommand));
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
                    case CREATE_PERMISSION:
                        out.writeObject(createPermission(userCommand));
                        out.flush();
                        break;
                    case CREATE_CONSTRAINT:
                        out.writeObject(createConstraint(userCommand));
                        out.flush();
                        break;
                    case ASSIGN_ROLE:
                        out.writeObject(assignRole(userCommand));
                        out.flush();
                        break;
                    case REVOKE_ROLE:
                        out.writeObject(revokeRole(userCommand));
                        out.flush();
                        break;
                    case ADD_PERMISSION_TO_ROLE:
                        out.writeObject(addPermissionToRole(userCommand));
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

    private Response createResource(Command userCommand) throws IOException {
        Path path = Paths.get(ROOT_PATH + userCommand.getFile().getName());

        if (Files.exists(path))
            return new Response(ResponseType.ALREADY_EXISTING);

        if (!isOwnerOnTheRootDirectory(userCommand))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (FileType.DIRECTORY.equals(userCommand.getFile().getType()))
            Files.createDirectories(path);

        if (FileType.FILE.equals(userCommand.getFile().getType())) {
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
            Files.createFile(path);
            Files.write(path, userCommand.getFile().getValue().getBytes());
        }

        return new Response(ResponseType.OK);
    }

    private Response readResource(Command userCommand) throws IOException {
        Path path = Paths.get(ROOT_PATH + userCommand.getFile().getName());

        if (!Files.exists(path))
            return new Response(ResponseType.NOT_EXISTING);

        if (!isOwnerOnTheRootDirectory(userCommand) && !hasRights(userCommand.getUser().getUsername(), userCommand.getFile(), FilePermission.READ))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (!Files.isDirectory(path)) {
            return new Response(ResponseType.OK, Files.readAllLines(path).stream().collect(Collectors.joining()));
        } else {
            try (Stream<Path> entries = Files.list(path)) {
                return new Response(ResponseType.OK, entries.map(Path::getFileName).map(Path::toString).collect(Collectors.joining("\n")));
            }
        }
    }

    private Response writeResource(Command userCommand) throws IOException {
        Path path = Paths.get(ROOT_PATH + userCommand.getFile().getName());

        if (!Files.exists(path))
            return new Response(ResponseType.NOT_EXISTING);

        if (!isOwnerOnTheRootDirectory(userCommand) && !hasRights(userCommand.getUser().getUsername(), userCommand.getFile(), FilePermission.WRITE))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (!Files.isDirectory(path))
            Files.write(path, userCommand.getFile().getValue().getBytes());

        return new Response(ResponseType.OK);
    }

    private Response assignPermission(Command userCommand) throws IOException {
        Path path = Paths.get(ROOT_PATH + userCommand.getFile().getName());

        if (!Files.exists(path))
            return new Response(ResponseType.NOT_EXISTING);

        if (!isOwnerOnTheRootDirectory(userCommand))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (repository.getPermission(userCommand.getPermission().getPermissionName()) == null) {
            return new Response(ResponseType.NOT_EXISTING);
        }

        Set<String> existingPermissions = ServerRunner.fileSystem.get(userCommand.getFile().getName());
        if (existingPermissions == null)
            existingPermissions = new HashSet<>();
        existingPermissions.add(userCommand.getPermission().getPermissionName());
        ServerRunner.fileSystem.put(userCommand.getFile().getName(), existingPermissions);

        return new Response(ResponseType.OK);
    }

    private Response createRole(Command userCommand) {
        if (!isRoot(userCommand.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        repository.createEntity(userCommand.getRole());

        return new Response(ResponseType.OK);
    }

    private Response createUser(Command userCommand) {
        if (!isRoot(userCommand.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        repository.createEntity(userCommand.getTargetUser());

        return new Response(ResponseType.OK);
    }

    private Response createPermission(Command userCommand) {
        if (!isRoot(userCommand.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        repository.createEntity(userCommand.getPermission());

        return new Response(ResponseType.OK);
    }

    private Response createConstraint(Command userCommand) {
        if (!isRoot(userCommand.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        repository.createEntity(constraint().withRoleName1(userCommand.getRole().getName()).withRoleName2(userCommand.getTargetRole().getName()).build());

        return new Response(ResponseType.OK);
    }

    private Response addPermissionToRole(Command userCommand) {
        if (!isRoot(userCommand.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        repository.addPermissionToRole(userCommand.getPermission().getPermissionName(), userCommand.getRole().getName());

        return new Response(ResponseType.OK);
    }

    private Response assignRole(Command userCommand) {
        if (!isRoot(userCommand.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (exitsConstraint(userCommand.getTargetUser().getUsername(), userCommand.getRole().getName()))
            return new Response(ResponseType.FORBIDDEN);

        repository.addRoleToUser(userCommand.getTargetUser().getUsername(), userCommand.getRole().getName());

        return new Response(ResponseType.OK);
    }

    private Response revokeRole(Command userCommand) {
        if (!isRoot(userCommand.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (!userHasRole(userCommand.getTargetUser().getUsername(), userCommand.getRole().getName()))
            return new Response(ResponseType.INVALID);

        repository.deleteRoleForUser(userCommand.getTargetUser().getUsername(), userCommand.getRole().getName());

        return new Response(ResponseType.OK);
    }

    private boolean exitsConstraint(String userName, String roleName) {
        User user = repository.getUser(userName);

        return user.getRoles().stream()
                .anyMatch(role ->
                        repository.getConstraint(roleName, role.getName()) != null
                                || repository.getConstraint(role.getName(), roleName) != null);
    }

    private boolean userHasRole(String userName, String roleName) {
        Role role = repository.getRole(roleName);
        User user = repository.getUser(userName);

        return user.getRoles().contains(role);
    }

    private boolean isRoot(User user) {
        return user.equals(user().withUsername("root").withPassword("root").build());
    }

    private boolean hasRights(String userName, File file, FilePermission filePermission) {
        String fileName = file.getName();
        User user = repository.getUser(userName);
        while (!fileName.isEmpty()) {
            Set<String> permissionNames = ServerRunner.fileSystem.get(fileName);
            if (permissionNames != null) {
                boolean hasRights = user.getRoles().stream()
                        .map(Role::getPermissions)
                        .flatMap(Collection::stream)
                        .filter(permission -> permissionNames.contains(permission.getPermissionName()))
                        .anyMatch(permission -> permission.getRights().contains(filePermission.getPermission()));
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