package client;

import chess.InvalidMoveException;
import model.*;
import records.*;
import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(String.format("http://localhost:%s", port));
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clear() throws Exception {
        facade.clear();
    }

    @Test
    void registerSuccess() throws Exception {
        var authData = facade.register(new UserData("player1", "password", "p1@email.com"));
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    void registerFail() throws Exception {
        facade.register(new UserData("player1", "password", "p1@email.com"));
        assertThrows(InvalidMoveException.class, () -> facade.register(new UserData("player1", "password", "email")));
    }

    @Test
    void loginSuccess() throws Exception {
        facade.register(new UserData("player1", "password", "p1@email.com"));
        facade.logout();
        var authData = facade.login(new LoginRequest("player1", "password"));
        assertTrue(authData.authToken().length() > 10);
    }

    @Test
    void loginFail() throws Exception {
        facade.register(new UserData("player1", "password", "p1@email.com"));
        assertThrows(InvalidMoveException.class, () -> facade.login(new LoginRequest("player1", "pass")));
    }

}
