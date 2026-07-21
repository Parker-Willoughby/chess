package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.mysql.cj.x.protobuf.MysqlxConnection;
import kotlin.collections.IndexedValue;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import service.GameService;
import service.UserService;
import service.records.GameCreate;
import service.records.GameInfo;
import service.records.ListResult;

import java.util.ArrayList;
import java.util.Collection;

public class GameDAOTests {
    @Test
    public void createGameSuccess() throws DataAccessException {
        SQLUserDAO.clear();
        GameCreate game = new GameCreate("user1", "user2", "game", new ChessGame());
        int id = SQLGameDAO.createGame(game);
        Assertions.assertEquals(new GameData(id, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game()), SQLGameDAO.getGame(id));
        SQLUserDAO.clear();
    }

    @Test
    public void createGameFail() throws DataAccessException{
        SQLUserDAO.clear();
        GameCreate game = new GameCreate("user1", "user2", null, null);
        Assertions.assertThrows(DataAccessException.class, () -> SQLGameDAO.createGame(game));
        SQLUserDAO.clear();
    }

    @Test
    public void getGameSuccess() throws DataAccessException {
        SQLUserDAO.clear();
        GameCreate game = new GameCreate("Magnus", "Carl", "game3", new ChessGame());
        int id = SQLGameDAO.createGame(game);
        Assertions.assertEquals(new GameData(id, game.whiteUsername(), game.blackUsername(), game.gameName(), game.game()), SQLGameDAO.getGame(id));
        SQLUserDAO.clear();
    }

    @Test
    public void getGameFail() throws DataAccessException {
        SQLUserDAO.clear();
        Assertions.assertEquals(SQLGameDAO.getGame(76), null);
        SQLUserDAO.clear();
    }

    @Test
    public void listSuccess() throws DataAccessException {
        SQLUserDAO.clear();
        GameCreate game = new GameCreate("a", "b", "game1", new ChessGame());
        int id = SQLGameDAO.createGame(game);
        Collection<GameInfo> correctList = new ArrayList<>();
        correctList.add(new GameInfo(id, "a", "b", "game1"));
        ListResult correct = new ListResult(correctList);
        Assertions.assertEquals(correct, SQLGameDAO.listGames());
        SQLUserDAO.clear();
    }

    @Test
    public void  listFail() throws DataAccessException {
        SQLUserDAO.clear();
        Collection<GameInfo> emptyList = new ArrayList<>();
        ListResult result = new ListResult(emptyList);
        Assertions.assertEquals(SQLGameDAO.listGames(), result);
    }

    @Test
    public void updateGameSuccess() throws DataAccessException {
        SQLUserDAO.clear();
        ChessGame gameData = new ChessGame();
        GameCreate game = new GameCreate("user1", "user2", "game", gameData);
        int id = SQLGameDAO.createGame(game);
        try {
            gameData.makeMove(new ChessMove(new ChessPosition(2, 2), new ChessPosition(3, 2), null));
        }
        catch (InvalidMoveException e) {

        }
        GameData newGame = new GameData(id, "user1", "user2", "game", gameData);
        SQLGameDAO.updateGame(newGame);
        Assertions.assertEquals(newGame, SQLGameDAO.getGame(id));
        SQLUserDAO.clear();
    }

    @Test
    public void updateGameFail() throws DataAccessException{
        SQLUserDAO.clear();
        GameData game = new GameData(72, "user1", "user2", null, new ChessGame());
        SQLGameDAO.updateGame(game);
        Assertions.assertEquals(SQLGameDAO.getGame(72), null);
        SQLUserDAO.clear();
    }
}
