package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PrioritizedHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public PrioritizedHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            if (h.getRequestMethod().equals("GET") && h.getRequestURI().getPath().equals("/prioritized")) {
                // Пока возвращаем все задачи как есть (можно расширить логикой сортировки)
                List<Task> prioritized = new ArrayList<>();
                prioritized.addAll(manager.getAllTasks());
                prioritized.addAll(manager.getAllSubtasks());
                prioritized.addAll(manager.getAllEpics());
                String json = gson.toJson(prioritized);
                sendText(h, json);
            } else {
                h.sendResponseHeaders(405, -1);
                h.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
            sendInternalServerError(h);
        }
    }
}
