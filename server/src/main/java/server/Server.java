package server;

import com.google.gson.Gson;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.Context;
import model.UserData;
import service.*;
import service.records.*;

import javax.xml.crypto.Data;
import java.util.Map;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

    }

    public int run(int desiredPort) {
        try {
            SQLUserDAO.configureDatabase();
        }
        catch (DataAccessException e) {
            throw new RuntimeException("Error");
        }
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
        catch (DataAccessException e) {
            ctx.status(500).result(new Gson().toJson(Map.of("message", "Error: internal error")));
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
        catch (UnauthorizedException e) {
            ctx.status(401).result(new Gson().toJson(Map.of("message", "Error: unauthorized")));
        }
        catch (DataAccessException e) {
            ctx.status(500).result(new Gson().toJson(Map.of("message", "Error: Internal Error")));
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
        catch (UnauthorizedException e) {
            ctx.status(401).result(new Gson().toJson(Map.of("message", "Error: unauthorized")));
        }
        catch (DataAccessException e) {
            ctx.status(500).result(new Gson().toJson(Map.of("message", "Error: Internal Error")));
        }

    }

    public void handleList(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            ListResult gamesList = GameService.list(authToken);
            String thingy = returnBodyObject(gamesList);
            ctx.status(200).result(thingy);
        }
        catch (UnauthorizedException e) {
            ctx.status(401).result(new Gson().toJson(Map.of("message", "Error: unauthorized")));
        }
        catch (DataAccessException e) {
            ctx.status(500).result(new Gson().toJson(Map.of("message", "Error: Internal Error")));
        }
    }

    public void handleJoin(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            JoinRequest request = getBodyObject(ctx, JoinRequest.class);
            if (request.gameID() == 0 || request.playerColor() == null
                    || (!request.playerColor().equals("WHITE")) && !request.playerColor().equals("BLACK")) {
                ctx.status(400).result(new Gson().toJson(Map.of("message", "Error: bad request")));
            }
            else {
                GameService.join(authToken, request);
            }
        }
        catch (UnauthorizedException e) {
            ctx.status(401).result(new Gson().toJson(Map.of("message", "Error: unauthorized")));
        }
        catch (AlreadyTakenException e) {
            ctx.status(403).result(new Gson().toJson(Map.of("message", "Error: already taken")));
        }
        catch (DataAccessException e) {
            ctx.status(500).result(new Gson().toJson(Map.of("message", "Error: Internal Error")));
        }
    }

    public void handleLogout(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            UserService.logout(authToken);
        }
        catch (UnauthorizedException e) {
            ctx.status(401).result(new Gson().toJson(Map.of("message", "Error: bad request")));
        }
        catch (DataAccessException e) {
            ctx.status(500).result(new Gson().toJson(Map.of("message", "Error: Internal Error")));
        }
    }

    public void handleClear(Context ctx) {
        try {
            UserService.clear();
        }
        catch (DataAccessException e) {
            ctx.status(500).result(new Gson().toJson(Map.of("message", "Error: Internal Error")));
        }
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
