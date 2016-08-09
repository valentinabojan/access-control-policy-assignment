import api.Response;
import api.ResponseType;
import client.Client;
import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientTest {

    private static Client client;

    @BeforeClass
    public static void setup() {
        try {
            client = new Client();
            client.connect("127.0.0.1", 8005);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void tearDown() throws IOException {
        deleteNonEmptyDirectory(Paths.get("src/main/resources/"));
    }

    @Test
    public void givenSeveralClientRequests_WhenTheRequestsAreProcessedByTheServerWorkers_ThenTheFileSystemIsUpdatesCorrectly() {
        Response response;

        // 1
        response = client.createResource("alice", "alice", "/bob/cursuri", 0, null);
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 2
        response = client.createResource("alice", "alice", "/alice/cursuri", 0, null);
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 3
        response = client.createResource("alice", "alice", "/alice/cursuri", 0, null);
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.ALREADY_EXISTING));

        // 4
        response = client.createResource("alice", "alice", "/alice/cursuri", 1, null);
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.ALREADY_EXISTING));

        // 5
        response = client.readResource("bob", "bob", "/alice/cursuri");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 6
        response = client.readResource("alice", "alice", "/alice/cursuri");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, ""));

        // 7
        response = client.changeRights("bob", "bob", "/alice/cursuri", "rw");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 8
        response = client.changeRights("alice", "alice", "/alice/cursuri", "rw");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 9
        response = client.readResource("bob", "bob", "/alice/cursuri");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, ""));

        // 10
        response = client.createResource("alice", "alice", "/alice/cursuri/a.java", 1, "Test");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 11
        response = client.readResource("bob", "bob", "/alice/cursuri");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, "a.java - FILE"));

        // 12
        response = client.readResource("bob", "bob", "/alice/cursuri/a.java");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, "Test"));

        // 13
        response = client.changeRights("alice", "alice", "/alice/cursuri/b.java", "rw");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_EXISTING));

        // 14
        response = client.changeRights("alice", "alice", "/alice/cursuri/a.java", "");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 15
        response = client.readResource("bob", "bob", "/alice/cursuri/a.java");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));
    }

    private static void deleteNonEmptyDirectory(Path path) {
        try {
            if (Files.isDirectory(path))
                Files.list(path).forEach(ClientTest::deleteNonEmptyDirectory);
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}