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
                    // GET /tasks — получить все задачи
                    List<Task> tasks = manager.getAllTasks();
                    String json = gson.toJson(tasks);
                    sendText(h, json);
                } else {
                    // GET /tasks/{id} — получить задачу по ID
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
                // POST /tasks — создать или обновить задачу
                String body = new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Task task = gson.fromJson(body, Task.class);
                if (task.getId() == 0 || manager.getTaskById(task.getId()) == null) {
                    // Новая задача
                    Task newTask = manager.createTask(task.getTitle(), task.getDescription(), task.getStatus());
                    String json = gson.toJson(newTask);
                    sendText(h, json); // 200 OK — возвращаем созданную задачу
                } else {
                    // Обновление существующей
                    manager.updateTask(task);
                    h.sendResponseHeaders(201, -1); // 201 Created — без тела
                    h.close();
                }
            } else if (method.equals("DELETE")) {
                if (path.equals("/tasks")) {
                    // DELETE /tasks — удалить все задачи
                    manager.removeAllTasks();
                    h.sendResponseHeaders(201, -1);
                    h.close();
                } else {
                    // DELETE /tasks/{id} — удалить задачу по ID
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
                h.sendResponseHeaders(405, -1); // Method Not Allowed
                h.close();
            }
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