package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class UserService {

    public static RegisterResult register(UserData registerRequest) throws DataAccessException {
        UserData user = UserDAO.getUser(registerRequest.username());
        if (user == null) {
            UserDAO.createUser(registerRequest);
            AuthData authData = new AuthData(generateToken(), registerRequest.username());
            AuthDAO.createAuth(authData);
            return new RegisterResult(registerRequest.username(), authData.authToken());
        }
        else {
            throw new DataAccessException("Error");
        }
    }
    public static RegisterResult login(LoginRequest loginRequest) throws DataAccessException {
        UserData user = UserDAO.getUser(loginRequest.username());
        if (user != null) {
            AuthData authData = new AuthData(generateToken(), loginRequest.username());
            AuthDAO.createAuth(authData);
            return new RegisterResult(loginRequest.username(), authData.authToken());
        }
        else {
            throw new DataAccessException("Error");
        }
    }

    public static void logout(String authToken) throws DataAccessException {
        AuthDAO.deleteAuth(authToken);
    }

    public static void clear() {
        AuthDAO.clear();
        UserDAO.clear();
        GameDAO.clear();
    }

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

}
