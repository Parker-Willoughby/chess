package dataaccess;

import model.UserData;

import java.util.ArrayList;
import java.util.Collection;

public class UserDAO {
    Collection<UserData> users = new ArrayList<>();

    public UserDAO() {
    }

    public UserData getUser(String username) {
        for (UserData user: users) {
            if (user.username().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public void createUser(UserData data) {
        users.add(data);
    }
}
