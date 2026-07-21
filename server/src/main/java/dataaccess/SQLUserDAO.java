package dataaccess;

import com.google.gson.Gson;
import model.UserData;

import java.sql.PreparedStatement;

import model.*;

import java.sql.*;
import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class SQLUserDAO {

    public static void createUser(UserData data) throws DataAccessException {
        var statement = "INSERT INTO user (username, password, email) VALUES (?, ?, ?)";
        executeUpdate(statement, data.username(), data.password(), data.email());
    }

    public static UserData getUser(String username) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM user WHERE username = ?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readUser(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException("Error");
        }
        return null;
    }

    private static UserData readUser(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var password = rs.getString("password");
        var email = rs.getString("email");
        return new UserData(username, password, email);
    }

    public static void clear() throws DataAccessException {
        var statement1 = "TRUNCATE user";
        executeUpdate(statement1);
        var statement2 = "TRUNCATE auth";
        executeUpdate(statement2);
        var statement3 = "TRUNCATE game";
        executeUpdate(statement3);
    }

    public static int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String p) {ps.setString(i + 1, p);}
                    else if (param instanceof Integer p) {ps.setInt(i + 1, p);}
                    else if (param == null) {ps.setNull(i + 1, NULL);}
                }
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }

                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Error");
        }
    }

    private static final String[] CREATE_STATEMENTS = {
            """
            CREATE TABLE IF NOT EXISTS  user (
              `username` varchar(256) NOT NULL,
              `password` varchar(256) NOT NULL,
              `email` varchar(256) NOT NULL,
              PRIMARY KEY (`username`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
            """
            CREATE TABLE IF NOT EXISTS  auth (
              `username` varchar(256) NOT NULL,
              `token` varchar(256) NOT NULL,
              PRIMARY KEY (`token`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """,
            """
            CREATE TABLE IF NOT EXISTS  game (
              `id` int NOT NULL AUTO_INCREMENT,
              `whiteUsername` varchar(256),
              `blackUsername` varchar(256),
              `gameName` varchar(256) NOT NULL,
              `game` TEXT DEFAULT NULL,
              PRIMARY KEY (`id`),
              INDEX(gameName)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };


    public static void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : CREATE_STATEMENTS) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Error");
        }
    }

    public static Boolean isEmpty() throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement1 = "SELECT EXISTS (SELECT 1 FROM user)";
            var statement2 = "SELECT EXISTS (SELECT 1 FROM auth)";
            var statement3 = "SELECT EXISTS (SELECT 1 FROM game)";
            try {
                PreparedStatement ps1 = conn.prepareStatement(statement1);
                PreparedStatement ps2 = conn.prepareStatement(statement2);
                PreparedStatement ps3 = conn.prepareStatement(statement3);
                ResultSet result1 = ps1.executeQuery();
                ResultSet result2 = ps2.executeQuery();
                ResultSet result3 = ps3.executeQuery();

                result1.next();
                result2.next();
                result3.next();

                if (result1.getBoolean(1) || result2.getBoolean(1) || result3.getBoolean(1)) {
                    return false;
                }

            } catch (Exception e) {
                throw new DataAccessException("Error");
            }
        }
        catch (Exception e) {
            throw new DataAccessException("Error");
        }
        return true;
    }
}

