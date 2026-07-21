package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AuthDAOTests {
    @Test
    public void createAuthSuccess() throws DataAccessException {
        SQLUserDAO.clear();
        AuthData auth = new AuthData("token", "user");
        SQLAuthDAO.createAuth(auth);
        Assertions.assertEquals(auth, SQLAuthDAO.getAuth("token"));
        SQLUserDAO.clear();
    }

    @Test
    public void createAuthFail() throws DataAccessException{
        SQLUserDAO.clear();
        AuthData auth = new AuthData("token", "user");
        SQLAuthDAO.createAuth(auth);
        Assertions.assertThrows(DataAccessException.class, () -> SQLAuthDAO.createAuth(auth));
        SQLUserDAO.clear();
    }

    @Test
    public void getAuthSuccess() throws DataAccessException {
        SQLUserDAO.clear();
        AuthData auth = new AuthData("secure", "user2");
        SQLAuthDAO.createAuth(auth);
        Assertions.assertEquals(auth, SQLAuthDAO.getAuth("secure"));
        SQLUserDAO.clear();
    }

    @Test
    public void getAuthFail() throws DataAccessException {
        SQLUserDAO.clear();
        Assertions.assertEquals(SQLAuthDAO.getAuth("notsecure"), null);
        SQLUserDAO.clear();
    }

    @Test
    public void deleteAuthSuccess() throws DataAccessException {
        SQLUserDAO.clear();
        AuthData auth = new AuthData("secure", "user2");
        SQLAuthDAO.createAuth(auth);
        SQLAuthDAO.deleteAuth("secure");
        Assertions.assertEquals(null, SQLAuthDAO.getAuth("secure"));
        SQLUserDAO.clear();
    }

    @Test
    public void deleteAuthNegative() throws DataAccessException {
        SQLUserDAO.clear();
        SQLAuthDAO.deleteAuth("notsecure");
        Assertions.assertEquals(SQLAuthDAO.getAuth("notsecure"), null);
        SQLUserDAO.clear();
    }
}
