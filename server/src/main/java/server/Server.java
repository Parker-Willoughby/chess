package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.*;
import io.javalin.http.Context;
import model.UserData;
import service.RegisterResult;
import service.UserService;

public class Server {

    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.

    }

    public int run(int desiredPort) {
        javalin
                .start(desiredPort)
                .post("/user", this::handleRequest);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    public void handleRequest(Context ctx) throws DataAccessException {
        UserData datar = getBodyObject(ctx, UserData.class);
        RegisterResult result = UserService.register(datar);
        ctx.json(returnBodyObject(result));
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
