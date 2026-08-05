package client;

import websocket.messages.*;

public interface NotificationHandler {
    void notify(NotificationMessage notification);
}
