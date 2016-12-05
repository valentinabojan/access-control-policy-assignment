package server;

import api.*;
import api.entities.*;

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

import static api.FilePermission.READ;
import static api.FilePermission.WRITE;
import static api.FileType.DIRECTORY;
import static api.FileType.FILE;
import static api.entities.Constraint.ConstraintBuilder.constraint;
import static api.entities.RoleHierarchy.RoleHierarchyBuilder.roleHierarchy;
import static api.entities.User.UserBuilder.user;
import static server.ServerRunner.fileSystem;

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
                    case CREATE_HIERARCHY:
                        out.writeObject(createHierarchy(userCommand));
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

        if (DIRECTORY.equals(userCommand.getFile().getType()))
            Files.createDirectories(path);

        if (FILE.equals(userCommand.getFile().getType())) {
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

        if (!isOwnerOnTheRootDirectory(userCommand) && !hasRights(userCommand.getUser().getUsername(), userCommand.getFile(), READ))
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

        if (!isOwnerOnTheRootDirectory(userCommand) && !hasRights(userCommand.getUser().getUsername(), userCommand.getFile(), WRITE))
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

        if (repository.getEntity(Permission.class, userCommand.getPermission().getPermissionName()) == null) {
            return new Response(ResponseType.NOT_EXISTING);
        }

        Set<String> existingPermissions = fileSystem.get(userCommand.getFile().getName());
        if (existingPermissions == null)
            existingPermissions = new HashSet<>();
        existingPermissions.add(userCommand.getPermission().getPermissionName());
        fileSystem.put(userCommand.getFile().getName(), existingPermissions);

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

    private Response createHierarchy(Command userCommand) {
        if (!isRoot(userCommand.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        repository.createEntity(roleHierarchy().withParent(userCommand.getTargetRole().getName()).withChild(userCommand.getRole().getName()).build());

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
        User user = repository.getEntity(User.class, userName);

        return user.getRoles().stream()
                .map(this::getAncestorRoles)
                .flatMap(Collection::stream)
                .anyMatch(role ->
                        repository.getEntity(Constraint.class, new ConstraintId(role.getName(), roleName)) != null
                                || repository.getEntity(Constraint.class, new ConstraintId(roleName, role.getName())) != null);
    }

    private Set<Role> getAncestorRoles(Role role) {
        Set<Role> children = repository.getChildrenRoles(role.getName());

        if (children.isEmpty())
            return new HashSet<>();

        return children.stream()
                .flatMap(r -> add(r, getAncestorRoles(r)).stream())
                .collect(Collectors.toSet());
    }

    private boolean userHasRole(String userName, String roleName) {
        Role role = repository.getEntity(Role.class, roleName);
        User user = repository.getEntity(User.class, userName);

        return user.getRoles().contains(role);
    }

    private boolean isRoot(User user) {
        return user.equals(user().withUsername("root").withPassword("root").build());
    }

    private boolean hasRights(String userName, File file, FilePermission filePermission) {
        String fileName = file.getName();
        User user = repository.getEntity(User.class, userName);
        while (!fileName.isEmpty()) {
            Set<String> permissionNames = ServerRunner.fileSystem.get(fileName);
            if (permissionNames != null) {
                boolean hasRights = user.getRoles().stream()
                        .map(role -> add(role, getAncestorRoles(role)))
                        .flatMap(Collection::stream)
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

    private <T> Set<T> add(T item, Set<T> itemSet) {
        itemSet.add(item);
        return itemSet;
    }

    private boolean isOwnerOnTheRootDirectory(Command userCommand) {
        return userCommand.getFile().getName().toLowerCase().matches("^/" + userCommand.getUser().getUsername().toLowerCase() + "/.*$");
    }
}