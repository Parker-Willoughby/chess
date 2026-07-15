package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static dataaccess.AuthDAO.getAuth;

public class GameService {
    public static Collection<GameInfo> list(String AuthToken) {
        Collection<GameInfo> gamesList = new ArrayList<>();
        for(GameData game: GameDAO.gameDb) {
            gamesList.add(new GameInfo(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName()));
        }
        return gamesList;
    }

    public static int create(String AuthToken, String gameName) {
        Random rand = new Random();
        int GameID = rand.nextInt(10000);
        GameData datar = new GameData(GameID, null, null, gameName, new ChessGame());
        GameDAO.createGame(datar);
        return GameID;
    }

    public static void join(String authToken, JoinRequest request) throws DataAccessException {
        GameData game = GameDAO.getGame(request.gameID());
        String username = AuthDAO.getAuth(authToken).username();
        if (game != null) {
            GameData newGame;
            if (request.playerColor().equals("WHITE")) {
                newGame = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
            }
            else {
                newGame = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
            }
            GameDAO.updateGame(newGame);
        }
        else {
            throw new DataAccessException("Error");
        }
    }

}
