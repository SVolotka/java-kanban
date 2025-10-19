package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import exceptions.ErrorResponse;
import http.HttpTaskServer;
import models.HttpMethod;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {
    protected static final String CONTENT_TYPE_JSON = "application/json;charset=utf-8";
    protected final Gson gson;

    public BaseHttpHandler() {
        this.gson = HttpTaskServer.getGson();
    }

    protected void sendSuccess(HttpExchange exchange, String response) throws IOException {
        sendResponse(exchange, response, 200);
    }

    protected void sendCreated(HttpExchange exchange, String response) throws IOException {
        sendResponse(exchange, response, 201);
    }

    protected void sendBadRequest(HttpExchange exchange, String message, String path) throws IOException {
        sendErrorResponse(exchange, message, 400, path); //// 4444
    }

    protected void sendNotFound(HttpExchange exchange, String message, String path) throws IOException {
        sendErrorResponse(exchange, message, 404, path); /// 2222
    }

    protected void sendMethodNotAllowed(HttpExchange exchange, String message, String path) throws IOException {
        sendErrorResponse(exchange, message, 405, path); /// 1111
    }

    protected void sendHasOverlaps(HttpExchange exchange, String message, String path) throws IOException {
        sendErrorResponse(exchange, message, 406, path); /// 3333
    }

    protected void sendInternalError(HttpExchange exchange, String message, String path) throws IOException {
        sendErrorResponse(exchange, message, 500, path);
    }

    private void sendErrorResponse(HttpExchange exchange, String message, int code, String path) throws IOException {
        ErrorResponse errorResponse = new ErrorResponse(message, code, path);
        String response = gson.toJson(errorResponse);
        sendResponse(exchange, response, code);
    }

    protected void sendResponse(HttpExchange exchange, String response, int code) throws IOException {
        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", CONTENT_TYPE_JSON);
        exchange.sendResponseHeaders(code, responseBytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    protected HttpMethod getHttpMethod(String method) {
        try {
            return HttpMethod.valueOf(method);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

}