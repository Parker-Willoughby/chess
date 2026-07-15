package dataaccess;

import model.UserData;

import java.util.ArrayList;
import java.util.Collection;

public class UserDAO {
    public static Collection<UserData> users = new ArrayList<>();

    public static UserData getUser(String username) {
        for (UserData user: users) {
            if (user.username().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public static void createUser(UserData data) {
        users.add(data);
    }

    public static void clear(){
        users.clear();
    }
}
