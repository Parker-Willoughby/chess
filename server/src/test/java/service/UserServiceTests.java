package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.records.LoginRequest;
import service.records.RegisterResult;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class UserServiceTests {

    @Test
    public void registerSucceed() throws DataAccessException{
        UserService.clear();
        UserData testUser = new UserData("username", "password", "email");
        RegisterResult given = UserService.register(testUser);
        RegisterResult correct = new RegisterResult("username", given.authToken());
        Assertions.assertEquals(given, correct);
        UserService.clear();
    }

    @Test
    public void registerFail() throws DataAccessException{
        UserService.clear();
        UserData testUser = new UserData("username", "password", "email");
        RegisterResult given = UserService.register(testUser);
        Assertions.assertThrows(AlreadyTakenException.class, () -> UserService.register(testUser));
        UserService.clear();
    }

    @Test
    public void loginSucceed() throws DataAccessException{
        UserService.clear();
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
        UserService.clear();
    }

    @Test
    public void loginFail() throws DataAccessException{
        UserService.clear();
        UserData testUser = new UserData("username", "password", "email");
        UserService.register(testUser);
        LoginRequest testLogin = new LoginRequest("username", "wrong");
        Assertions.assertThrows(UnauthorizedException.class, () -> UserService.login(testLogin));
        UserService.clear();
    }

    @Test
    public void logoutSucceed() throws DataAccessException {
        UserService.clear();
        UserData testUser = new UserData("username", "password", "email");
        RegisterResult result = UserService.register(testUser);
        String authToken = result.authToken();
        try {
            UserService.logout(authToken);
            Assertions.assertNull(AuthDAO.getAuth(authToken));
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
        UserService.clear();
    }

    @Test
    public void logoutFail() {
        try {
            Assertions.assertThrows(UnauthorizedException.class, () -> UserService.logout("wrong"));
            UserService.clear();
        }
        catch (DataAccessException e) {
        }
    }

    @Test
    public void clearSucceed() {
        try {
            UserData user1 = new UserData("username", "password", "email");
            UserData user2 = new UserData("username2", "password", "email");
            UserData user3 = new UserData("username3", "password", "email");
            RegisterResult register1 = UserService.register(user1);
            RegisterResult register2 = UserService.register(user2);
            RegisterResult register3 = UserService.register(user3);
            String authToken1 = register1.authToken();
            String authToken2 = register2.authToken();
            String authToken3 = register3.authToken();
            GameService.create(authToken1, "game1");
            GameService.create(authToken3, "game2");
            UserService.clear();
            Assertions.assertTrue(SQLUserDAO.isEmpty());
        }
        catch (DataAccessException e) {

        }
    }

}
