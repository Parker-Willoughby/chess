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

    @Test
    void logoutSuccess() throws Exception {
        facade.register(new UserData("player1", "password", "p1@email.com"));
        facade.logout();
        assertThrows(InvalidMoveException.class, () -> facade.logout());
    }

    @Test
    void logoutFail() throws Exception {
        assertThrows(InvalidMoveException.class, () -> facade.logout());
    }

    @Test
    void createSuccess() throws Exception {
        facade.register(new UserData("joe", "joe", "joe"));
        CreateResult created = facade.create(new CreateRequest("game"));
        assertTrue(created.gameID() >= 1);
    }

    @Test
    void createFail() throws Exception {
        assertThrows(InvalidMoveException.class, () -> facade.create(new CreateRequest("game")));
    }

    @Test
    void listSuccess() throws Exception {
        facade.register(new UserData("joe", "joe", "joe"));
        facade.create(new CreateRequest("game"));
        facade.create(new CreateRequest("game2"));
        assertTrue(facade.list().games().size() == 2);
    }

    @Test
    void listFail() throws Exception {
        assertThrows(InvalidMoveException.class, () -> facade.list());
    }

    @Test
    void joinSuccess() throws Exception {
        facade.register(new UserData("joe", "joe", "joe"));
        CreateResult result = facade.create(new CreateRequest("game"));
        facade.join(new JoinRequest("WHITE", result.gameID()));
        for (GameInfo game : facade.list().games()) {
            assertEquals(game.whiteUsername(), "joe");
        }
    }

    @Test
    void joinFail() throws Exception {
        facade.register(new UserData("joe", "joe", "joe"));
        assertThrows(InvalidMoveException.class, () -> facade.join(new JoinRequest("WHITE", 7)));
    }

    @Test
    void clearSuccess() throws Exception {
        facade.register(new UserData("joe", "joe", "joe"));
        facade.create(new CreateRequest("game"));
        facade.create(new CreateRequest("game2"));
        facade.clear();
        assertThrows(InvalidMoveException.class, () -> facade.login(new LoginRequest("joe", "joe")));
    }

}
