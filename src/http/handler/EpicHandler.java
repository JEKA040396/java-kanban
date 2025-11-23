package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Epic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EpicHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public EpicHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            String path = h.getRequestURI().getPath();
            String method = h.getRequestMethod();

            if (method.equals("GET")) {
                if (path.equals("/epics")) {
                    List<Epic> epics = manager.getAllEpics();
                    String json = gson.toJson(epics);
                    sendText(h, json);
                } else {
                    // Возможно /epics/{id}/subtasks — но задание не требует
                    int id = getIdFromPath(path);
                    Epic epic = manager.getEpicById(id);
                    if (epic == null) {
                        sendNotFound(h);
                    } else {
                        String json = gson.toJson(epic);
                        sendText(h, json);
                    }
                }
            } else if (method.equals("POST")) {
                String body = new String(h.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                Epic epic = gson.fromJson(body, Epic.class);
                if (epic.getId() == 0 || manager.getEpicById(epic.getId()) == null) {
                    Epic newEpic = manager.createEpic(epic.getTitle(), epic.getDescription(), epic.getStatus());
                    String json = gson.toJson(newEpic);
                    sendText(h, json);
                } else {
                    manager.updateTask(epic);
                    h.sendResponseHeaders(201, -1);
                    h.close();
                }
            } else if (method.equals("DELETE")) {
                if (path.equals("/epics")) {
                    manager.removeAllEpics();
                    h.sendResponseHeaders(201, -1);
                    h.close();
                } else {
                    int id = getIdFromPath(path);
                    if (manager.getEpicById(id) == null) {
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