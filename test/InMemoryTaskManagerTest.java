import manager.InMemoryTaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

    private InMemoryTaskManager taskManager;

    @BeforeEach
    void setup() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    public void testCreateTask() {
        Task task = taskManager.createTask("Test Task", "Description", Status.NEW);
        assertNotNull(task);
        Assertions.assertEquals("Test Task", task.getTitle());
        Assertions.assertEquals(Status.NEW, task.getStatus());
        Assertions.assertTrue(taskManager.getAllTasks().contains(task));
    }

    @Test
    public void testCreateEpicAndSubtask() {
        Epic epic = taskManager.createEpic("Epic 1", "Epic Description", Status.NEW);
        assertNotNull(epic);

        Subtask subtask = taskManager.createSubtask("Subtask 1", "Subtask Description", Status.NEW, epic.getId());
        assertNotNull(subtask);

        List<Subtask> subtasks = taskManager.getSubtasksOfEpic(epic.getId());
        assertTrue(subtasks.contains(subtask));
        Assertions.assertEquals(epic.getId(), subtask.getEpicId());
    }

    @Test
    public void testGetTaskByIdAddsToHistory() {
        Task task = taskManager.createTask("Task for History", "Desc", Status.NEW);
        Task retrieved = taskManager.getTaskById(task.getId());
        assertNotNull(retrieved);
        assertEquals(task, retrieved);

        List<Task> history = taskManager.getHistory();
        assertTrue(history.contains(task));
    }

    @Test
    public void testRemoveByIdRemovesTask() {
        Task task = taskManager.createTask("Task to Remove", "Desc", Status.NEW);
        int id = task.getId();
        taskManager.removeById(id);
        assertNull(taskManager.getTaskById(id));
    }

    @Test
    public void testUpdateTask() {
        Task task = taskManager.createTask("Old Title", "Old Desc", Status.NEW);
        task.setTitle("New Title");
        task.setDescription("New Desc");
        taskManager.updateTask(task);
        Task updated = taskManager.getTaskById(task.getId());
        Assertions.assertEquals("New Title", updated.getTitle());
        Assertions.assertEquals("New Desc", updated.getDescription());
    }
}