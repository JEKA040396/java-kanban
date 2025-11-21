import com.google.gson.Gson;
import http.HttpTaskServer;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Status;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {
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
    public void testGetAllTasks() throws IOException, InterruptedException {
        // Подготавливаем данные
        manager.createTask("Task 1", "Desc 1", Status.NEW);
        manager.createTask("Task 2", "Desc 2", Status.DONE);

        // Отправляем запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Проверяем статус
        assertEquals(200, response.statusCode());

        // Десериализуем ответ
        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertEquals(2, tasks.length);
        assertEquals("Task 1", tasks[0].getTitle());
        assertEquals(Status.NEW, tasks[0].getStatus());
    }

    @Test
    public void testGetTaskByIdNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }

    @Test
    public void testCreateTask() throws IOException, InterruptedException {
        Task task = new Task(0, "Новая задача", "Описание новой задачи", Status.IN_PROGRESS);
        String json = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());

        // Десериализуем созданную задачу
        Task createdTask = gson.fromJson(response.body(), Task.class);
        assertEquals("Новая задача", createdTask.getTitle());
        assertEquals(Status.IN_PROGRESS, createdTask.getStatus());
        assertTrue(createdTask.getId() > 0); // ID присвоен

        // Проверяем, что задача действительно в менеджере
        List<Task> allTasks = manager.getAllTasks();
        assertEquals(1, allTasks.size());
        assertEquals(createdTask.getId(), allTasks.get(0).getId());
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        // Сначала создаём задачу
        Task original = manager.createTask("Старое имя", "Старое описание", Status.NEW);
        int id = original.getId();

        // Отправляем обновлённую задачу
        Task updated = new Task(id, "Новое имя", "Новое описание", Status.DONE);
        String json = gson.toJson(updated);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());

        // Проверяем, что задача обновилась в менеджере
        Task fromManager = manager.getTaskById(id);
        assertEquals("Новое имя", fromManager.getTitle());
        assertEquals("Новое описание", fromManager.getDescription());
        assertEquals(Status.DONE, fromManager.getStatus());
    }

    @Test
    public void testDeleteAllTasks() throws IOException, InterruptedException {
        manager.createTask("Задача 1", "Описание", Status.NEW);
        manager.createTask("Задача 2", "Описание", Status.NEW);

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertTrue(manager.getAllTasks().isEmpty());
    }

    @Test
    public void testDeleteTaskById() throws IOException, InterruptedException {
        Task task = manager.createTask("Удаляемая задача", "Описание", Status.NEW);
        int id = task.getId();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/" + id))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertNull(manager.getTaskById(id));
    }

    @Test
    public void testDeleteTaskById_NotFound_Returns404() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/tasks/999"))
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode());
    }
}
