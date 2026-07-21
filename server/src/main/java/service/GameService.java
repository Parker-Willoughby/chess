package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import service.records.CreateResult;
import service.records.GameInfo;
import service.records.JoinRequest;
import service.records.ListResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class GameService {
    public static ListResult list(String authToken) throws DataAccessException {
        if (SQLAuthDAO.getAuth(authToken) != null) {
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
        if (SQLAuthDAO.getAuth(authToken) != null) {
            Random rand = new Random();
            int gameID = rand.nextInt(10000);
            GameData datar = new GameData(gameID, null, null, gameName, new ChessGame());
            GameDAO.createGame(datar);
            return new CreateResult(gameID);
        }
        else {
            throw new DataAccessException("Error");
        }
    }

    public static void join(String authToken, JoinRequest request) throws DataAccessException, AlreadyTakenException {
        GameData game = GameDAO.getGame(request.gameID());
        AuthData authData = SQLAuthDAO.getAuth(authToken);
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
