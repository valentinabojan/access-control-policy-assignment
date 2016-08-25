import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import psd.api.*;
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
        clearDatabase();
        Response response;

        // 1
        response = client.createRole("root", "root", "role1");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        response = client.createPermission("bob", "bob", "perm1", "r");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 2
        response = client.createPermission("root", "root", "perm1", "r");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        response = client.createUser("bob", "bob", "bob", "bob");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        response = client.createUser("root", "root", "bob", "bob");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        response = client.assignRole("bob", "bob", "bob", "role1");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 3
        response = client.assignRole("root", "root", "bob", "role1");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 4
        response = client.createResource("alice", "alice", "/alice/cursuri.java", 1, "cursuri");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        response = client.assignPermission("bob", "bob", "/alice/cursuri.java", "perm1");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 5
        response = client.assignPermission("alice", "alice", "/alice/cursuri.java", "perm1");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        response = client.readResource("bob", "bob", "/alice/cursuri.java");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        response = client.addPermissionToRole("bob", "bob", "role1", "perm1");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 6
        response = client.addPermissionToRole("root", "root", "role1", "perm1");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 7
        response = client.readResource("bob", "bob", "/alice/cursuri.java");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, "cursuri"));

        // 8
        response = client.createRole("root", "root", "role2");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 9
        response = client.revokeRole("root", "root", "bob", "role1");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 10
        response = client.readResource("bob", "bob", "/alice/cursuri.java");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 11
        response = client.assignRole("root", "root", "bob", "role2");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 12
        response = client.readResource("bob", "bob", "/alice/cursuri.java");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 13
        response = client.createHierarchy("root", "root", "role1", "role2");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 14
        response = client.readResource("bob", "bob", "/alice/cursuri.java");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, "cursuri"));

        // 15
        response = client.createRole("root", "root", "role3");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 16
        response = client.createConstraint("root", "root", "role1", "role3");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 17
        response = client.assignRole("root", "root", "bob", "role3");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.FORBIDDEN));
    }

    private void clearDatabase() {
        EntityManager em = PersistenceManager.INSTANCE.getEntityManager();
        em.getTransaction().begin();

        TypedQuery<User> userQuery = em.createQuery("SELECT u FROM User u", User.class);
        userQuery.getResultList().stream().forEach(em::remove);

        TypedQuery<Constraint> constraintQuery = em.createQuery("SELECT c FROM Constraint c", Constraint.class);
        constraintQuery.getResultList().stream().forEach(em::remove);

        TypedQuery<Role> roleQuery = em.createQuery("SELECT r FROM Role r", Role.class);
        roleQuery.getResultList().stream().forEach(em::remove);

        TypedQuery<Permission> permissionQuery = em.createQuery("SELECT p FROM Permission p", Permission.class);
        permissionQuery.getResultList().stream().forEach(em::remove);

        TypedQuery<RoleHierarchy> hierarchyQuery = em.createQuery("SELECT rh FROM RoleHierarchy rh", RoleHierarchy.class);
        hierarchyQuery.getResultList().stream().forEach(em::remove);

        em.getTransaction().commit();
        em.close();
    }

}