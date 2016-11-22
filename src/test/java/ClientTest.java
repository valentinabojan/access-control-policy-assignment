import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import api.Response;
import api.ResponseType;
import api.Role;
import api.User;
import client.Client;
import server.PersistenceManager;
import server.ServerRunner;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Optional.of;

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
            Thread.sleep(1000);
            client = new Client();
            client.connect("127.0.0.1", 8005);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() throws IOException {
        deleteNonEmptyDirectory(Paths.get("src/main/resources/workspace"));
        clearDatabase();
        serverThread.interrupt();
    }

    @Test
    public void assignment2_testCase() {
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

        // 5
        response = client.createResource("alice", "alice", "/alice/cursuri/cursuri.java", 1, of("cursuri"));
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 6
        response = client.readResource("bob", "bob", "/alice/cursuri");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 7
        response = client.addRights("bob", "bob", "/alice/cursuri", "role1");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 8
        response = client.addRights("alice", "alice", "/alice/cursuri", "role1");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 9
        response = client.readResource("bob", "bob", "/alice/cursuri/cursuri.java");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, "cursuri"));

        // 10
        response = client.writeResource("alice", "alice", "/alice/cursuri/cursuri.java", "cursuri2");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 11
        response = client.writeResource("bob", "bob", "/alice/cursuri/cursuri.java", "cursuri3");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 12
        response = client.changeRights("root", "root", "role1", "w");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 13
        response = client.writeResource("bob", "bob", "/alice/cursuri/cursuri.java", "cursuri3");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 14
        response = client.readResource("bob", "bob", "/alice/cursuri/cursuri.java");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 15
        response = client.createRole("root", "root", "role2");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 16
        response = client.changeRights("root", "root", "role2", "r");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 17
        response = client.assignRole("root", "root", "bob", "role2");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 18
        response = client.addRights("alice", "alice", "/alice/cursuri", "role2");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 19
        response = client.readResource("bob", "bob", "/alice/cursuri/cursuri.java");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, "cursuri3"));

        response = client.writeResource("bob", "bob", "/alice/cursuri/cursuri.java", "cursuri4");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        response = client.readResource("bob", "bob", "/alice/cursuri/cursuri.java");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, "cursuri4"));
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