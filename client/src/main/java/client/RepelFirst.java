package client;

import java.lang.reflect.Array;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import chess.ChessBoard;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import model.*;
import records.*;
import records.GameInfo;
import records.ListResult;
import ui.EscapeSequences;

public class RepelFirst {
    private String visitorName = null;
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private int[] gameIDs = new int[100];
    private int currentIndex = 0;

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
                System.out.print(EscapeSequences.SET_TEXT_COLOR_BLUE + result);
            } catch (Throwable e) {
                var msg = e.toString();
                System.out.print(msg);
            }
        }
        System.out.println();
    }


    private void printPrompt() {
        if (state == State.SIGNEDOUT) {
            System.out.print("\n" + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "[LOGGED OUT] " + ">>> " + EscapeSequences.SET_TEXT_COLOR_GREEN);
        }
        else if (state == State.SIGNEDIN) {
            System.out.print("\n" + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "[LOGGED IN] " + ">>> " + EscapeSequences.SET_TEXT_COLOR_GREEN);
        }
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
                case "list" -> list();
                case "create" -> createGame(params);
                case "join" -> playGame(params);
                case "logout" -> logout();
                case "observe" -> observeGame(params);
                case "quit" -> "quit";
                default -> help();
            };
        } catch (InvalidMoveException ex) {
            return ex.getMessage();
        }
    }

    public String login(String... params) throws InvalidMoveException {
        assertSignedOut();
        if (params.length == 2) {
            visitorName = params[0];
            server.login(new LoginRequest(params[0], params[1]));
            state = State.SIGNEDIN;
            return String.format("You are logged in as %s.", visitorName);
        }
        throw new InvalidMoveException("Wrong number of arguments");
    }

    public String register(String... params) throws InvalidMoveException {
        assertSignedOut();
        if (params.length == 3) {
            visitorName = params[0];
            server.register(new UserData(params[0], params[1], params[2]));
            state = State.SIGNEDIN;
            return String.format("%s is registered and logged in.", visitorName);
        }
        throw new InvalidMoveException("Wrong number of arguments");
    }

    public String list() throws InvalidMoveException {
        assertSignedIn();
        ListResult games = server.list();
        var result = new StringBuilder();
        String toAdd;
        int gameNum = 1;
        for (GameInfo game : games.games()) {
            gameIDs[gameNum - 1] = game.gameID();
            toAdd = gameNum + ": " + game.gameName();
            if (game.whiteUsername() != null) {
                toAdd += ", white user = " + game.whiteUsername();
            }
            if (game.blackUsername() != null) {
                toAdd += ", black user = " + game.blackUsername();
            }
            result.append(toAdd).append('\n');
            gameNum ++;
        }
        currentIndex = gameNum - 1;
        return result.toString();
    }

    public String createGame(String... params) throws InvalidMoveException {
        assertSignedIn();
        if (params.length == 1) {
            CreateResult created = server.create(new CreateRequest(params[0]));
            gameIDs[currentIndex] = created.gameID();
            currentIndex ++;
            return String.format("Game is created with Game Number = %s", currentIndex);
        }
        throw new InvalidMoveException("Wrong number of arguments");
    }

    public String playGame(String... params) throws InvalidMoveException {
        assertSignedIn();
        int id = 0;
        if (params.length == 2) {
            if (params[1].equalsIgnoreCase("WHITE") || params[1].equalsIgnoreCase("BLACK")) {
                try {
                    id = Integer.parseInt(params[0]);
                }
                catch (NumberFormatException e) {
                    throw new InvalidMoveException("Game Number must be a number");
                }
                if (id < 1 || id > 100 || gameIDs[id - 1] == 0) {
                    throw new InvalidMoveException("No game exists. \n" + "If you think one should exist, try list first");
                }
                server.join(new JoinRequest(params[1].toUpperCase(), gameIDs[Integer.parseInt(params[0]) - 1]));
                String board = " ";
                if (params[1].equalsIgnoreCase("WHITE")) {
                    board = buildWhiteBoard();
                } else if (params[1].equalsIgnoreCase("BLACK")) {
                    board = buildBlackBoard();
                }
                return "Game joined" + "\n" + board;
            }
            throw new InvalidMoveException("Incorrect or Invalid player color");
        }
        throw new InvalidMoveException("Wrong number of arguments");
    }

    public String observeGame(String... params) throws InvalidMoveException {
        assertSignedIn();
        if (params.length == 1) {
            int id = 0;
            try {
                id = Integer.parseInt(params[0]);
            } catch (NumberFormatException e) {
                throw new InvalidMoveException("Game Number must be a number");
            }
            if (id < 1 || id > 100 || gameIDs[id - 1] == 0) {
                throw new InvalidMoveException("No game exists. \n" + "If you think one should exist, try list first");
            }
            return buildWhiteBoard();
        }
        throw new InvalidMoveException("Incorrect Number of Arguments");
    }

    public String logout() throws InvalidMoveException {
        assertSignedIn();
        server.logout();
        state = State.SIGNEDOUT;
        return "You have signed out";
    }

    public String help() {
        if (state == State.SIGNEDOUT) {
            return EscapeSequences.SET_TEXT_COLOR_YELLOW +
                    """
                    - help
                    - register <username> <password> <email> (registers a new user)
                    - login <username> <password> (logs in)
                    - quit (quits program)
                    """ + EscapeSequences.RESET_TEXT_COLOR;
        }
        return EscapeSequences.SET_TEXT_COLOR_YELLOW +
                """
                - help
                - list (lists all games)
                - create <game name> (creates a new game)
                - join <Num> <WHITE|BLACK> (joins a game)
                - observe <Num> (observes an active game)
                - logout (logs out)
                """+ EscapeSequences.RESET_TEXT_COLOR;
    }

    private void assertSignedIn() throws InvalidMoveException {
        if (state == State.SIGNEDOUT) {
            throw new InvalidMoveException("You must sign in");
        }
    }

    private void assertSignedOut() throws InvalidMoveException {
        if (state == State.SIGNEDIN) {
            throw new InvalidMoveException("You are already signed in");
        }
    }

    private String buildWhiteBoard() {
        return EscapeSequences.SET_BG_COLOR_LIGHT_GREY + EscapeSequences.SET_TEXT_COLOR_BLACK + "    a  b  c  d  e  f  g  h    " +
                EscapeSequences. RESET_BG_COLOR + "\n" +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 8 " + EscapeSequences.SET_TEXT_COLOR_BLUE +  EscapeSequences.SET_BG_COLOR_WHITE
                + " R " + EscapeSequences.SET_BG_COLOR_BLACK + " N " +
                EscapeSequences.SET_BG_COLOR_WHITE + " B " + EscapeSequences.SET_BG_COLOR_BLACK + " Q " +
                EscapeSequences.SET_BG_COLOR_WHITE + " K " + EscapeSequences.SET_BG_COLOR_BLACK + " B " +
                EscapeSequences.SET_BG_COLOR_WHITE + " N " + EscapeSequences.SET_BG_COLOR_BLACK + " R " + EscapeSequences.SET_TEXT_COLOR_BLACK +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 8 " +  EscapeSequences.RESET_BG_COLOR + "\n" +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 7 " + EscapeSequences.SET_TEXT_COLOR_BLUE +
                EscapeSequences.SET_BG_COLOR_BLACK + " P " + EscapeSequences.SET_BG_COLOR_WHITE + " P " +
                EscapeSequences.SET_BG_COLOR_BLACK + " P " + EscapeSequences.SET_BG_COLOR_WHITE + " P " +
                EscapeSequences.SET_BG_COLOR_BLACK + " P " + EscapeSequences.SET_BG_COLOR_WHITE + " P " +
                EscapeSequences.SET_BG_COLOR_BLACK + " P " + EscapeSequences.SET_BG_COLOR_WHITE + " P " + EscapeSequences.SET_TEXT_COLOR_BLACK +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 7 " + EscapeSequences.RESET_BG_COLOR + "\n" +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 6 " + EscapeSequences.SET_BG_COLOR_WHITE + "   "
                + EscapeSequences.SET_BG_COLOR_BLACK + "   " +
                EscapeSequences.SET_BG_COLOR_WHITE + "   " + EscapeSequences.SET_BG_COLOR_BLACK + "   " +
                EscapeSequences.SET_BG_COLOR_WHITE + "   " + EscapeSequences.SET_BG_COLOR_BLACK + "   " +
                EscapeSequences.SET_BG_COLOR_WHITE + "   " + EscapeSequences.SET_BG_COLOR_BLACK + "   " +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 6 " + EscapeSequences.RESET_BG_COLOR + "\n" +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 5 " + EscapeSequences.SET_BG_COLOR_BLACK + "   "
                + EscapeSequences.SET_BG_COLOR_WHITE + "   " +
                EscapeSequences.SET_BG_COLOR_BLACK + "   " + EscapeSequences.SET_BG_COLOR_WHITE + "   " +
                EscapeSequences.SET_BG_COLOR_BLACK + "   " + EscapeSequences.SET_BG_COLOR_WHITE + "   " +
                EscapeSequences.SET_BG_COLOR_BLACK + "   " + EscapeSequences.SET_BG_COLOR_WHITE + "   " +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 5 " + EscapeSequences.RESET_BG_COLOR + "\n" +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 4 " + EscapeSequences.SET_BG_COLOR_WHITE + "   "
                + EscapeSequences.SET_BG_COLOR_BLACK + "   " +
                EscapeSequences.SET_BG_COLOR_WHITE + "   " + EscapeSequences.SET_BG_COLOR_BLACK + "   " +
                EscapeSequences.SET_BG_COLOR_WHITE + "   " + EscapeSequences.SET_BG_COLOR_BLACK + "   " +
                EscapeSequences.SET_BG_COLOR_WHITE + "   " + EscapeSequences.SET_BG_COLOR_BLACK + "   " +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 4 " + EscapeSequences.RESET_BG_COLOR + "\n" +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 3 " + EscapeSequences.SET_BG_COLOR_BLACK + "   "
                + EscapeSequences.SET_BG_COLOR_WHITE + "   " +
                EscapeSequences.SET_BG_COLOR_BLACK + "   " + EscapeSequences.SET_BG_COLOR_WHITE + "   " +
                EscapeSequences.SET_BG_COLOR_BLACK + "   " + EscapeSequences.SET_BG_COLOR_WHITE + "   " +
                EscapeSequences.SET_BG_COLOR_BLACK + "   " + EscapeSequences.SET_BG_COLOR_WHITE + "   " +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 3 " + EscapeSequences.RESET_BG_COLOR + "\n"
                + EscapeSequences.SET_TEXT_COLOR_BLACK +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 2 " + EscapeSequences.SET_TEXT_COLOR_RED +
                EscapeSequences.SET_BG_COLOR_WHITE + " P " + EscapeSequences.SET_BG_COLOR_BLACK + " P " +
                EscapeSequences.SET_BG_COLOR_WHITE + " P " + EscapeSequences.SET_BG_COLOR_BLACK + " P " +
                EscapeSequences.SET_BG_COLOR_WHITE + " P " + EscapeSequences.SET_BG_COLOR_BLACK + " P " +
                EscapeSequences.SET_BG_COLOR_WHITE + " P " + EscapeSequences.SET_BG_COLOR_BLACK + " P "
                + EscapeSequences.SET_TEXT_COLOR_BLACK +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 2 " + EscapeSequences.RESET_BG_COLOR + "\n" +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 1 " + EscapeSequences.SET_TEXT_COLOR_RED +
                EscapeSequences.SET_BG_COLOR_BLACK + " R " + EscapeSequences.SET_BG_COLOR_WHITE + " N " +
                EscapeSequences.SET_BG_COLOR_BLACK + " B " + EscapeSequences.SET_BG_COLOR_WHITE + " Q " +
                EscapeSequences.SET_BG_COLOR_BLACK + " K " + EscapeSequences.SET_BG_COLOR_WHITE + " B " +
                EscapeSequences.SET_BG_COLOR_BLACK + " N " + EscapeSequences.SET_BG_COLOR_WHITE + " R "
                + EscapeSequences.SET_TEXT_COLOR_BLACK +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 1 " + EscapeSequences.RESET_BG_COLOR + "\n" +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + "    a  b  c  d  e  f  g  h    "
                + EscapeSequences. RESET_BG_COLOR + "\n" + EscapeSequences.RESET_TEXT_COLOR;

    }

    private String realBuildWhiteBoard(ChessBoard board) {
        String printBoard = " ";
        for (int i = 1; i < 9; i ++) {
            for (int j = 1; j < 9; j++) {
                ChessPosition spot = new ChessPosition(i, j);
            }
        }
        return printBoard;
    }

    private String buildBlackBoard() {
        return EscapeSequences.SET_BG_COLOR_LIGHT_GREY + EscapeSequences.SET_TEXT_COLOR_BLACK + "    h  g  f  e  d  c  b  a    " +
                EscapeSequences. RESET_BG_COLOR + "\n" +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 1 " + EscapeSequences.SET_TEXT_COLOR_RED
                +  EscapeSequences.SET_BG_COLOR_WHITE
                + " R " + EscapeSequences.SET_BG_COLOR_BLACK + " N " +
                EscapeSequences.SET_BG_COLOR_WHITE + " B " + EscapeSequences.SET_BG_COLOR_BLACK + " K " +
                EscapeSequences.SET_BG_COLOR_WHITE + " Q " + EscapeSequences.SET_BG_COLOR_BLACK + " B " +
                EscapeSequences.SET_BG_COLOR_WHITE + " N " + EscapeSequences.SET_BG_COLOR_BLACK + " R "
                + EscapeSequences.SET_TEXT_COLOR_BLACK +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 1 " +  EscapeSequences.RESET_BG_COLOR + "\n" +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 2 " + EscapeSequences.SET_TEXT_COLOR_RED +
                EscapeSequences.SET_BG_COLOR_BLACK + " P " + EscapeSequences.SET_BG_COLOR_WHITE + " P " +
                EscapeSequences.SET_BG_COLOR_BLACK + " P " + EscapeSequences.SET_BG_COLOR_WHITE + " P " +
                EscapeSequences.SET_BG_COLOR_BLACK + " P " + EscapeSequences.SET_BG_COLOR_WHITE + " P " +
                EscapeSequences.SET_BG_COLOR_BLACK + " P " + EscapeSequences.SET_BG_COLOR_WHITE + " P "
                + EscapeSequences.SET_TEXT_COLOR_BLACK +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 2 " + EscapeSequences.RESET_BG_COLOR + "\n" +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 3 " + EscapeSequences.SET_BG_COLOR_WHITE + "   "
                + EscapeSequences.SET_BG_COLOR_BLACK + "   " +
                EscapeSequences.SET_BG_COLOR_WHITE + "   " + EscapeSequences.SET_BG_COLOR_BLACK + "   " +
                EscapeSequences.SET_BG_COLOR_WHITE + "   " + EscapeSequences.SET_BG_COLOR_BLACK + "   " +
                EscapeSequences.SET_BG_COLOR_WHITE + "   " + EscapeSequences.SET_BG_COLOR_BLACK + "   " +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 3 " + EscapeSequences.RESET_BG_COLOR + "\n" +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 4 " + EscapeSequences.SET_BG_COLOR_BLACK + "   "
                + EscapeSequences.SET_BG_COLOR_WHITE + "   " +
                EscapeSequences.SET_BG_COLOR_BLACK + "   " + EscapeSequences.SET_BG_COLOR_WHITE + "   " +
                EscapeSequences.SET_BG_COLOR_BLACK + "   " + EscapeSequences.SET_BG_COLOR_WHITE + "   " +
                EscapeSequences.SET_BG_COLOR_BLACK + "   " + EscapeSequences.SET_BG_COLOR_WHITE + "   " +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 4 " + EscapeSequences.RESET_BG_COLOR + "\n" +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 5 " + EscapeSequences.SET_BG_COLOR_WHITE + "   "
                + EscapeSequences.SET_BG_COLOR_BLACK + "   " +
                EscapeSequences.SET_BG_COLOR_WHITE + "   " + EscapeSequences.SET_BG_COLOR_BLACK + "   " +
                EscapeSequences.SET_BG_COLOR_WHITE + "   " + EscapeSequences.SET_BG_COLOR_BLACK + "   " +
                EscapeSequences.SET_BG_COLOR_WHITE + "   " + EscapeSequences.SET_BG_COLOR_BLACK + "   " +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 5 " + EscapeSequences.RESET_BG_COLOR + "\n" +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 6 " + EscapeSequences.SET_BG_COLOR_BLACK + "   "
                + EscapeSequences.SET_BG_COLOR_WHITE + "   " +
                EscapeSequences.SET_BG_COLOR_BLACK + "   " + EscapeSequences.SET_BG_COLOR_WHITE + "   " +
                EscapeSequences.SET_BG_COLOR_BLACK + "   " + EscapeSequences.SET_BG_COLOR_WHITE + "   " +
                EscapeSequences.SET_BG_COLOR_BLACK + "   " + EscapeSequences.SET_BG_COLOR_WHITE + "   " +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 6 " + EscapeSequences.RESET_BG_COLOR + "\n"
                + EscapeSequences.SET_TEXT_COLOR_BLACK +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 7 " + EscapeSequences.SET_TEXT_COLOR_BLUE +
                EscapeSequences.SET_BG_COLOR_WHITE + " P " + EscapeSequences.SET_BG_COLOR_BLACK + " P " +
                EscapeSequences.SET_BG_COLOR_WHITE + " P " + EscapeSequences.SET_BG_COLOR_BLACK + " P " +
                EscapeSequences.SET_BG_COLOR_WHITE + " P " + EscapeSequences.SET_BG_COLOR_BLACK + " P " +
                EscapeSequences.SET_BG_COLOR_WHITE + " P " + EscapeSequences.SET_BG_COLOR_BLACK + " P "
                + EscapeSequences.SET_TEXT_COLOR_BLACK +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 7 " + EscapeSequences.RESET_BG_COLOR + "\n" +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 8 " + EscapeSequences.SET_TEXT_COLOR_BLUE +
                EscapeSequences.SET_BG_COLOR_BLACK + " R " + EscapeSequences.SET_BG_COLOR_WHITE + " N " +
                EscapeSequences.SET_BG_COLOR_BLACK + " B " + EscapeSequences.SET_BG_COLOR_WHITE + " K " +
                EscapeSequences.SET_BG_COLOR_BLACK + " Q " + EscapeSequences.SET_BG_COLOR_WHITE + " B " +
                EscapeSequences.SET_BG_COLOR_BLACK + " N " + EscapeSequences.SET_BG_COLOR_WHITE + " R "
                + EscapeSequences.SET_TEXT_COLOR_BLACK +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + " 8 " + EscapeSequences.RESET_BG_COLOR + "\n" +
                EscapeSequences.SET_BG_COLOR_LIGHT_GREY + "    h  g  f  e  d  c  b  a    "
                + EscapeSequences. RESET_BG_COLOR + "\n" + EscapeSequences.RESET_TEXT_COLOR;
    }
}
