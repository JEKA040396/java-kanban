import manager.FileBackedTaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {

    private FileBackedTaskManager manager;
    private File tempFile;

    @BeforeEach
    public void setup() throws IOException {
        tempFile = File.createTempFile("tasks", ".csv");
        manager = new FileBackedTaskManager(tempFile);
    }

    @AfterEach
    public void tearDown() {
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }

    @Test
    public void testSaveAndLoadEmpty() {
        // Сохраняем пустой менеджер
        manager.save();

        // Загружаем из файла
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(tempFile);

        assertTrue(loadedManager.getAllTasks().isEmpty(), "Список задач должен быть пустым");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Список эпиков должен быть пустым");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Список подзадач должен быть пустым");
    }

    @Test
    public void testSaveAndLoadWithTasks() {
        // Используем старые методы без временных параметров для обратной совместимости
        Task task = manager.createTask("Task1", "Description1", Status.NEW);
        Epic epic = manager.createEpic("Epic1", "Epic desc", Status.NEW);
        Subtask subtask = manager.createSubtask("Subtask1", "Subtask desc", Status.NEW, epic.getId());

        manager.save();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loaded.getAllTasks().size(), "Должна быть 1 задача");
        assertEquals(1, loaded.getAllEpics().size(), "Должен быть 1 эпик");
        assertEquals(1, loaded.getAllSubtasks().size(), "Должна быть 1 подзадача");

        // Проверяем, что данные совпадают
        assertEquals(task.getTitle(), loaded.getAllTasks().get(0).getTitle());
        assertEquals(epic.getTitle(), loaded.getAllEpics().get(0).getTitle());
        assertEquals(subtask.getTitle(), loaded.getAllSubtasks().get(0).getTitle());
    }

    @Test
    public void testSaveAndLoadWithTimeFields() {
        // Используем новые методы с временными параметрами, но без пересечений
        LocalDateTime startTime1 = LocalDateTime.of(2025, 1, 1, 10, 0);
        LocalDateTime startTime2 = LocalDateTime.of(2025, 1, 1, 13, 0); // Разные времена чтобы избежать пересечений
        Duration duration = Duration.ofHours(2);

        Task task = manager.createTask("Task1", "Description1", Status.NEW, duration, startTime1);
        Epic epic = manager.createEpic("Epic1", "Epic desc", Status.NEW);
        Subtask subtask = manager.createSubtask("Subtask1", "Subtask desc", Status.NEW,
                epic.getId(), Duration.ofHours(1), startTime2);

        manager.save();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        assertEquals(1, loaded.getAllTasks().size());
        Task loadedTask = loaded.getAllTasks().get(0);
        assertEquals(startTime1, loadedTask.getStartTime());
        assertEquals(duration, loadedTask.getDuration());

        assertEquals(1, loaded.getAllSubtasks().size());
        Subtask loadedSubtask = loaded.getAllSubtasks().get(0);
        assertEquals(startTime2, loadedSubtask.getStartTime());
    }

    @Test
    public void testLoadUpdatesNextId() {
        Task task1 = manager.createTask("Task1", "Desc1", Status.NEW);
        manager.save();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        Task newTask = loaded.createTask("Task2", "Desc2", Status.NEW);
        assertTrue(newTask.getId() > task1.getId(), "ID новой задачи должен быть больше предыдущего");
    }
}