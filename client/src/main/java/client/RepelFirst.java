package client;

import java.security.InvalidKeyException;
import java.util.Arrays;
import java.util.Scanner;

import chess.InvalidMoveException;
import com.google.gson.Gson;
import model.*;
import records.*;
import records.GameInfo;
import records.ListResult;

import static java.awt.Color.BLUE;
import static java.awt.Color.GREEN;

public class RepelFirst {
    private String visitorName = null;
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;

    public RepelFirst(String serverUrl) throws InvalidMoveException {
        server = new ServerFacade(serverUrl);
    }

    public void run() {
        System.out.println(" Chess. Sign in to Chess.");
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")) {
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.print(BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }


//    public void notify(Notification notification) {
//        System.out.println(RED + notification.message());
//        printPrompt();
//    }

    private void printPrompt() {
        System.out.print("\n" + ">>> " + GREEN);
    }


    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd = (tokens.length > 0) ? tokens[0] : "help";
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "help" -> help();
                case "login" -> login(params);
                case "register" -> register(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (InvalidMoveException ex) {
            return ex.getMessage();
        }
    }

    public String login(String... params) throws InvalidMoveException {
        if (params.length >= 1) {
            state = State.SIGNEDIN;
            visitorName = params[0];
            server.login(new LoginRequest(params[0], params[1]));
            return String.format("You are logged in as %s.", visitorName);
        }
        throw new InvalidMoveException("Error");
    }

    public String register(String... params) throws InvalidMoveException {
        if (params.length >= 1) {
            state = State.SIGNEDIN;
            visitorName = params[0];
            server.register(new UserData(params[0], params[1], params[2]));
            return String.format("%s is registered.", visitorName);
        }
        throw new InvalidMoveException("Error");
    }

    public String list() throws InvalidMoveException {
        assertSignedIn();
        ListResult games = server.list();
        var result = new StringBuilder();
        var gson = new Gson();
        for (GameInfo game : games.games()) {
            result.append(gson.toJson(game)).append('\n');
        }
        return result.toString();
    }

    public String createGame(String... params) throws InvalidMoveException {
        assertSignedIn();
        if (params.length == 1) {
            CreateResult created = server.create(new CreateRequest(params[0]));
            return String.format("Game is created with id = %s", created.gameID());
        }
        throw new InvalidMoveException("Error");
    }

    public String playGame(String... params) throws InvalidMoveException {
        assertSignedIn();
        if (params.length == 2) {
            server.join(new JoinRequest(params[1], Integer.parseInt(params[0])));
            return String.format("Game joined");
        }
        throw new InvalidMoveException("Error");
    }

    public String logout() throws InvalidMoveException {
        assertSignedIn();
        server.logout();
        state = State.SIGNEDOUT;
        return "You have signed out";
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return """
                    - help
                    - register <username> <password> <email>
                    - login <username> <password>
                    - quit
                    """;
        }
        return """
                - help
                - list
                - create <game name>
                - join <ID> <WHITE|BLACK>
                - observe <ID>
                - logout
                """;
    }

    private void assertSignedIn() throws InvalidMoveException {
        if (state == State.SIGNEDOUT) {
            throw new InvalidMoveException("You must sign in");
        }
    }
}
