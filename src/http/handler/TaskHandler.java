package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TaskHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public TaskHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String path = h.getRequestURI().getPath();
            String method = h.getRequestMethod();

            if (method.equals("GET")) {
                if (path.equals("/tasks")) {
                    List<Task> tasks = manager.getAllTasks();
                    String json = gson.toJson(tasks);
                    sendText(h, json);
                } else {
                    int id = getIdFromPath(path);
                    Task task = manager.getTaskById(id);
                    if (task == null) {
                        sendNotFound(h);
                    } else {
                        String json = gson.toJson(task);
                        sendText(h, json);
                    }
                }
            } else if (method.equals("POST")) {
                String body = new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Task task = gson.fromJson(body, Task.class);

                try {
                    if (task.getId() == 0 || manager.getTaskById(task.getId()) == null) {
                        Task newTask = manager.createTask(task.getTitle(), task.getDescription(), task.getStatus());
                        String json = gson.toJson(newTask);
                        sendText(h, json);
                    } else {
                        manager.updateTask(task);
                        h.sendResponseHeaders(201, -1);
                        h.close();
                    }
                } catch (IllegalArgumentException e) {
                    if (e.getMessage() != null && e.getMessage().contains("пересекается")) {
                        sendHasOverlaps(h);
                    } else {
                        throw e; // пробрасываем другие IllegalArgumentException дальше
                    }
                }
            } else if (method.equals("DELETE")) {
                if (path.equals("/tasks")) {
                    manager.removeAllTasks();
                    h.sendResponseHeaders(201, -1);
                    h.close();
                } else {
                    int id = getIdFromPath(path);
                    if (manager.getTaskById(id) == null) {
                        sendNotFound(h);
                    } else {
                        manager.removeById(id);
                        h.sendResponseHeaders(201, -1);
                        h.close();
                    }
                }
            } else {
                h.sendResponseHeaders(405, -1);
                h.close();
            }
        } catch (IllegalArgumentException e) {
            // Обрабатываем другие IllegalArgumentException
            sendInternalServerError(h);
        } catch (Exception e) {
            e.printStackTrace();
            sendInternalServerError(h);
        }
    }

    private int getIdFromPath(String path) {
        String[] parts = path.split("/");
        return Integer.parseInt(parts[parts.length - 1]);
    }
}