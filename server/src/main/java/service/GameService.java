package service;

import dataaccess.GameDAO;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;

public class GameService {
    public static Collection<GameData> list(String AuthToken) {
        return GameDAO.gameDb;
    }

}
