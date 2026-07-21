package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.records.*;

import java.util.ArrayList;
import java.util.Collection;

public class GameServiceTests {

    @Test
    public void listSuccess() throws DataAccessException {
        UserService.clear();
        GameCreate game = new GameCreate("a", "b", "game1", new ChessGame());
        int id = SQLGameDAO.createGame(game);
        String authToken = "Access";
        SQLAuthDAO.createAuth(new AuthData(authToken, "name"));
        Collection<GameInfo> correctList = new ArrayList<>();
        correctList.add(new GameInfo(id, "a", "b", "game1"));
        ListResult correct = new ListResult(correctList);
        try {
            Assertions.assertEquals(correct, GameService.list(authToken));
            UserService.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void  listFail() {
        Assertions.assertThrows(UnauthorizedException.class, () -> GameService.list("invalid"));
    }

    @Test
    public void createSuccess() throws DataAccessException{
        UserService.clear();
        String authToken = "Access";
        SQLAuthDAO.createAuth(new AuthData(authToken, "name"));

        try {
            CreateResult result = GameService.create(authToken, "game1");
            GameData game = new GameData(result.gameID(), null, null, "game1", new ChessGame());
            Assertions.assertEquals(game, SQLGameDAO.getGame(result.gameID()));
            UserService.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void  createFail() {
        Assertions.assertThrows(UnauthorizedException.class, () -> GameService.create("invalid", "game2"));
    }

    @Test
    public void joinSuccess() throws DataAccessException {
        UserService.clear();
        UserData user = new UserData("username", "password", "email");
        try {
            RegisterResult register = UserService.register(user);
            String authToken = register.authToken();
            CreateResult gameResult = GameService.create(authToken, "game1");
            JoinRequest request = new JoinRequest("WHITE", gameResult.gameID());
            GameService.join(authToken, request);
            GameData correctGame = new GameData(gameResult.gameID(), "username", null, "game1", new ChessGame());
            Assertions.assertEquals(correctGame, SQLGameDAO.getGame(gameResult.gameID()));
            UserService.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void  joinFail() {
        Assertions.assertThrows(UnauthorizedException.class, () -> GameService.join("invalid", new JoinRequest("BLACK", 12)));
    }
}
