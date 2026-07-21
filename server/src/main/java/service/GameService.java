package service;

import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import service.records.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

public class GameService {
    public static ListResult list(String authToken) throws DataAccessException, UnauthorizedException {
        if (SQLAuthDAO.getAuth(authToken) != null) {
            return SQLGameDAO.listGames();
        }
        else {
            throw new UnauthorizedException("Error");
        }
    }

    public static CreateResult create(String authToken, String gameName) throws DataAccessException, UnauthorizedException {
        if (SQLAuthDAO.getAuth(authToken) != null) {
            GameCreate datar = new GameCreate(null, null, gameName, new ChessGame());
            int gameID = SQLGameDAO.createGame(datar);
            return new CreateResult(gameID);
        }
        else {
            throw new UnauthorizedException("Error");
        }
    }

    public static void join(String authToken, JoinRequest request) throws DataAccessException, AlreadyTakenException, UnauthorizedException {
        GameData game = SQLGameDAO.getGame(request.gameID());
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
            SQLGameDAO.updateGame(newGame);
        }
        else {
            throw new UnauthorizedException("Error");
        }
    }

}
