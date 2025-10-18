package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import http.adapter.DurationAdapter;
import http.adapter.LocalDateTimeAdapter;
import http.handler.EpicsHandler;
import http.handler.HistoryHandler;
import http.handler.PrioritizedHandler;
import http.handler.SubtasksHandler;
import http.handler.TasksHandler;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    public static final int PORT = 8080;

    private final HttpServer server;
    InetSocketAddress address = new InetSocketAddress("localhost", PORT);
    private final TaskManager manager; // = Managers.getDefault();
    private static Gson gson;

    public HttpTaskServer(TaskManager taskManager) throws IOException {
        manager = taskManager;
        server = HttpServer.create(address, 0);
        configureHandlers();
    }

    private void configureHandlers() {
        server.createContext("/tasks", new TasksHandler(manager));
        server.createContext("/epics", new EpicsHandler(manager));
        server.createContext("/subtasks", new SubtasksHandler(manager));
        server.createContext("/history", new HistoryHandler(manager));
        server.createContext("/prioritized", new PrioritizedHandler(manager));
    }

    public static Gson getGson() {
        if (gson == null) {
            gson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    .registerTypeAdapter(Duration.class, new DurationAdapter())
                    .create();
        }
        return gson;
    }

    public static void main(String[] args) {
        try {
            HttpTaskServer server = new HttpTaskServer(Managers.getDefault());
            server.start();
        } catch (IOException e) {
            System.err.println("Failed to start HTTP Task Server: " + e.getMessage());
        }

    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
    }
}
