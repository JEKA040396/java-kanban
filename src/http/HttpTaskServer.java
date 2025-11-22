package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import http.handler.*;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .setPrettyPrinting()
            .create();

    private final HttpServer server;
    private final TaskManager manager;

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        this.server = HttpServer.create(new InetSocketAddress(8080), 0);
        configureContexts();
    }

    private void configureContexts() {
        server.createContext("/tasks", new TaskHandler(manager, GSON));
        server.createContext("/subtasks", new SubtaskHandler(manager, GSON));
        server.createContext("/epics", new EpicHandler(manager, GSON));
        server.createContext("/history", new HistoryHandler(manager, GSON));
        server.createContext("/prioritized", new PrioritizedHandler(manager, GSON));
    }

    public static Gson getGson() {
        return GSON;
    }

    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();
        try {
            HttpTaskServer server = new HttpTaskServer(manager);
            server.start();
            System.out.println("HTTP-сервер запущен на порту 8080");
        } catch (IOException e) {
            System.err.println("Ошибка при запуске сервера: " + e.getMessage());
        }
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(1);
    }
}