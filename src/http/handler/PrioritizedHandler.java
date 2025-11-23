package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
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
                // Используем метод менеджера для получения отсортированных задач
                List<Task> prioritized = manager.getPrioritizedTasks();
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