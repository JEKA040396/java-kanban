package http;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpServer;
import http.handler.*;
import manager.Managers;
import manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final Gson GSON = new Gson();
    private final HttpServer server;
    private final TaskManager manager;

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        this.server = HttpServer.create(new InetSocketAddress(8080), 0);
    }

    public static Gson getGson() {
        return GSON;
    }

    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();
        HttpTaskServer server = null;
        try {
            server = new HttpTaskServer(manager);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("HTTP-сервер запущен на порту 8080");
    }

    public void start() throws IOException {
        server.createContext("/tasks", new TaskHandler(manager, GSON));
        server.createContext("/subtasks", new SubtaskHandler(manager, GSON));
        server.createContext("/epics", new EpicHandler(manager, GSON));
        server.createContext("/history", new HistoryHandler(manager, GSON));
        server.createContext("/prioritized", new PrioritizedHandler(manager, GSON));
        server.start();
    }

    public void stop() {
        server.stop(1);
    }
}