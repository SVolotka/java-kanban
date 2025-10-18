package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.TaskValidationException;
import http.HttpTaskServer;
import manager.TaskManager;
import models.Epic;
import models.Subtask;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicsHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager taskManager;
    private final Gson gson;

    public EpicsHandler(TaskManager taskManager) {
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
            Epic epic = taskManager.getEpicByID(id);

            if (epic != null) {
                String response = gson.toJson(epic);
                sendSuccess(exchange, response);
            } else {
                sendNotFound(exchange, "Эпик с id " + id + " не найден", path);
            }
        } else if (urlParts.length == 2) {
            List<Epic> allEpics = taskManager.getAllEpics();
            String response = gson.toJson(allEpics);
            sendSuccess(exchange, response);
        } else if (urlParts.length == 4 && "subtasks".equals(urlParts[3])) {
            int epicId = getIDFromPath(urlParts[2]);
            Epic epic = taskManager.getEpicByID(epicId);

            if (epic == null) {
                sendNotFound(exchange, "Эпик с id " + epicId + " не найден", path);
                return;
            }

            List<Subtask> subtasks = taskManager.getSubtasksByEpic(epicId);
            String response = gson.toJson(subtasks);
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
        Epic epic = gson.fromJson(bodyString, Epic.class);

        if (epic == null) {
            sendBadRequest(exchange, "Неверный формат эпика", path);
            return;
        }

        if (urlParts.length == 3) {
            int id = getIDFromPath(urlParts[2]);
            Epic existingEpic = taskManager.getEpicByID(id);

            if (existingEpic == null) {
                sendNotFound(exchange, "Эпик с id " + id + " не найден", path);
            }

            taskManager.updateEpic(epic);
            String response = gson.toJson(taskManager.getEpicByID(id));
            sendSuccess(exchange, response);
        } else if (urlParts.length == 2) {
            try {
                int id = taskManager.addEpic(epic);
                String response = gson.toJson(taskManager.getEpicByID(id));
                sendCreated(exchange, response);
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
        }

        Integer id = getIDFromPath(urlParts[2]);
        Epic epic = taskManager.deleteEpicByID(id);
        if (epic != null) {
            sendSuccess(exchange, "Эпик с id " + id + " успешно удален!");
        } else {
            sendNotFound(exchange, "Эпик с id " + id + " не найден!", path);
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