package dataaccess;

import model.AuthData;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;

public class GameDAO {
    public static Collection<GameData> gameDb = new ArrayList<>();

    public static void createGame(GameData GameData) {
        gameDb.add(GameData);
    }

    public static void clear(){
        gameDb.clear();
    }

    public static GameData getGame(int gameID) throws DataAccessException {
        for (GameData data: gameDb) {
            if (data.gameID() == gameID) {
                return data;
            }
        }
        return null;
    }

    public static void updateGame(GameData game) {
        for (GameData data: gameDb) {
            if(data.gameID() == game.gameID()) {
                gameDb.remove(data);
                gameDb.add(game);
            }
        }
    }
}
