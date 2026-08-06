package server;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.SQLAuthDAO;
import dataaccess.SQLGameDAO;
import dataaccess.UnauthorizedException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import records.GameInfo;
import websocket.commands.*;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;

public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final ConnectionManager connections = new ConnectionManager();

    @Override
    public void handleConnect(WsConnectContext ctx) {
        System.out.println("Websocket connected");
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) throws IOException{
        int gameId = -1;
        Session session = ctx.session;
        Gson deserialize = new Gson();

        try {
            UserGameCommand firstCommand = deserialize.fromJson(ctx.message(), UserGameCommand.class);
            gameId = firstCommand.getGameID();
            String username = getUsername(firstCommand.getAuthToken());
            switch (firstCommand.getCommandType()) {
                case CONNECT ->  {
                    ConnectCommand command = deserialize.fromJson(ctx.message(), ConnectCommand.class);
                    connect(session, username, (ConnectCommand) command);
                }
                case MAKE_MOVE -> {
                    MakeMoveCommand command = deserialize.fromJson(ctx.message(), MakeMoveCommand.class);
                    makeMove(session, username, (MakeMoveCommand) command);
                }
                case LEAVE -> {
                    LeaveGameCommand command = deserialize.fromJson(ctx.message(), LeaveGameCommand.class);
                    leaveGame(session, username, (LeaveGameCommand) command);
                }
                case RESIGN -> {
                    ResignCommand command = deserialize.fromJson(ctx.message(), ResignCommand.class);
                    resign(session, username, (ResignCommand) command);
                }
            }
        } catch (UnauthorizedException ex) {
            sendMessage(session, gameId, new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error: unauthorized"));
        } catch (Exception ex) {
            sendMessage(session, gameId, new ErrorMessage(ServerMessage.ServerMessageType.ERROR, ex.getMessage()));
        }

    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(Session session, String username, ConnectCommand command) throws IOException, DataAccessException {
        connections.add(command.getGameID(), session);
        var message = String.format("%s has joined the game", username);
        message += String.format(" as %s.", getPlayerColor(username, command.getGameID()));
        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        GameData gameData = SQLGameDAO.getGame(command.getGameID());
        sendMessage(session, command.getGameID(), new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, gameData.game()));
        connections.broadcast(session, notification, command.getGameID());
    }

    private String getPlayerColor(String username, int gameId) throws DataAccessException {
        GameData gameData = SQLGameDAO.getGame(gameId);
        if (gameData.whiteUsername().equals(username)) {
            return "white";
        }
        else if (gameData.blackUsername().equals(username)) {
            return "black";
        }
        else {
            return "an observer";
        }
    }

    private void leaveGame(Session session, String username, LeaveGameCommand command) throws IOException, DataAccessException {
        var message = String.format("%s has left the game", username);
        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, notification, command.getGameID());
        connections.remove(command.getGameID(), session);
        GameData gameData = SQLGameDAO.getGame(command.getGameID());
        ChessGame game = gameData.game();
        GameData newGame = new GameData(gameData.gameID(), null, null, gameData.gameName(), game);
        if (getTeamStatus(username, command.getGameID()).equals("WHITE")) {
            newGame = new GameData(gameData.gameID(), null, gameData.blackUsername(), gameData.gameName(), game);
        }
        else if (getTeamStatus(username, command.getGameID()).equals("BLACK")) {
            newGame = new GameData(gameData.gameID(), gameData.whiteUsername(), null, gameData.gameName(), game);
        }
        else if (getTeamStatus(username, command.getGameID()).equals("OBSERVER")) {
            newGame = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
        }
        SQLGameDAO.updateGame(newGame);
    }

    private void resign(Session session, String username, ResignCommand command) throws IOException, DataAccessException, InvalidMoveException {
        if (getTeamStatus(username, command.getGameID()).equals("OBSERVER")) {
            throw new InvalidMoveException("Error: Not a valid player");
        }
        GameData gameData = SQLGameDAO.getGame(command.getGameID());
        ChessGame game;
        if (gameData.game() != null) {
            game = gameData.game();
        }
        else {
            throw new IOException("Game already ended");
        }
        var message = String.format("%s has resigned", username);
        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(null, notification, command.getGameID());
        connections.remove(command.getGameID(), session);
        GameData newGame = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), null);
        SQLGameDAO.updateGame(newGame);
    }

    public void makeMove(Session session, String username, MakeMoveCommand command) throws DataAccessException {
        try {
            if (getTeamStatus(username, command.getGameID()).equals("OBSERVER")) {
                throw new InvalidMoveException("Error: Not a valid player");
            }
            GameData gameData = SQLGameDAO.getGame(command.getGameID());
            ChessGame game = gameData.game();
            if (getTeamStatus(username, command.getGameID()) != game.getTeamTurn().toString()) {
                throw new InvalidMoveException("Error: Wrong turn");
            }
            ChessMove move = command.getMove();
            int startRow = move.getStartPosition().getRow();
            int startCol = move.getStartPosition().getColumn();
            int endRow = move.getEndPosition().getRow();
            int endCol = move.getEndPosition().getColumn();
            game.makeMove(move);
            GameData newGame = new GameData(gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), game);
            SQLGameDAO.updateGame(newGame);
            connections.broadcast(null, new LoadGameMessage(ServerMessage.ServerMessageType.LOAD_GAME, game), command.getGameID());
            connections.broadcast(session, new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                    String.format("%s made the move (%s, %s) -> (%s, %s)", username, startRow, startCol, endRow, endCol)), command.getGameID());
            if (game.isInCheckmate(ChessGame.TeamColor.WHITE) || game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                connections.broadcast(null, new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, String.format("%s is in Checkmate", getOpponent(username, command.getGameID()))), command.getGameID());
            }
            else if (game.isInStalemate(ChessGame.TeamColor.WHITE) || game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                connections.broadcast(null, new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, String.format("%s is in Stalemate", getOpponent(username, command.getGameID()))), command.getGameID());
            }
            else if (game.isInCheck(ChessGame.TeamColor.WHITE) || game.isInCheckmate(ChessGame.TeamColor.BLACK)) {
                connections.broadcast(null, new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, String.format("%s is in Check", getOpponent(username, command.getGameID()))), command.getGameID());
            }
        } catch (Exception ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }

    private String getOpponent(String username, int gameId) throws DataAccessException {
        GameData data = SQLGameDAO.getGame(gameId);
        if (username.equals(data.whiteUsername()) && data.blackUsername() != null) {
            return data.blackUsername();
        }
        else if (username.equals(data.blackUsername()) && data.whiteUsername() != null) {
            return data.blackUsername();
        }
        else {
            return "absent player";
        }
    }

    private String getUsername(String authToken) throws DataAccessException {
        AuthData data = SQLAuthDAO.getAuth(authToken);
        if (data != null) {
            return data.username();
        }
        else {
            throw new UnauthorizedException("Error: unauthorized");
        }
    }

    private void sendMessage(Session session, int gameId, ServerMessage notification) throws IOException {
        String msg = new Gson().toJson(notification);
        session.getRemote().sendString(msg);
    }

    private String getTeamStatus(String username, int gameId) throws DataAccessException{
        GameData data = SQLGameDAO.getGame(gameId);
        if (data != null && username.equals(data.whiteUsername())) {
            return "WHITE";
        }
        else if (data != null && username.equals(data.blackUsername())) {
            return "BLACK";
        }
        else {
            return "OBSERVER";
        }
    }
}
