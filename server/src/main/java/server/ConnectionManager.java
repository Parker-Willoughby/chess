package server;

import messages.ServerMessage;
import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
public class ConnectionManager {
    public final Map<Integer, Set<Session>> connections = new ConcurrentHashMap<>();

    public void add(int gameId, Session session) {
        if (!connections.containsKey(gameId)) {
            Set<Session> sets = new HashSet<>();
            sets.add(session);
            connections.put(gameId, sets);
        }
        else {
            Set<Session> sets = connections.get(gameId);
            sets.add(session);
        }
    }

    public void remove(Session session) {
        connections.remove(session);
    }

    public void broadcast(Session excludeSession, ServerMessage notification, int gameId) throws IOException {
        String msg = notification.toString();
        for (Session c : connections.values()) {
            if (c.isOpen()) {
                if (!c.equals(excludeSession)) {
                    c.getRemote().sendString(msg);
                }
            }
        }
    }
}
