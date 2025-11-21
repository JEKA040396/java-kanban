import manager.InMemoryTaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

    private InMemoryTaskManager manager;

    @BeforeEach
    void setUp() {
        manager = new InMemoryTaskManager();
    }

    @Test
    public void testCreateTask() {
        Task task = manager.createTask("Test Task", "Description", Status.NEW);
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofHours(1));
        manager.updateTask(task);

        assertNotNull(task);
        assertEquals("Test Task", task.getTitle());
        assertEquals(Status.NEW, task.getStatus());
        assertTrue(manager.getAllTasks().contains(task));
    }

    @Test
    public void testCreateEpicAndSubtask() {
        Epic epic = manager.createEpic("Epic 1", "Epic Description", Status.NEW);
        assertNotNull(epic);

        Subtask subtask = manager.createSubtask("Subtask 1", "Subtask Description", Status.NEW, epic.getId());
        subtask.setStartTime(LocalDateTime.now().plusDays(1));
        subtask.setDuration(Duration.ofHours(2));
        manager.updateTask(subtask);

        assertNotNull(subtask);
        List<Subtask> subtasks = manager.getSubtasksOfEpic(epic.getId());
        assertTrue(subtasks.contains(subtask));
        assertEquals(epic.getId(), subtask.getEpicId());
    }

    @Test
    public void testGetTaskByIdAddsToHistory() {
        Task task = manager.createTask("Task for History", "Desc", Status.NEW);
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofHours(1));
        manager.updateTask(task);

        Task retrieved = manager.getTaskById(task.getId());
        assertNotNull(retrieved);
        assertEquals(task, retrieved);

        List<Task> history = manager.getHistory();
        assertTrue(history.contains(task));
    }

    @Test
    public void testRemoveByIdRemovesTask() {
        Task task = manager.createTask("Task to Remove", "Desc", Status.NEW);
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofHours(1));
        manager.updateTask(task);

        int id = task.getId();
        manager.removeById(id);
        assertNull(manager.getTaskById(id));
    }

    @Test
    public void testUpdateTask() {
        Task task = manager.createTask("Old Title", "Old Desc", Status.NEW);
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofHours(1));
        manager.updateTask(task);

        task.setTitle("New Title");
        task.setDescription("New Desc");
        manager.updateTask(task);

        Task updated = manager.getTaskById(task.getId());
        assertEquals("New Title", updated.getTitle());
        assertEquals("New Desc", updated.getDescription());
    }

    @Test
    public void createTasksWithIntersectionShouldThrow() {
        Task task1 = manager.createTask("Task1", "Desc1", Status.NEW);
        task1.setStartTime(LocalDateTime.of(2025, 11, 8, 10, 0));
        task1.setDuration(Duration.ofHours(2));
        manager.updateTask(task1);

        Task task2 = manager.createTask("Task2", "Desc2", Status.NEW);
        task2.setStartTime(LocalDateTime.of(2025, 11, 8, 11, 0));
        task2.setDuration(Duration.ofHours(1));

        assertThrows(IllegalArgumentException.class, () -> {
            manager.updateTask(task2);
        });
    }

    @Test
    public void getPrioritizedTasksShouldReturnSorted() {
        Task task1 = manager.createTask("Task1", "Desc1", Status.NEW);
        task1.setStartTime(LocalDateTime.of(2025, 11, 8, 12, 0));
        task1.setDuration(Duration.ofHours(1));
        manager.updateTask(task1);

        Task task2 = manager.createTask("Task2", "Desc2", Status.NEW);
        task2.setStartTime(LocalDateTime.of(2025, 11, 8, 10, 0));
        task2.setDuration(Duration.ofHours(1));
        manager.updateTask(task2);

        List<Task> prioritized = manager.getPrioritizedTasks();
        assertEquals(task2.getId(), prioritized.get(0).getId());
        assertEquals(task1.getId(), prioritized.get(1).getId());
    }

    @Test
    public void removeTaskByIdShouldDelete() {
        Task task = manager.createTask("Task", "Desc", Status.NEW);
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofHours(1));
        manager.updateTask(task);

        int id = task.getId();
        manager.removeById(id);
        assertNull(manager.getTaskById(id));
    }
}
