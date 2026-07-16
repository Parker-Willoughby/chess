package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import service.Records.LoginRequest;
import service.Records.RegisterResult;

import java.util.UUID;

public class UserService {

    public static RegisterResult register(UserData registerRequest) throws AlreadyTakenException {
        UserData user = UserDAO.getUser(registerRequest.username());
        if (user == null) {
            UserDAO.createUser(registerRequest);
            AuthData authData = new AuthData(generateToken(), registerRequest.username());
            AuthDAO.createAuth(authData);
            return new RegisterResult(registerRequest.username(), authData.authToken());
        }
        else {
            throw new AlreadyTakenException("Error");
        }
    }

    public static RegisterResult login(LoginRequest loginRequest) throws DataAccessException {
        UserData user = UserDAO.getUser(loginRequest.username());
        if (user != null && user.password().equals(loginRequest.password())) {
            AuthData authData = new AuthData(generateToken(), loginRequest.username());
            AuthDAO.createAuth(authData);
            return new RegisterResult(loginRequest.username(), authData.authToken());
        }
        else {
            throw new DataAccessException("Error");
        }
    }

    public static void logout(String authToken) throws DataAccessException {
        if (AuthDAO.getAuth(authToken) != null) {
            AuthDAO.deleteAuth(authToken);
        }
        else {
            throw new DataAccessException("Error");
        }
    }

    public static void clear() {
        AuthDAO.clear();
        UserDAO.clear();
        GameDAO.clear();
    }

    public static String generateToken() {
        String token = UUID.randomUUID().toString();
        while (AuthDAO.getAuth(token) != null) {
            token = UUID.randomUUID().toString();
        }
        return token;
    }

}
