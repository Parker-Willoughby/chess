package service;

import chess.ChessGame;
import dataaccess.AlreadyTakenException;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;
import service.Records.CreateResult;
import service.Records.GameInfo;
import service.Records.JoinRequest;
import service.Records.ListResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class GameService {
    public static ListResult list(String authToken) throws DataAccessException {
        if (AuthDAO.getAuth(authToken) != null) {
            Collection<GameInfo> gamesList = new ArrayList<>();
            for (GameData game : GameDAO.gameDb) {
                gamesList.add(new GameInfo(game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName()));
            }
            return new ListResult(gamesList);
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

    public static void join(String authToken, JoinRequest request) throws DataAccessException, AlreadyTakenException {
        GameData game = GameDAO.getGame(request.gameID());
        AuthData authData = AuthDAO.getAuth(authToken);
        if (game != null && authData != null) {
            String username = authData.username();
            GameData newGame;
            if (request.playerColor().equals("WHITE") && game.whiteUsername() == null) {
                newGame = new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
            }
            else if (request.playerColor().equals("BLACK") && game.blackUsername() == null) {
                newGame = new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
            }
            else {
                throw new AlreadyTakenException("Error");
            }
            GameDAO.updateGame(newGame);
        }
        else {
            throw new DataAccessException("Error");
        }
    }

}
