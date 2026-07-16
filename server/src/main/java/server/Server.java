package server;

import com.google.gson.Gson;
import dataaccess.AlreadyTakenException;
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

    public void handleRegister(Context ctx) {
        try {
            UserData datar = getBodyObject(ctx, UserData.class);
            if (datar.username() == null || datar.password() == null || datar.email() == null) {
                ctx.status(400).result(new Gson().toJson(Map.of("message", "Error: bad request")));
            }
            else {
                RegisterResult result = UserService.register(datar);
                ctx.json(returnBodyObject(result));
            }
        }
        catch (AlreadyTakenException e) {
            ctx.status(403).result(new Gson().toJson(Map.of("message", "Error: username already taken")));
        }
    }

    public void handleLogin(Context ctx) {
        try {
            LoginRequest login = getBodyObject(ctx, LoginRequest.class);
            if (login.username() == null || login.password() == null) {
                ctx.status(400).result(new Gson().toJson(Map.of("message", "Error: bad request")));
            } else {
                RegisterResult result = UserService.login(login);
                ctx.json(returnBodyObject(result));
            }
        }
        catch (DataAccessException e) {
            ctx.status(401).result(new Gson().toJson(Map.of("message", "Error: unauthorized")));
        }
    }

    public void handleCreate(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            CreateRequest request = getBodyObject(ctx, CreateRequest.class);
            if (request.gameName() == null || authToken == null) {
                ctx.status(400).result(new Gson().toJson(Map.of("message", "Error: bad request")));
            }
            else {
                CreateResult result = GameService.create(authToken, request.gameName());
                ctx.json(returnBodyObject(result));
            }
        }
        catch (DataAccessException e) {
            ctx.status(401).result(new Gson().toJson(Map.of("message", "Error: unauthorized")));
        }
    }

    public void handleList(Context ctx) throws DataAccessException {
        try {
            String authToken = ctx.header("authorization");
            Collection<GameInfo> gamesList = GameService.list(authToken);
            var bodyObject = returnBodyObject(gamesList);
            ctx.json(new Gson().toJson(Map.of("games", gamesList)));
        }
        catch (DataAccessException e) {
            ctx.status(401).result(new Gson().toJson(Map.of("message", "Error: unauthorized")));
        }
    }

    public void handleJoin(Context ctx) throws DataAccessException {
        String authToken = ctx.header("authorization");
        JoinRequest request = getBodyObject(ctx, JoinRequest.class);
        GameService.join(authToken, request);
    }

    public void handleLogout(Context ctx) throws DataAccessException {
        try {
            String authToken = ctx.header("authorization");
            UserService.logout(authToken);
        }
        catch (DataAccessException e) {
            ctx.status(401).result(new Gson().toJson(Map.of("message", "Error: unauthorized")));
        }
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
