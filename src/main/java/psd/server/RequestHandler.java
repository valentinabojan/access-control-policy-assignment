package psd.server;

import psd.api.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
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
            if (!Files.exists(path.getParent())) {
                Files.createDirectories(path.getParent());
            }
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

        if (!isOwnerOnTheRootDirectory(userCommand) && !hasRights(userCommand.getUser().getUsername(), userCommand.getFile(), READ))
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

        if (!isOwnerOnTheRootDirectory(userCommand) && !hasRights(userCommand.getUser().getUsername(), userCommand.getFile(), WRITE))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (!Files.isDirectory(path))
            Files.write(path, userCommand.getFile().getValue().getBytes());

        return new Response(ResponseType.OK);
    }

    /**
     * a. Daca nu exista resursa, trebuie sa returneze eroare.
     * b. Politica de securitate trebuie analizata si sa se returneze eroare daca cererea nu este autorizata.
     */
    private Response assignPermission(Command userCommand) throws IOException {
        Path path = Paths.get("src/main/resources/workspace" + userCommand.getFile().getName());

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

        repository.createRole(userCommand.getRole());

        return new Response(ResponseType.OK);
    }

    private Response createUser(Command userCommand) {
        if (!isRoot(userCommand.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        repository.createUser(userCommand.getNewUser());

        return new Response(ResponseType.OK);
    }

    private Response createPermission(Command userCommand) {
        if (!isRoot(userCommand.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        repository.createPermission(userCommand.getPermission());

        return new Response(ResponseType.OK);
    }

    private Response createConstraint(Command userCommand) {
        if (!isRoot(userCommand.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        repository.createConstraint(new Constraint(userCommand.getRole1().getRoleName(), userCommand.getRole2().getRoleName()));

        return new Response(ResponseType.OK);
    }

    private Response addPermissionToRole(Command userCommand) {
        if (!isRoot(userCommand.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        repository.addPermissionToRole(userCommand.getPermission().getPermissionName(), userCommand.getRole().getRoleName());

        return new Response(ResponseType.OK);
    }

    private Response assignRole(Command userCommand) {
        if (!isRoot(userCommand.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (exitsConstraint(userCommand.getTargetUserName(), userCommand.getTargetRoleName()))
            return new Response(ResponseType.FORBIDDEN);

        repository.addRoleToUser(userCommand.getTargetUserName(), userCommand.getTargetRoleName());

        return new Response(ResponseType.OK);
    }

    private boolean exitsConstraint(String userName, String roleName) {
        User user = repository.getUser(userName);

        return user.getRoles().stream()
                .anyMatch(role ->
                        repository.getConstraint(roleName, role.getRoleName()) != null
                        || repository.getConstraint(role.getRoleName(), roleName) != null);
    }

    private Response revokeRole(Command userCommand) {
        if (!isRoot(userCommand.getUser()))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (!userHasRole(userCommand.getTargetUserName(), userCommand.getTargetRoleName()))
            return new Response(ResponseType.INVALID);

        repository.deleteRoleForUser(userCommand.getTargetUserName(), userCommand.getTargetRoleName());

        return new Response(ResponseType.OK);
    }

    private boolean userHasRole(String userName, String roleName) {
        Role role = repository.getRole(roleName);
        User user = repository.getUser(userName);

        return user.getRoles().contains(role);
    }

    private boolean isRoot(User user) {
        return user.equals(new User("root", "root"));
    }

    private boolean hasRights(String userName, File file, FilePermission filePermission) {
        String fileName = file.getName();
        User user = repository.getUser(userName);
        while (!fileName.isEmpty()) {
            Set<String> permissionNames = ServerRunner.fileSystem.get(fileName);
            if (permissionNames != null) {
                Stream<List<Permission>> listStream = user.getRoles().stream()
                        .map(Role::getPermissions);
                Stream<Permission> permissionStream = listStream
                        .flatMap(Collection::stream);
                Stream<Permission> permissionStream1 = permissionStream
                        .filter(permission -> permissionNames.contains(permission.getPermissionName()));
                Stream<Permission> permissionStream2 = permissionStream1
                        .filter(permission -> permission.getRights().contains(filePermission.getPermission()));
                boolean hasRights = permissionStream2
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