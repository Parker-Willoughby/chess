package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
import service.records.LoginRequest;
import service.records.RegisterResult;

import java.util.UUID;

public class UserService {

    public static RegisterResult register(UserData registerRequest) throws AlreadyTakenException {
        UserData user = UserDAO.getUser(registerRequest.username());
        if (user == null) {
            String newPassword = encryptUserPassword(registerRequest.username(), registerRequest.password());
            UserData user = new UserData(registerRequest.username(), newPassword, registerRequest.email());
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

    private static String encryptUserPassword(String username, String clearTextPassword) {
        String hashedPassword = BCrypt.hashpw(clearTextPassword, BCrypt.gensalt());
        // write the hashed password in database along with the user's other information
        return hashedPassword;
    }

    private static boolean verifyUser(String username, String providedClearTextPassword) {
        // read the previously hashed password from the database
        var hashedPassword = readHashedPasswordFromDatabase(username);

        return BCrypt.checkpw(providedClearTextPassword, hashedPassword);
    }

}
