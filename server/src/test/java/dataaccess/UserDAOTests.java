package dataaccess;

import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.xml.crypto.Data;

public class UserDAOTests {
    @Test
    public void createUserSuccess() throws DataAccessException {
        SQLUserDAO.clear();
        UserData user = new UserData("username", "password", "email");
        SQLUserDAO.createUser(user);
        Assertions.assertEquals(user, SQLUserDAO.getUser("username"));
        SQLUserDAO.clear();
    }

    @Test
    public void createUserFail() throws DataAccessException{
        SQLUserDAO.clear();
        UserData user = new UserData("username", "password", "email");
        SQLUserDAO.createUser(user);
        Assertions.assertThrows(DataAccessException.class, () -> SQLUserDAO.createUser(user));
        SQLUserDAO.clear();
    }

    @Test
    public void getUserSuccess() throws DataAccessException {
        SQLUserDAO.clear();
        UserData user = new UserData("joe", "1234", "email.email");
        SQLUserDAO.createUser(user);
        Assertions.assertEquals(user, SQLUserDAO.getUser("joe"));
        SQLUserDAO.clear();
    }

    @Test
    public void getUserFail() throws DataAccessException {
        SQLUserDAO.clear();
        Assertions.assertEquals(SQLUserDAO.getUser("jeff"), null);
        SQLUserDAO.clear();
    }

    @Test
    public void clearSuccess() throws DataAccessException {
        UserData user = new UserData("username", "password", "email");
        UserData user2 = new UserData("username2", "password", "email");
        SQLUserDAO.createUser(user);
        SQLUserDAO.createUser(user2);
        SQLUserDAO.clear();
        Assertions.assertTrue(SQLUserDAO.isEmpty());
    }

}
