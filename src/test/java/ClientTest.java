import api.Response;
import api.ResponseType;
import client.Client;
import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import server.ServerRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static java.util.Optional.empty;
import static java.util.Optional.of;

public class ClientTest {

    private static Client client;
    private static Thread serverThread;

    @Before
    public void setup() {
        serverThread = new Thread(() -> {
            try {
                ServerRunner.main("1234");
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();

        try {
            Thread.sleep(1000);
            client = new Client();
            client.connect("127.0.0.1", 1234);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearDown() throws IOException {
        deleteNonEmptyDirectory(Paths.get("src/main/resources"));
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
    public void givenSeveralClientRequests_WhenTheRequestsAreProcessedByTheServerWorkers_ThenTheFileSystemIsUpdatedCorrectly() {
        Response response;

        // 1
        response = client.createResource("alice", "alice", "/bob/cursuri", 0, empty());
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));

        // 2
        response = client.createResource("alice", "alice", "/alice/cursuri", 0, empty());
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 3
        response = client.createResource("alice", "alice", "/alice/cursuri", 0, empty());
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.ALREADY_EXISTING));

        // 4
        response = client.createResource("alice", "alice", "/alice/cursuri", 1, empty());
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
        response = client.createResource("alice", "alice", "/alice/cursuri/a.java", 1, of("Test"));
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 11
        response = client.readResource("bob", "bob", "/alice/cursuri");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, "a.java"));

        // 12
        response = client.readResource("bob", "bob", "/alice/cursuri/a.java");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, "Test"));

        // 13
        response = client.changeRights("alice", "alice", "/alice/cursuri/b.java", "rw");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_EXISTING));

        // 16 - write test
        response = client.writeResource("bob", "bob", "/alice/cursuri/a.java", "writeTest");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 17 - write test
        response = client.readResource("bob", "bob", "/alice/cursuri/a.java");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK, "writeTest"));

        // 14
        response = client.changeRights("alice", "alice", "/alice/cursuri/a.java", "");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.OK));

        // 15
        response = client.readResource("bob", "bob", "/alice/cursuri/a.java");
        Assertions.assertThat(response).isEqualTo(new Response(ResponseType.NOT_AUTHORIZED));
    }
}