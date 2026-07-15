package dataaccess;

import model.AuthData;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;

public class GameDAO {
    public static Collection<GameData> gameDb = new ArrayList<>();

    public static void createAuth(AuthData authData) {
        gameDb.add(GameData);
    }
}
