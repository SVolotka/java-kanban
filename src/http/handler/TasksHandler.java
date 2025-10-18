package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import exceptions.TaskValidationException;
import http.HttpTaskServer;
import manager.TaskManager;
import models.Task;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TasksHandler extends BaseHttpHandler implements HttpHandler {

    private final TaskManager taskManager;
    private final Gson gson;

    public TasksHandler(TaskManager taskManager) {
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
                    sendMethodNotAllowed(exchange, String.format("Обработка данного метода %s не предусмотрена", method), method);
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
            Task task = taskManager.getTaskByID(id);

            if (task != null) {
                String response = gson.toJson(task);
                sendSuccess(exchange, response);
            } else {
                sendNotFound(exchange, "Задача с id " + id + " не найдена", path);
            }
        } else if (urlParts.length == 2) {
            List<Task> allTasks = taskManager.getAllTasks();
            String response = gson.toJson(allTasks);
            sendSuccess(exchange, response);
        } else {
            sendBadRequest(exchange, "Неверный формат задачи", path);
        }
    }

    private void handlePost(HttpExchange exchange) throws IOException {
        URI requestURI = exchange.getRequestURI();
        String path = requestURI.getPath();
        String[] urlParts = path.split("/");

        byte[] bodyBytes = exchange.getRequestBody().readAllBytes();
        String bodyString = new String(bodyBytes, StandardCharsets.UTF_8);
        Task task = gson.fromJson(bodyString, Task.class);

        if (task == null) {
            sendBadRequest(exchange, "Неверный формат задачи", path);
        }

        if (urlParts.length == 3) {
            int id = getIDFromPath(urlParts[2]);
            Task existingTask = taskManager.getTaskByID(id);

            if (existingTask == null) {
                sendNotFound(exchange, "Задача с id " + id + " не найдена", path);
            }

            taskManager.updateTask(task);
            String response = gson.toJson(taskManager.getTaskByID(id));
            sendSuccess(exchange, response);
        } else if (urlParts.length == 2) {
            try {
                int id = taskManager.addTask(task);
                String response = gson.toJson(taskManager.getTaskByID(id));
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
            return;
        }

        Integer id = getIDFromPath(urlParts[2]);
        Task task = taskManager.deleteTaskByID(id);
        if (task != null) {
            sendSuccess(exchange, "Задача с id " + id + " успешно удалена!");
        } else {
            sendNotFound(exchange, "Задача с id " + id + " не найдена!", path);
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