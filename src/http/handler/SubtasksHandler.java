package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.TaskValidationException;
import http.HttpTaskServer;
import manager.TaskManager;
import models.Subtask;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtasksHandler extends BaseHttpHandler implements HttpHandler {
    private final TaskManager taskManager;
    private final Gson gson;

    public SubtasksHandler(TaskManager taskManager) {
        this.taskManager = taskManager;
        gson = HttpTaskServer.getGson();
    }


    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();

        try {
            switch (method) {
                case "GET":
                    handleGet(exchange);
                    break;
                case "POST":
                    handlePost(exchange);
                    break;
                case "DELETE":
                    handleDelete(exchange);
                    break;
                default:
                    sendMethodNotAllowed(exchange, String.format(
                            "Обработка данного метода %s не предусмотрена", method), method);
            }
        } catch (Exception e) {
            sendInternalError(exchange, e.getMessage(), method);
        } finally {
            exchange.close();
        }
    }

    private void handleGet(HttpExchange exchange) throws IOException {
        URI requestURI = exchange.getRequestURI();
        String path = requestURI.getPath();
        String[] urlParts = path.split("/");

        if (urlParts.length == 3) {
            Integer id = getIDFromPath(urlParts[2]);
            Subtask subtask = taskManager.getSubtaskByID(id);

            if (subtask != null) {
                String response = gson.toJson(subtask);
                sendSuccess(exchange, response);
            } else {
                sendNotFound(exchange, "Сабтаск с id " + id + " не найден", path);
            }
        } else if (urlParts.length == 2) {
            List<Subtask> allSubtasks = taskManager.getAllSubtasks();
            String response = gson.toJson(allSubtasks);
            sendSuccess(exchange, response);
        } else {
            sendBadRequest(exchange, "Некорректный путь", path);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        URI requestURI = exchange.getRequestURI();
        String path = requestURI.getPath();
        String[] urlParts = path.split("/");

        byte[] bodyBytes = exchange.getRequestBody().readAllBytes();
        String bodyString = new String(bodyBytes, StandardCharsets.UTF_8);
        Subtask subtask = gson.fromJson(bodyString, Subtask.class);

        if (subtask == null) {
            sendBadRequest(exchange, "Неверный формат сабтаска", path);
            return;
        }

        if (urlParts.length == 3) {
            int id = getIDFromPath(urlParts[2]);
            Subtask existingSubtask = taskManager.getSubtaskByID(id);

            if (existingSubtask == null) {
                sendNotFound(exchange, "Сабтаск с id " + id + " не найден", path);
                return;
            }

            taskManager.updateSubtask(subtask);
            String response = gson.toJson(taskManager.getSubtaskByID(id));

            sendSuccess(exchange, response);
        } else if (urlParts.length == 2) {
            try {
                Integer id = taskManager.addSubtask(subtask);

                if (id != null) {
                    String response = gson.toJson(taskManager.getSubtaskByID(id));
                    sendCreated(exchange, response);
                } else {
                    sendNotFound(exchange, "Эпик для подзадачи не найден", path);
                }
            } catch (TaskValidationException e) {
                sendHasOverlaps(exchange, e.getMessage(), path);
            }
        } else {
            sendBadRequest(exchange, "Некорректный путь", path);
        }
    }

    private void handleDelete(HttpExchange exchange) throws IOException {
        URI requestURI = exchange.getRequestURI();
        String path = requestURI.getPath();
        String[] urlParts = path.split("/");

        if (urlParts.length != 3) {
            sendBadRequest(exchange, "Некорректный путь для удаления", path);
            return;
        }

        Integer id = getIDFromPath(urlParts[2]);
        Subtask subtask = taskManager.deleteSubtaskByID(id);
        if (subtask != null) {
            sendSuccess(exchange, "Cабтаск с id " + id + " успешно удален!");
        } else {
            sendNotFound(exchange, "Сабтаск с id " + id + " не найден!", path);
        }
    }

    private Integer getIDFromPath(String id) {
        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Некорректный формат ID: " + id);
        }
    }

}