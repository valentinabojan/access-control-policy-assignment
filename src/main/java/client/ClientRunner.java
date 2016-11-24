package client;

import api.Response;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.valueOf;
import static java.util.Optional.ofNullable;

public class ClientRunner {

    private static final String EXIT = "exit";

    public static void main(String... args) {
        if (args.length != 2) {
            System.err.println("Usage: java ClientRunner <host name> <port number>");
            System.exit(1);
        }
        System.out.println("Client started...");

        String hostName = args[0];
        int portNumber = Integer.parseInt(args[1]);
        Client client = new Client();

        try (Scanner scanner = new Scanner(System.in)) {
            client.connect(hostName, portNumber);
            processCommands(client, scanner);
        } catch (UnknownHostException e) {
            System.err.println("Client doesn't know about host " + hostName);
        } catch (IOException e) {
            System.err.println("Client couldn't get I/O for the connection to " + hostName);
        }
    }

    private static void processCommands(Client client, Scanner scanner) throws IOException {
        String commandString;
        while ((commandString = scanner.nextLine()) != null) {
            if (commandString.isEmpty()) {
                continue;
            }

            System.out.println(commandString);

            if (commandString.equals(EXIT)) {
                return;
            }

            Pattern pattern = Pattern.compile("(.+?)\\((.+?),\\s?(.+?),\\s?(.+?)(?:,\\s?(.+?))?(?:,\\s?(.+?))?\\)");
            Matcher matcher = pattern.matcher(commandString.trim());
            if (!matcher.matches()) {
                System.err.println("Invalid command format");
                continue;
            }

            String commandTypeString = matcher.group(1);
            switch (commandTypeString) {
                case "createResource":
                    processCreateResourceCommand(client, matcher);
                    break;
                case "readResource":
                    processReadResourceCommand(client, matcher);
                    break;
                case "writeResource":
                    processWriteResourceCommand(client, matcher);
                    break;
                case "changeRights":
                    processChangeRightsCommand(client, matcher);
                    break;
                case "addRights":
                    processAddRightsCommand(client, matcher);
                    break;
                case "assignRole":
                    processAssignRoleCommand(client, matcher);
                    break;
                case "createRole":
                    processCreateRoleCommand(client, matcher);
                    break;
                case "createUser":
                    processCreateUserCommand(client, matcher);
                    break;
            }
        }
    }

    private static void printResponse(Response response) throws IOException {
        System.out.println(response);
    }

    private static void processCreateResourceCommand(Client client, Matcher matcher) throws IOException {
        String username = matcher.group(2);
        String password = matcher.group(3);
        String resourceName = matcher.group(4);
        String fileTypeString = matcher.group(5);

        if (fileTypeString == null || (!fileTypeString.equals("0") && !fileTypeString.equals("1"))) {
            System.err.println("Invalid createResource command format");
            return;
        }

        String resourceValue = matcher.group(6) != null ? matcher.group(6).replace("\"", "") : null;
        printResponse(client.createResource(username, password, resourceName, valueOf(fileTypeString), ofNullable(resourceValue)));
    }

    private static void processReadResourceCommand(Client client, Matcher matcher) throws IOException {
        String username = matcher.group(2);
        String password = matcher.group(3);
        String resourceName = matcher.group(4);

        printResponse(client.readResource(username, password, resourceName));
    }

    private static void processWriteResourceCommand(Client client, Matcher matcher) throws IOException {
        String username = matcher.group(2);
        String password = matcher.group(3);
        String resourceName = matcher.group(4);
        String resourceValue = matcher.group(5);

        if (resourceValue == null) {
            System.err.println("Invalid writeResource command format");
            return;
        }

        printResponse(client.writeResource(username, password, resourceName, resourceValue.replace("\"", "")));
    }

    private static void processChangeRightsCommand(Client client, Matcher matcher) throws IOException {
        String username = matcher.group(2);
        String password = matcher.group(3);
        String roleName = matcher.group(4);
        String resourcePermissions = matcher.group(5);

        if (resourcePermissions == null) {
            System.err.println("Invalid changeRights command format");
            return;
        }

        printResponse(client.changeRights(username, password, roleName, resourcePermissions.replace("\"", "")));
    }

    private static void processAddRightsCommand(Client client, Matcher matcher) throws IOException {
        String username = matcher.group(2);
        String password = matcher.group(3);
        String resourceName = matcher.group(4);
        String roleName = matcher.group(5);

        if (roleName == null) {
            System.err.println("Invalid addRights command format");
            return;
        }

        printResponse(client.addRights(username, password, resourceName, roleName));
    }

    private static void processAssignRoleCommand(Client client, Matcher matcher) throws IOException {
        String username = matcher.group(2);
        String password = matcher.group(3);
        String targetUsername = matcher.group(4);
        String roleName = matcher.group(5);

        if (roleName == null) {
            System.err.println("Invalid assignRole command format");
            return;
        }

        printResponse(client.assignRole(username, password, targetUsername, roleName));
    }

    private static void processCreateRoleCommand(Client client, Matcher matcher) throws IOException {
        String username = matcher.group(2);
        String password = matcher.group(3);
        String roleName = matcher.group(4);

        printResponse(client.createRole(username, password, roleName));
    }

    private static void processCreateUserCommand(Client client, Matcher matcher) throws IOException {
        String username = matcher.group(2);
        String password = matcher.group(3);
        String targetUsername = matcher.group(4);
        String targetUserPassword = matcher.group(5);

        if (targetUserPassword == null) {
            System.err.println("Invalid createUser command format");
            return;
        }

        printResponse(client.createUser(username, password, targetUsername, targetUserPassword));
    }
}
