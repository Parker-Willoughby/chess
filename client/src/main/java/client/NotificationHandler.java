package client;

import websocket.messages.*;

public interface NotificationHandler {
    void notify(NotificationMessage notification);
    void error(ErrorMessage error);
    void loadGame(LoadGameMessage load);
}
