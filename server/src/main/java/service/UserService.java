package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import service.records.LoginRequest;
import service.records.RegisterResult;

import javax.xml.crypto.Data;
import java.util.UUID;

public class UserService {

    public static RegisterResult register(UserData registerRequest) throws AlreadyTakenException, DataAccessException {
        UserData user = SQLUserDAO.getUser(registerRequest.username());
        if (user == null) {
            String newPassword = encryptUserPassword(registerRequest.username(), registerRequest.password());
            UserData newUser = new UserData(registerRequest.username(), newPassword, registerRequest.email());
            SQLUserDAO.createUser(newUser);
            AuthData authData = new AuthData(generateToken(), registerRequest.username());
            SQLAuthDAO.createAuth(authData);
            return new RegisterResult(registerRequest.username(), authData.authToken());
        }
        else {
            throw new AlreadyTakenException("Error");
        }
    }

    public static RegisterResult login(LoginRequest loginRequest) throws DataAccessException, UnauthorizedException {
        UserData user = SQLUserDAO.getUser(loginRequest.username());
        if (user != null && BCrypt.checkpw(loginRequest.password(), user.password())) {
            AuthData authData = new AuthData(generateToken(), loginRequest.username());
            SQLAuthDAO.createAuth(authData);
            return new RegisterResult(loginRequest.username(), authData.authToken());
        }
        else {
            throw new UnauthorizedException("Error");
        }
    }

    public static void logout(String authToken) throws UnauthorizedException, DataAccessException {
        if (SQLAuthDAO.getAuth(authToken) != null) {
            SQLAuthDAO.deleteAuth(authToken);
        }
        else {
            throw new UnauthorizedException("Error");
        }
    }

    public static void clear() throws DataAccessException {
        SQLUserDAO.clear();
    }

    public static String generateToken() throws DataAccessException{
        String token = UUID.randomUUID().toString();
        while (SQLAuthDAO.getAuth(token) != null) {
            token = UUID.randomUUID().toString();
        }
        return token;
    }

    private static String encryptUserPassword(String username, String clearTextPassword) {
        String hashedPassword = BCrypt.hashpw(clearTextPassword, BCrypt.gensalt());
        // write the hashed password in database along with the user's other information
        return hashedPassword;
    }

}
