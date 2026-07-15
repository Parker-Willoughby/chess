package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
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

    public static String generateToken() {
        return UUID.randomUUID().toString();
    }

}
