package client;

import chess.InvalidMoveException;
import com.google.gson.Gson;
import model.*;
import records.*;

import java.net.*;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public RegisterResult register(UserData user) throws InvalidMoveException {
        var request = buildRequest("POST", "/user", user);
        var response = sendRequest(request);
        return handleResponse(response, RegisterResult.class);
    }

    public RegisterResult register(LoginRequest login) throws InvalidMoveException {
        var request = buildRequest("POST", "/session", login);
        var response = sendRequest(request);
        return handleResponse(response, RegisterResult.class);
    }

    public CreateResult create(CreateRequest create) throws InvalidMoveException {
        var request = buildRequest("POST", "/game", create);
        var response = sendRequest(request);
        return handleResponse(response, CreateResult.class);
    }

    public ListResult list() throws InvalidMoveException {
        var request = buildRequest("GET", "/game", null);
        var response = sendRequest(request);
        return handleResponse(response, ListResult.class);
    }

    public void join(JoinRequest join) throws InvalidMoveException {
        var request = buildRequest("PUT", "/game", join);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public void logout() throws InvalidMoveException {
        //var path = String.format("/pet/%s", id);
        var request = buildRequest("DELETE", "/session", null);
        var response = sendRequest(request);
        handleResponse(response, null);
    }

    public void clear() throws InvalidMoveException {
        var request = buildRequest("DELETE", "/db", null);
        sendRequest(request);
    }

    private HttpRequest buildRequest(String method, String path, Object body) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            request.setHeader("Content-Type", "application/json");
        }
        return request.build();
    }

    private BodyPublisher makeRequestBody(Object request) {
        if (request != null) {
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws InvalidMoveException {
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new InvalidMoveException("Error");
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws InvalidMoveException {
        var status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            if (body != null) {
                throw new InvalidMoveException("Error");
            }

            throw new InvalidMoveException("Error");
        }

        if (responseClass != null) {
            return new Gson().fromJson(response.body(), responseClass);
        }

        return null;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}

