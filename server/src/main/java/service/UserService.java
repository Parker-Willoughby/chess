package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.RegisterResult;
import model.UserData;

public class UserService {

    public RegisterResult register(UserData registerRequest) {
        UserDAO thingy = new UserDAO();
        UserData user = thingy.getUser(registerRequest.username());
        if (user == null) {
            thingy.createUser(registerRequest);
            AuthData authData = new AuthData(registerRequest.username(), )
            AuthDAO.creatAuth(authData);
        }
    }
}
