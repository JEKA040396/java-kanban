import com.google.gson.Gson;
import http.HttpTaskServer;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerPrioritizedTest {
    private TaskManager manager;
    private HttpTaskServer server;
    private Gson gson;

    @BeforeEach
    public void setUp() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        gson = HttpTaskServer.getGson();
        server.start();
    }

    @AfterEach
    public void tearDown() {
        server.stop();
    }

    @Test
    public void testGetPrioritized() throws IOException, InterruptedException {
        // Создаем задачи с временем начала, чтобы они попали в prioritized
        Task task = manager.createTask("Task", "Desc", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now().plusHours(1));

        Epic epic = manager.createEpic("Epic", "Desc", Status.NEW);

        Subtask subtask = manager.createSubtask("Subtask", "Desc", Status.DONE, epic.getId(),
                Duration.ofHours(2), LocalDateTime.now().plusHours(3));

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] tasks = gson.fromJson(response.body(), Task[].class);

        // Ожидаем 2 задачи (task и subtask), так как epic без времени начала не попадает в prioritized
        assertEquals(2, tasks.length);
    }

    @Test
    public void testGetPrioritized_Empty() throws IOException, InterruptedException {
        // Создаем задачи без времени начала - они не должны попасть в prioritized
        manager.createTask("Task", "Desc", Status.NEW);
        Epic epic = manager.createEpic("Epic", "Desc", Status.NEW);
        manager.createSubtask("Subtask", "Desc", Status.DONE, epic.getId());

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/prioritized"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(0, tasks.length); // Все задачи без времени начала
    }
}