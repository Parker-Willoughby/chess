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
    public static Collection<GameInfo> list(String authToken) throws DataAccessException {
        if (AuthDAO.getAuth(authToken) != null) {
            Collection<GameInfo> gamesList = new ArrayList<>();
            for (GameData game : GameDAO.gameDb) {
                gamesList.add(new GameInfo(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName()));
            }
            return gamesList;
        }
        else {
            throw new DataAccessException("Error");
        }
    }

    public static CreateResult create(String authToken, String gameName) throws DataAccessException {
        if (AuthDAO.getAuth(authToken) != null) {
            Random rand = new Random();
            int GameID = rand.nextInt(10000);
            GameData datar = new GameData(GameID, null, null, gameName, new ChessGame());
            GameDAO.createGame(datar);
            return new CreateResult(GameID);
        }
        else {
            throw new DataAccessException("Error");
        }
    }

    public static void join(String authToken, JoinRequest request) throws DataAccessException {
        GameData game = GameDAO.getGame(request.gameID());
        if (game != null && AuthDAO.getAuth(authToken) != null) {
            String username = AuthDAO.getAuth(authToken).username();
            GameData newGame;
            if (request.playerColor().equals("WHITE")) {
                newGame = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
            }
            else {
                newGame = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
            }
            GameDAO.updateGame(newGame);
        }
        else {
            throw new DataAccessException("Error");
        }
    }

}
