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

    public static void clear(){
        authDb.clear();
    }
}
