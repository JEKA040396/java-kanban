package http.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import manager.TaskManager;
import model.Task;

import java.io.IOException;
import java.util.List;

public class HistoryHandler extends BaseHttpHandler {
    private final TaskManager manager;
    private final Gson gson;

    public HistoryHandler(TaskManager manager, Gson gson) {
        this.manager = manager;
        this.gson = gson;
    }

    @Override
    public void handle(HttpExchange h) throws IOException {
        try {
            if (h.getRequestMethod().equals("GET") && h.getRequestURI().getPath().equals("/history")) {
                List<Task> history = manager.getHistory();
                String json = gson.toJson(history);
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
