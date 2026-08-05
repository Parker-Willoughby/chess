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
    private String authToken = null;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public RegisterResult register(UserData user) throws InvalidMoveException {
        var request = buildRequest("POST", "/user", user, null);
        var response = sendRequest(request);
        RegisterResult result = handleResponse(response, RegisterResult.class);
        authToken = result.authToken();
        return result;

    }

    public RegisterResult login(LoginRequest login) throws InvalidMoveException {
        var request = buildRequest("POST", "/session", login, null);
        var response = sendRequest(request);
        RegisterResult result = handleResponse(response, RegisterResult.class);
        authToken = result.authToken();
        return result;
    }

    public CreateResult create(CreateRequest create) throws InvalidMoveException {
        var request = buildRequest("POST", "/game", create, authToken);
        var response = sendRequest(request);
        return handleResponse(response, CreateResult.class);
    }

    public ListResult list() throws InvalidMoveException {
        var request = buildRequest("GET", "/game", null, authToken);
        var response = sendRequest(request);
        return handleResponse(response, ListResult.class);
    }

    public String join(JoinRequest join) throws InvalidMoveException {
        var request = buildRequest("PUT", "/game", join, authToken);
        var response = sendRequest(request);
        handleResponse(response, null);
        return authToken;
    }

    public void logout() throws InvalidMoveException {
        var request = buildRequest("DELETE", "/session", null, authToken);
        var response = sendRequest(request);
        handleResponse(response, null);
        authToken = null;
    }

    public void clear() throws InvalidMoveException {
        var request = buildRequest("DELETE", "/db", null, null);
        sendRequest(request);
    }

    private HttpRequest buildRequest(String method, String path, Object body, String token) {
        var request = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (token != null) {
            request.setHeader("authorization", token);
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
                throw InvalidMoveException.fromJson(body);
            }

            throw new InvalidMoveException("Error, request failed");
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

