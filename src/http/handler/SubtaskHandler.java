package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class SubtaskHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public SubtaskHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String path = h.getRequestURI().getPath();
            String method = h.getRequestMethod();

            if (method.equals("GET")) {
                if (path.equals("/subtasks")) {
                    List<Subtask> subtasks = manager.getAllSubtasks();
                    String json = gson.toJson(subtasks);
                    sendText(h, json);
                } else {
                    int id = getIdFromPath(path);
                    Subtask subtask = manager.getSubtaskById(id);
                    if (subtask == null) {
                        sendNotFound(h);
                    } else {
                        String json = gson.toJson(subtask);
                        sendText(h, json);
                    }
                }
            } else if (method.equals("POST")) {
                String body = new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Subtask subtask = gson.fromJson(body, Subtask.class);
                if (subtask.getId() == 0 || manager.getSubtaskById(subtask.getId()) == null) {
                    Subtask newSubtask = manager.createSubtask(
                            subtask.getTitle(),
                            subtask.getDescription(),
                            subtask.getStatus(),
                            subtask.getEpicId()
                    );
                    String json = gson.toJson(newSubtask);
                    sendText(h, json);
                } else {
                    manager.updateTask(subtask);
                    h.sendResponseHeaders(201, -1);
                    h.close();
                }
            } else if (method.equals("DELETE")) {
                if (path.equals("/subtasks")) {
                    manager.removeAllSubtasks();
                    h.sendResponseHeaders(201, -1);
                    h.close();
                } else {
                    int id = getIdFromPath(path);
                    if (manager.getSubtaskById(id) == null) {
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
