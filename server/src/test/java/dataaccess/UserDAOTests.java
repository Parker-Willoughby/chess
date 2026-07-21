package dataaccess;

import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;

public class UserDAOTests {
    @Test
    public void createUserSuccess() throws DataAccessException {
        UserData user = new UserData("username", "passwrod", "email");
        SQLUserDAO.createUser(user);
        Assertions.assertEquals(user, SQLUserDAO.getUser("username"));
        UserDAO.clear();
    }

}
