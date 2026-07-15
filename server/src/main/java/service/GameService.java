package service;

import chess.ChessGame;
import dataaccess.GameDAO;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;

public class GameService {
    public static Collection<GameData> list(String AuthToken) {
        return GameDAO.gameDb;
    }

    public static int create(String AuthToken, String gameName) {
        int GameID = 11111;
        GameData datar = new GameData(GameID null, null, gameName, new ChessGame());

    }

}
