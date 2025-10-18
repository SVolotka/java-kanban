package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import http.HttpTaskServer;
import manager.TaskManager;
import models.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public HistoryHandler(TaskManager tasksManager) {
        this.taskManager = tasksManager;
        gson = HttpTaskServer.getGson();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        try {
            switch (method) {
                case "GET": {
                    handleGet(exchange);
                    break;
                }
                default: {
                    sendMethodNotAllowed(exchange, String.format(
                            "Обработка данного метода %s не предусмотрена", method), method);
                }
            }
        } catch (Exception e) {
            sendInternalError(exchange, e.getMessage(), method);
        } finally {
            exchange.close();
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        List<Task> history = taskManager.getHistory();
        String response = gson.toJson(history);
        sendSuccess(exchange, response);
    }
}