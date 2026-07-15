package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.*;
import io.javalin.http.Context;
import model.GameData;
import model.UserData;
import service.*;

import java.util.Collection;
import java.util.Map;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

    }

    public int run(int desiredPort) {
        javalin
                .start(desiredPort)
                .post("/user", this::handleRegister)
                .post("/session", this::handleLogin)
                .post("/game", this::handleCreate)
                .get("/game", this::handleList)
                .put("/game", this::handleJoin)
                .delete("/session", this::handleLogout)
                .delete("/db", this::handleClear);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    public void handleRegister(Context ctx) throws DataAccessException {
        UserData datar = getBodyObject(ctx, UserData.class);
        RegisterResult result = UserService.register(datar);
        ctx.json(returnBodyObject(result));
    }

    public void handleLogin(Context ctx) throws DataAccessException {
        LoginRequest login = getBodyObject(ctx, LoginRequest.class);
        RegisterResult result = UserService.login(login);
        ctx.json(returnBodyObject(result));
    }

    public void handleCreate(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        CreateRequest request = getBodyObject(ctx, CreateRequest.class);
        CreateResult result = GameService.create(authToken, request.gameName());
        ctx.json(returnBodyObject(result));
    }

    public void handleList(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        Collection<GameInfo> gamesList = GameService.list(authToken);
        var bodyObject = returnBodyObject(gamesList);
        ctx.json(new Gson().toJson(Map.of("games", gamesList)));
    }

    public void handleJoin(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        JoinRequest request = getBodyObject(ctx, JoinRequest.class);
        GameService.join(authToken, request);
    }

    public void handleLogout(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        UserService.logout(authToken);
    }

    public void handleClear(Context ctx) throws DataAccessException {
        UserService.clear();
    }

    private static <T> T getBodyObject(Context context, Class<T> clazz) {
        var bodyObject = new Gson().fromJson(context.body(), clazz);

        if (bodyObject == null) {
            throw new RuntimeException("missing required body");
        }

        return bodyObject;
    }

    private static <T> String returnBodyObject(T object) {
        var result = new Gson().toJson(object);

        if (result == null) {
            throw new RuntimeException("missing required body");
        }

        return result;
    }
}
