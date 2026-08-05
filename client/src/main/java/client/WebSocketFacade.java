package client;

import chess.ChessMove;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import websocket.commands.*;
import websocket.messages.*;

import jakarta.websocket.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {

    Session session;
    NotificationHandler notificationHandler;
    String authToken;
    int gameId;

    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws InvalidMoveException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);


            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage notification = new Gson().fromJson(message, ServerMessage.class);
                    notificationHandler.notify(notification);
                }
            });
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new InvalidMoveException("Error");
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    public void connect(String authToken, int gameId) throws InvalidMoveException {
        try {
            var connect = new ConnectCommand(UserGameCommand.CommandType.CONNECT, authToken, gameId);
            this.session.getBasicRemote().sendText(new Gson().toJson(connect));
            this.authToken = authToken;
            this.gameId = gameId;
        } catch (IOException ex) {
            throw new InvalidMoveException("Error");
        }
    }

    public void leave() throws InvalidMoveException {
        try {
            var leave = new LeaveGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameId);
            this.session.getBasicRemote().sendText(new Gson().toJson(leave));
        } catch (IOException ex) {
            throw new InvalidMoveException("Error");
        }
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        try {
            var moveCommand = new MakeMoveCommand(UserGameCommand.CommandType.MAKE_MOVE, authToken, gameId, move);
            this.session.getBasicRemote().sendText(new Gson().toJson(moveCommand));
        } catch (IOException ex) {
            throw new InvalidMoveException("Error");
        }
    }
}
