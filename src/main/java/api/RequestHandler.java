package api;

import server.Server;

import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class RequestHandler implements Runnable {
    private Socket client;

    public RequestHandler(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try (ObjectInputStream in = new ObjectInputStream(client.getInputStream());
             ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream())
        ) {
            System.out.println("Thread started with name:" + Thread.currentThread().getName());

            Command userCommand;
            while ((userCommand = (Command) in.readObject()) != null) {
                if (CommandType.CREATE_RESOURCE.equals(userCommand.getType())) {
                    out.writeObject(createResource(userCommand));
                    out.flush();
                }

                if (CommandType.READ_RESOURCE.equals(userCommand.getType())) {
                    out.writeObject(readResource(userCommand));
                    out.flush();
                }

                if (CommandType.WRITE_RESOURCE.equals(userCommand.getType())) {
                    out.writeObject(writeResource(userCommand));
                    out.flush();
                }

                if (CommandType.CHANGE_RIGHTS.equals(userCommand.getType())) {
                    out.writeObject(changeRights(userCommand));
                    out.flush();
                }
            }
        } catch (IOException e) {
            System.out.println("I/O exception: " + e);
        } catch (Exception ex) {
            System.out.println("Exception in Thread Run. Exception : " + ex);
        }

    }

//   a. Daca numeResursa exista deja, serverul trebuie sa returneze eroare.
//   b. Tip poate fi 0 (director) sau 1 (fisier). Daca este fisier, atunci valoarea va fi asignata acestei resurse.
//   C. Numai utilizatorul care este owner in acel director (de exemplu Bob pt orice din /bob) are voie sa faca asta
    private Response createResource(Command userCommand) throws IOException {
        java.io.File file = new java.io.File("src/main/resources/" + userCommand.getFile().getName());

        if (!userCommand.getFile().getName().toLowerCase().matches("^/" + userCommand.getUser().getName().toLowerCase() + "/.*$"))
            return new Response(ResponseType.NOT_AUTHORIZED);

        if (file.exists())
            return new Response(ResponseType.ALREADY_EXISTING);

        if (FileType.DIRECTORY.equals(userCommand.getFile().getType())) {
            file.mkdirs();
        }

        if (FileType.FILE.equals(userCommand.getFile().getType())) {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(userCommand.getFile().getValue());
            writer.close();
        }


        String n = userCommand.getFile().getName().substring(0, userCommand.getFile().getName().lastIndexOf("/"));
        while(!n.isEmpty()) {
            if (Server.fileSystem.containsKey(n)) {
                Server.fileSystem.put(userCommand.getFile().getName(), Server.fileSystem.get(n));
                return new Response(ResponseType.OK);
            }

            n = n.substring(0, n.lastIndexOf("/"));
        }

        Server.fileSystem.put(userCommand.getFile().getName(), new ArrayList<>());
        return new Response(ResponseType.OK);
    }

//    a. Daca nu exista resursa, trebuie sa returneze eroare.
//    b. Politica de securitate trebuie analizata si sa se returneze eroare daca cererea nu este autorizata.
//    c. Daca este director, trebuie sa returneze ce se gaseste in acel director
    private Response readResource(Command userCommand) throws IOException {
        Path path = Paths.get("src/main/resources/" + userCommand.getFile().getName());

        if (!Files.exists(path))
            return new Response(ResponseType.NOT_EXISTING);

        if (!userCommand.getFile().getName().toLowerCase().matches("^/" + userCommand.getUser().getName().toLowerCase() + "/.*$")
                && !Server.fileSystem.get(userCommand.getFile().getName()).contains(FilePermission.READ))
                return new Response(ResponseType.NOT_AUTHORIZED);

        if (!Files.isDirectory(path)) {
            return new Response(ResponseType.OK, Files.readAllLines(path).stream().collect(Collectors.joining()));
        } else {
//            return new api.Response(api.ResponseType.OK, FileUtils.listFilesAndDirs(path.toFile(), FileFileFilter.FILE, DirectoryFileFilter.DIRECTORY).stream()
//                    .map(o -> o.getName()).collect(Collectors.joining(", ")));
            return new Response(ResponseType.OK, Files.list(path)
                    .map(Path::getFileName)
                    .map(o -> {
                        if (!Files.isDirectory(o))
                            return o.toString() + " - FILE";
                        return o.toString() + " - DIRECTORY";
                    }).collect(Collectors.joining()));
        }
    }

//    a. Daca nu exista resursa, trebuie sa returneze eroare.
//    b. Politica de securitate trebuie analizata si sa se returneze eroare daca cererea nu este autorizata.
    private Response writeResource(Command userCommand) throws IOException {
        Path path = Paths.get("src/main/resources/" + userCommand.getFile().getName());

        if (!Files.exists(path))
            return new Response(ResponseType.NOT_EXISTING);

        if (!userCommand.getFile().getName().toLowerCase().matches("^/" + userCommand.getUser().getName().toLowerCase() + "/.*$")
                && !Server.fileSystem.get(userCommand.getFile().getName()).contains(FilePermission.WRITE))
                return new Response(ResponseType.NOT_AUTHORIZED);

        if (FileType.FILE.equals(userCommand.getFile().getType())) {
            Files.createFile(path);
        }

        return new Response(ResponseType.OK);
    }

//    changeRights(String user, String parola, String numeResursa, String rights)
//    a. Daca nu exista resursa, trebuie sa returneze eroare.
//    b. Politica de securitate trebuie analizata si sa se returneze eroare daca cererea nu este autorizata.
    private Response changeRights(Command userCommand) throws IOException {
        Path path = Paths.get("src/main/resources/" + userCommand.getFile().getName());

        if (!Files.exists(path))
            return new Response(ResponseType.NOT_EXISTING);

        if (!userCommand.getFile().getName().toLowerCase().matches("^/" + userCommand.getUser().getName().toLowerCase() + "/.*$"))
            return new Response(ResponseType.NOT_AUTHORIZED);

        Server.fileSystem.put(userCommand.getFile().getName(), userCommand.getFile().getPermission());

        return new Response(ResponseType.OK);
    }
}