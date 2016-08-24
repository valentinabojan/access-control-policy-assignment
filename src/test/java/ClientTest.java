import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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

    @Test
    public void assignment3_testCase() {
        Response response;

        // 1
        response = client.createRole("root", "root", "role1");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 2
        response = client.createPermission("bob", "bob", "perm1", "r");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 3
        response = client.createPermission("root", "root", "perm1", "r");
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
        response = client.createResource("alice", "alice", "/alice/cursuri.java", 1, "cursuri");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 6
        response = client.assignPermission("bob", "bob", "/alice/cursuri.java", "role1");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 7
        response = client.assignPermission("alice", "alice", "/alice/cursuri.java", "role1");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 8
        response = client.readResource("bob", "bob", "/alice/cursuri.java");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, "cursuri"));

        response = client.addPermissionToRole("bob", "bob", "role1", "perm1");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 9
        response = client.addPermissionToRole("root", "root", "role1", "perm1");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 10
        response = client.readResource("bob", "bob", "/alice/cursuri.java");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, "cursuri"));

        // 11
        response = client.createRole("root", "root", "role2");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));







//        // 10
//        response = client.writeResource("alice", "alice", "/alice/cursuri.java", "cursuri2");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));
//
//        // 11
//        response = client.writeResource("bob", "bob", "/alice/cursuri.java", "cursuri3");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));
//
//        // 12
//        response = client.changeRights("root", "root", "role1", "w");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));
//
//        // 13
//        response = client.writeResource("bob", "bob", "/alice/cursuri.java", "cursuri3");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));
//
//        // 14
//        response = client.readResource("bob", "bob", "/alice/cursuri.java");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));
//
//        // 16
//        response = client.changeRights("root", "root", "role2", "r");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));
//
//        // 17
//        response = client.assignRole("root", "root", "bob", "role2");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));
//
//        // 18
//        response = client.assignPermission("alice", "alice", "/alice/cursuri.java", "role2");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));
//
//        // 19
//        response = client.readResource("bob", "bob", "/alice/cursuri.java");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, "cursuri3"));
//
//        response = client.writeResource("bob", "bob", "/alice/cursuri.java", "cursuri4");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));
//
//        response = client.readResource("bob", "bob", "/alice/cursuri.java");
//        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, "cursuri4"));
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