package client;

import java.lang.reflect.Array;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Scanner;

import chess.*;
import com.google.gson.Gson;
import model.*;
import records.*;
import records.GameInfo;
import records.ListResult;
import ui.EscapeSequences;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

public class RepelFirst implements NotificationHandler {
    private String visitorName = null;
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private int[] gameIDs = new int[100];
    private int currentIndex = 0;
    private final WebSocketFacade ws;
    private String userColor;
    private String authToken;
    private ChessGame currentGame;

    public RepelFirst(String serverUrl) throws InvalidMoveException {
        server = new ServerFacade(serverUrl);
        ws = new WebSocketFacade(serverUrl, this);
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
        else if (state == State.INGAME) {
            System.out.print("\n" + EscapeSequences.SET_TEXT_COLOR_MAGENTA + "[IN GAME] " + ">>> " + EscapeSequences.SET_TEXT_COLOR_GREEN);
        }
    }

    public void notify(NotificationMessage notification) {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + notification.getMessage());
        printPrompt();
    }

    public void error(ErrorMessage error) {
        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + error.getErrorMessage());
        printPrompt();
    }

    public void loadGame(LoadGameMessage message) {
        currentGame = message.getGame();
        System.out.println("\n" + buildBoard(null));
        printPrompt();
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
                case "redraw" -> redraw();
                case "leave" -> leave();
                case "move" -> move(params);
                case "resign" -> resign();
                case "highlight" -> highlight(params);
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
            RegisterResult result = server.login(new LoginRequest(params[0], params[1]));
            state = State.SIGNEDIN;
            authToken = result.authToken();
            return String.format("You are logged in as %s.", visitorName);
        }
        throw new InvalidMoveException("Wrong number of arguments");
    }

    public String register(String... params) throws InvalidMoveException {
        assertSignedOut();
        if (params.length == 3) {
            visitorName = params[0];
            RegisterResult result = server.register(new UserData(params[0], params[1], params[2]));
            state = State.SIGNEDIN;
            authToken = result.authToken();
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
                ws.connect(authToken, gameIDs[Integer.parseInt(params[0]) - 1]);
                state = State.INGAME;
                userColor = params[1].toUpperCase();
                return "Game joined" + "\n";
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
            ws.connect(authToken, gameIDs[Integer.parseInt(params[0]) - 1]);
            state = State.INGAME;
            userColor = "WHITE";
            return "You are observing";
        }
        throw new InvalidMoveException("Incorrect Number of Arguments");
    }

    public String leave() throws InvalidMoveException {
        assertInGame();
        ws.leave();
        state = State.SIGNEDIN;
        currentGame = null;
        return "You have left the game";
    }

    public String move(String... params) throws InvalidMoveException {
        assertInGame();
        int startRow = Integer.parseInt(params[0]);
        int startCol = Integer.parseInt(params[1]);
        int endRow = Integer.parseInt(params[2]);
        int endCol = Integer.parseInt(params[3]);
        ChessMove move = new ChessMove(new ChessPosition(startRow, startCol), new ChessPosition(endRow, endCol), null);
        ws.makeMove(move);
        return " ";
    }

    public String resign() throws InvalidMoveException {
        assertInGame();
        Scanner scanner = new Scanner(System.in);
        System.out.println(EscapeSequences.SET_TEXT_COLOR_RED + "Would you like to resign? [Y/N]: ");
        String line = scanner.nextLine();
        if (line.equals("Y")) {
            ws.resign();
            return "You have resigned";
        }
        else if (line.equals("N")) {
            return " ";
        }
        else {
            throw new InvalidMoveException("Please input Y or N");
        }
    }

    public String redraw() throws InvalidMoveException {
        assertInGame();
        return buildBoard(null);
    }

    public String highlight(String... params) throws InvalidMoveException {
        assertInGame();
        int row = Integer.parseInt(params[0]);
        int col = Integer.parseInt(params[1]);
        return buildBoard(new ChessPosition(row, col));
    }

    public String logout() throws InvalidMoveException {
        assertSignedIn();
        server.logout();
        state = State.SIGNEDOUT;
        authToken = null;
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
        else if (state == State.SIGNEDIN) {
            return EscapeSequences.SET_TEXT_COLOR_YELLOW +
                    """
                            - help
                            - list (lists all games)
                            - create <game name> (creates a new game)
                            - join <Num> <WHITE|BLACK> (joins a game)
                            - observe <Num> (observes an active game)
                            - logout (logs out)
                            """ + EscapeSequences.RESET_TEXT_COLOR;
        }
        return EscapeSequences.SET_TEXT_COLOR_YELLOW +
                """
                        - help
                        - redraw (redraws current board)
                        - leave (leaves game)
                        - move <Start Row> <Start Col> <End Row> <End Col> (makes a move)
                        - resign (resigns game)
                        - highlight <Piece Row> <Piece Col> (shows legal moves)
                        """ + EscapeSequences.RESET_TEXT_COLOR;
    }

    private void assertSignedIn() throws InvalidMoveException {
        if (state == State.SIGNEDOUT) {
            throw new InvalidMoveException("You must sign in");
        }
        else if (state == State.INGAME) {
            throw new InvalidMoveException("Leave current game first");
        }
    }

    private void assertSignedOut() throws InvalidMoveException {
        if (state != State.SIGNEDOUT) {
            throw new InvalidMoveException("You are already signed in");
        }
    }

    private void assertInGame() throws InvalidMoveException {
        if (state != State.INGAME) {
            throw new InvalidMoveException("You have not joined a game");
        }
    }

    private String buildBoard(ChessPosition highlight) {
        Collection<ChessPosition> toHighlight = new ArrayList<>();
        if (highlight != null) {
            for (ChessMove move: currentGame.validMoves(highlight)) {
                toHighlight.add(move.getEndPosition());
            }
        }
        ChessBoard board = currentGame.getBoard();
        String printBoard = EscapeSequences.SET_BG_COLOR_LIGHT_GREY + EscapeSequences.SET_TEXT_COLOR_BLACK;
        if (userColor.equals("WHITE")) {
            printBoard += "    a  b  c  d  e  f  g  h    ";
        }
        else {
            printBoard += "    h  g  f  e  d  c  b  a    ";
        }
        printBoard += EscapeSequences. RESET_BG_COLOR + "\n";
        String lastColor;
        for (int i = 1; i < 9; i ++) {
            if (i % 2 == 0) {
                lastColor = "WHITE";
            }
            else {
                lastColor = "BLACK";
            }
            for (int j = 0; j < 10; j++) {
                if (j > 0 && j < 9) {
                    if (lastColor.equals("WHITE")) {
                        printBoard += EscapeSequences.SET_BG_COLOR_BLACK;
                        lastColor = "BLACK";
                    }
                    else {
                        printBoard += EscapeSequences.SET_BG_COLOR_WHITE;
                        lastColor = "WHITE";
                    }
                    ChessPiece piece;
                    if (userColor.equals("WHITE")) {
                        piece = board.getPiece(new ChessPosition(9 - i, j));
                        if (toHighlight.contains(new ChessPosition(9 - i, j))) {
                            printBoard += EscapeSequences.SET_BG_COLOR_YELLOW;
                        }
                    }
                    else {
                        piece = board.getPiece(new ChessPosition(i, 9 - j));
                        if (toHighlight.contains(new ChessPosition(i, 9 - j))) {
                            printBoard += EscapeSequences.SET_BG_COLOR_YELLOW;
                        }
                    }
                    if (piece == null) {
                        printBoard += "   ";
                    }
                    else {
                        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                            printBoard += EscapeSequences.SET_TEXT_COLOR_RED;
                        }
                        else {
                            printBoard += EscapeSequences.SET_TEXT_COLOR_BLUE;
                        }
                        if (piece.getPieceType() == ChessPiece.PieceType.PAWN) {
                            printBoard += " P ";
                        }
                        else if (piece.getPieceType() == ChessPiece.PieceType.KNIGHT) {
                            printBoard += " N ";
                        }
                        else if (piece.getPieceType() == ChessPiece.PieceType.BISHOP) {
                            printBoard += " B ";
                        }
                        else if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
                            printBoard += " R ";
                        }
                        else if (piece.getPieceType() == ChessPiece.PieceType.QUEEN) {
                            printBoard += " Q ";
                        }
                        else if (piece.getPieceType() == ChessPiece.PieceType.KING) {
                            printBoard += " K ";
                        }
                    }
                }
                else {
                    if (userColor.equals("WHITE")) {
                        printBoard += EscapeSequences.SET_BG_COLOR_LIGHT_GREY + EscapeSequences.SET_TEXT_COLOR_BLACK + " " + (9 - i) + " ";
                        if (j == 9) {
                            printBoard += EscapeSequences.RESET_BG_COLOR + "\n";
                        }
                    }
                    else {
                        printBoard += EscapeSequences.SET_BG_COLOR_LIGHT_GREY + EscapeSequences.SET_TEXT_COLOR_BLACK + " " + i + " ";
                        if (j == 9) {
                            printBoard += EscapeSequences.RESET_BG_COLOR + "\n";
                        }
                    }
                }
            }
        }
        printBoard += EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
        if (userColor.equals("WHITE")) {
            printBoard += "    a  b  c  d  e  f  g  h    ";
        }
        else {
            printBoard += "    h  g  f  e  d  c  b  a    ";
        }
        printBoard += EscapeSequences. RESET_BG_COLOR + "\n" + EscapeSequences.RESET_TEXT_COLOR;
        return printBoard;
    }
}
