package dataaccess;

import model.AuthData;
import model.UserData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static dataaccess.SQLUserDAO.executeUpdate;

public class SQLAuthDAO {

    public static void createAuth(AuthData data) throws DataAccessException {
        var statement = "INSERT INTO auth (username, token) VALUES (?, ?)";
        executeUpdate(statement, data.username(), data.authToken());
    }

    public static void deleteAuth(String authToken) throws DataAccessException {
        var statement = "DELETE FROM auth WHERE token=?";
        executeUpdate(statement, authToken);
    }

    public static AuthData getAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM auth WHERE token = ?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readAuth(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Error");
        }
        return null;
    }

    private static AuthData readAuth(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var token = rs.getString("token");
        return new AuthData(token, username);
    }
}
