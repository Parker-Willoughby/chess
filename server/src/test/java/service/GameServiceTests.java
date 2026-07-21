package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
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
    public void listSuccess() {
        GameData game = new GameData(0, "a", "b", "game1", new ChessGame());
        GameDAO.gameDb.add(game);
        String authToken = "Access";
        AuthDAO.authDb.add(new AuthData(authToken, "name"));
        Collection<GameInfo> correctList = new ArrayList<>();
        correctList.add(new GameInfo(0, "a", "b", "game1"));
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
        Assertions.assertThrows(DataAccessException.class, () -> GameService.list("invalid"));
    }

    @Test
    public void createSuccess() {
        String authToken = "Access";
        AuthDAO.authDb.add(new AuthData(authToken, "name"));

        try {
            CreateResult result = GameService.create(authToken, "game1");
            GameData game = new GameData(result.gameID(), null, null, "game1", new ChessGame());
            Assertions.assertEquals(game, GameDAO.getGame(result.gameID()));
            UserService.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void  createFail() {
        Assertions.assertThrows(DataAccessException.class, () -> GameService.create("invalid", "game2"));
    }

    @Test
    public void joinSuccess() {
        UserData user = new UserData("username", "password", "email");
        try {
            RegisterResult register = UserService.register(user);
            String authToken = register.authToken();
            CreateResult gameResult = GameService.create(authToken, "game1");
            JoinRequest request = new JoinRequest("WHITE", gameResult.gameID());
            GameService.join(authToken, request);
            GameData correctGame = new GameData(gameResult.gameID(), "username", null, "game1", new ChessGame());
            Assertions.assertEquals(correctGame, GameDAO.getGame(gameResult.gameID()));
            UserService.clear();
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }

    }

    @Test
    public void  joinFail() {
        Assertions.assertThrows(DataAccessException.class, () -> GameService.join("invalid", new JoinRequest("BLACK", 12)));
    }
}
