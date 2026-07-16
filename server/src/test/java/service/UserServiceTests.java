package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserServiceTests {
    @Test
    public void registerSucceed() {
        UserData testUser = new UserData("username", "password", "email");
        RegisterResult given = UserService.register(testUser);
        RegisterResult correct = new RegisterResult("username", given.authToken());
        Assertions.assertEquals(given, correct);
    }

    @Test
    public void registerFail() {
        UserData testUser = new UserData("username", "password", "email");
        RegisterResult given = UserService.register(testUser);
        Assertions.assertThrows(AlreadyTakenException.class, () -> UserService.register(testUser));
    }

    @Test
    public void loginSucceed() {
        UserData testUser = new UserData("username", "password", "email");
        UserService.register(testUser);
        LoginRequest testLogin = new LoginRequest("username", "password");
        try {
            RegisterResult given = UserService.login(testLogin);
            RegisterResult correct = new RegisterResult("username", given.authToken());
            Assertions.assertEquals(given, correct);
        }
        catch (DataAccessException e) {
            return;
        }
    }

    @Test
    public void loginFail() {
        UserData testUser = new UserData("username", "password", "email");
        UserService.register(testUser);
        LoginRequest testLogin = new LoginRequest("username", "wrong");
        Assertions.assertThrows(DataAccessException.class, () -> UserService.login(testLogin));
    }

    @Test
    public void logoutSucceed() {
        UserData testUser = new UserData("username", "password", "email");
        RegisterResult result = UserService.register(testUser);
        String authToken = result.authToken();
        try {
            UserService.logout(authToken);
            Assertions.assertNull(AuthDAO.getAuth(authToken));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void logoutFail() {
        Assertions.assertThrows(DataAccessException.class, () -> UserService.logout("wrong"));
    }

    @Test
    public void clearSucceed() {
        UserData user1 = new UserData("username", "password", "email");
        UserData user2 = new UserData("username2", "password", "email");
        UserData user3 = new UserData("username3", "password", "email");
        UserDAO.users.add(user1);
        UserDAO.users.add(user2);
        UserDAO.users.add(user3);
        AuthData authData = new AuthData("hello", "user");
        AuthDAO.authDb.add(authData);
        GameData game1 = new GameData(0, "a", "b", "game1", new ChessGame());
        GameData game2 = new GameData(1, "a", "b", "game2", new ChessGame());
        GameDAO.gameDb.add(game1);
        GameDAO.gameDb.add(game2);
        UserService.clear();
        Assertions.assertTrue(UserDAO.users.isEmpty() && AuthDAO.authDb.isEmpty() && GameDAO.gameDb.isEmpty());
    }
}
