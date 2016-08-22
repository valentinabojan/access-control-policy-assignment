import org.assertj.core.api.Assertions;
import org.junit.*;
import psd.api.Response;
import psd.api.ResponseType;
import psd.api.Role;
import psd.api.User;
import psd.client.Client;
import psd.server.PersistenceManager;
import psd.server.ServerRunner;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

public class ClientTest {

    private Client client;
    private Thread serverThread;

    @Before
    public void setup() {
        serverThread = new Thread(() -> {
            try {
                ServerRunner.main("8005");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        try {
            client = new Client();
            client.connect("127.0.0.1", 8005);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() throws IOException {
        deleteNonEmptyDirectory(Paths.get("src/main/resources/workspace"));
        clearDatabase();
    }

    private void deleteNonEmptyDirectory(Path path) {
        try {
            if (Files.isDirectory(path)) {
                try (Stream<Path> entries = Files.list(path)) {
                    entries.forEach(this::deleteNonEmptyDirectory);
                }
            }

            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    @Test
//    public void givenSeveralClientRequests_WhenTheRequestsAreProcessedByTheServerWorkers_ThenTheFileSystemIsUpdatedCorrectly() {
//        Response response;
//
//        // 1
//        response = client.createResource("alice", "alice", "/bob/cursuri", 0, null);
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));
//
//        // 2
//        response = client.createResource("alice", "alice", "/alice/cursuri", 0, null);
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));
//
//        // 3
//        response = client.createResource("alice", "alice", "/alice/cursuri", 0, null);
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.ALREADY_EXISTING));
//
//        // 4
//        response = client.createResource("alice", "alice", "/alice/cursuri", 1, null);
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.ALREADY_EXISTING));
//
//        // 5
//        response = client.readResource("bob", "bob", "/alice/cursuri");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));
//
//        // 6
//        response = client.readResource("alice", "alice", "/alice/cursuri");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, ""));
//
//        // 7
//        response = client.changeRights("bob", "bob", "/alice/cursuri", "rw");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));
//
//        // 8
//        response = client.changeRights("alice", "alice", "/alice/cursuri", "rw");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));
//
//        // 9
//        response = client.readResource("bob", "bob", "/alice/cursuri");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, ""));
//
//        // 10
//        response = client.createResource("alice", "alice", "/alice/cursuri/a.java", 1, "Test");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));
//
//        // 11
//        response = client.readResource("bob", "bob", "/alice/cursuri");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, "a.java - FILE"));
//
//        // 12
//        response = client.readResource("bob", "bob", "/alice/cursuri/a.java");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, "Test"));
//
//        // 13
//        response = client.changeRights("alice", "alice", "/alice/cursuri/b.java", "rw");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_EXISTING));
//
//        // 16 - write test
//        response = client.writeResource("bob", "bob", "/alice/cursuri/a.java", "writeTest");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));
//
//        // 17 - write test
//        response = client.readResource("bob", "bob", "/alice/cursuri/a.java");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, "writeTest"));
//
//        // 14
//        response = client.changeRights("alice", "alice", "/alice/cursuri/a.java", "");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));
//
//        // 15
//        response = client.readResource("bob", "bob", "/alice/cursuri/a.java");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));
//    }

    @Test
    public void t2() {
        Response response;

        // 1
        response = client.createRole("bob", "bob", "role1");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 2
        response = client.createRole("root", "root", "role1");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        response = client.changeRights("bob", "bob", "role1", "r");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 3
        response = client.changeRights("root", "root", "role1", "r");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        response = client.createUser("bob", "bob", "bob", "bob");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        response = client.createUser("root", "root", "bob", "bob");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        response = client.assignRole("bob", "bob", "bob", "role1");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 4
        response = client.assignRole("root", "root", "bob", "role1");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

    }

    private void clearDatabase() {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        em.getTransaction().begin();

        TypedQuery<User> userQuery = em.createQuery("SELECT u FROM User u", User.class);
        userQuery.getResultList().stream().forEach(em::remove);

        TypedQuery<Role> roleQuery = em.createQuery("SELECT r FROM Role r", Role.class);
        roleQuery.getResultList().stream().forEach(em::remove);

        em.getTransaction().commit();
        em.close();
    }

}