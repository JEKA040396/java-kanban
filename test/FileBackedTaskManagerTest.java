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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
        tempFile.delete();  // Удаляем временный файл после теста
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
    public void testLoadUpdatesNextId() {
        Task task1 = manager.createTask("Task1", "Desc1", Status.NEW);
        manager.save();

        FileBackedTaskManager loaded = FileBackedTaskManager.loadFromFile(tempFile);

        Task newTask = loaded.createTask("Task2", "Desc2", Status.NEW);
        assertTrue(newTask.getId() > task1.getId(), "ID новой задачи должен быть больше предыдущего");
    }
}
