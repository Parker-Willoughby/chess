package dataaccess;

import com.google.gson.Gson;
import model.GameData;
import service.records.GameInfo;
import service.records.ListResult;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import static dataaccess.SQLUserDAO.executeUpdate;

public class SQLGameDAO {
    public static int createGame(GameData game) throws DataAccessException {
        var statement = "INSERT INTO game (whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?)";
        String json = new Gson().toJson(game.game());
        int id = executeUpdate(statement, game.whiteUsername(), game.blackUsername(), game.gameName(), json);
        return id;
    }

    public ListResult listGames() throws DataAccessException {
        Collection<GameInfo> result = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM game";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readGame(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Error");
        }
        return new ListResult(result);
    }

    public static void updateGame(GameData game) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
                var statement2 = "UPDATE game SET whiteUsername=?, blackUsername=?, gameName=?, game=? WHERE id=?";
                String json = new Gson().toJson(game.game());
                executeUpdate(statement2, game.gameID(), game.whiteUsername(), game.blackUsername(), game.gameName(), json);
        } catch (Exception e) {
            throw new DataAccessException("Error");
        }
    }

    private GameInfo readGame(ResultSet rs) throws SQLException {
        var id = rs.getInt("id");
        var gameName = rs.getString("gameName");
        var whiteUsername = rs.getString("whiteUsername");
        var blackUsername = rs.getString("blackUsername");

        return new GameInfo(id, whiteUsername, blackUsername, gameName);
    }
}
