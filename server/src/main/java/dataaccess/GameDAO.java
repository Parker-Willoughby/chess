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
}
