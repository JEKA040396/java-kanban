import com.google.gson.Gson;
import http.HttpTaskServer;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerSubtasksTest {
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
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        Epic epic = manager.createEpic("Epic", "Desc", Status.NEW);
        manager.createSubtask("Subtask 1", "Desc 1", Status.NEW, epic.getId());
        manager.createSubtask("Subtask 2", "Desc 2", Status.DONE, epic.getId());

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertEquals(2, subtasks.length);
        assertEquals("Subtask 1", subtasks[0].getTitle());
        assertEquals("Subtask 2", subtasks[1].getTitle());
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/999"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testCreateSubtask() throws IOException, InterruptedException {
        Epic epic = manager.createEpic("Epic", "Desc", Status.NEW);
        Subtask subtask = new Subtask(0, "New subtask", "Desc", Status.IN_PROGRESS, epic.getId());
        String json = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask created = gson.fromJson(response.body(), Subtask.class);
        assertTrue(created.getId() > 0);
        assertEquals(epic.getId(), created.getEpicId());
        assertEquals(1, manager.getAllSubtasks().size());
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = manager.createEpic("Epic", "Desc", Status.NEW);
        Subtask original = manager.createSubtask("Old", "Old desc", Status.NEW, epic.getId());
        Subtask updated = new Subtask(original.getId(), "Updated", "New desc", Status.DONE, epic.getId());
        String json = gson.toJson(updated);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        Subtask fromManager = manager.getSubtaskById(original.getId());
        assertEquals("Updated", fromManager.getTitle());
        assertEquals(Status.DONE, fromManager.getStatus());
    }

    @Test
    public void testDeleteAllSubtasks() throws IOException, InterruptedException {
        Epic epic = manager.createEpic("Epic", "Desc", Status.NEW);
        manager.createSubtask("Subtask", "Desc", Status.NEW, epic.getId());

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertTrue(manager.getAllSubtasks().isEmpty());
    }

    @Test
    public void testDeleteSubtaskById() throws IOException, InterruptedException {
        Epic epic = manager.createEpic("Epic", "Desc", Status.NEW);
        Subtask subtask = manager.createSubtask("To delete", "Desc", Status.NEW, epic.getId());

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/subtasks/" + subtask.getId()))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertNull(manager.getSubtaskById(subtask.getId()));
    }
}