package dataaccess;

import model.AuthData;

import java.util.ArrayList;
import java.util.Collection;

public class AuthDAO {

    public AuthDAO(){

    }

    public static Collection<AuthData> authDb = new ArrayList<>();

    public static void createAuth(AuthData authData) {
        authDb.add(authData);
    }

    public static void deleteAuth(String authToken) {
        authDb.removeIf(data -> data.authToken().equals(authToken));
    }

    public static AuthData getAuth(String authToken) {
        for (AuthData data: authDb) {
            if (data.authToken().equals(authToken)) {
                return data;
            }
        }
        return null;
    }

    public static void clear(){
        authDb.clear();
    }
}
