package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.SQLAuthDAO;
import dataaccess.UnauthorizedException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import model.AuthData;
import org.eclipse.jetty.websocket.api.Session;
import websocket.commands.*;
import websocket.messages.ErrorMessage;
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
            connections.broadcast(session, new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error"), gameId);
        } catch (Exception ex) {
            ex.printStackTrace();
            connections.broadcast(session, new ErrorMessage(ServerMessage.ServerMessageType.ERROR, "Error: " + ex.getMessage()), gameId);
        }

    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        System.out.println("Websocket closed");
    }

    private void connect(Session session, String username, ConnectCommand command) throws IOException {
        connections.add(command.getGameID(), session);
        var message = String.format("%s has joined the game", username);
        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, notification, command.getGameID());
    }

    private void leaveGame(Session session, String username, LeaveGameCommand command) throws IOException {
        var message = String.format("%s has left the game", username);
        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, notification, command.getGameID());
        connections.remove(command.getGameID(), session);
    }

    private void resign(Session session, String username, ResignCommand command) throws IOException {
        var message = String.format("%s has left the game", username);
        var notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, message);
        connections.broadcast(session, notification, command.getGameID());
        connections.remove(command.getGameID(), session);
    }

    public void makeMove(Session session, String username, MakeMoveCommand command) throws DataAccessException {
        try {
            //var message = String.format("%s says %s", petName, sound);
            //var notification = new Notification(Notification.Type.NOISE, message);
            //connections.broadcast(null, notification);
        } catch (Exception ex) {
            throw new DataAccessException("Error");
        }
    }

    private String getUsername(String authToken) throws DataAccessException {
        AuthData data = SQLAuthDAO.getAuth(authToken);
        if (data != null) {
            return data.username();
        }
        else {
            throw new UnauthorizedException("Error");
        }
    }
}
