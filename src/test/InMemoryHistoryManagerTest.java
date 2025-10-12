package test;

import manager.HistoryManager;
import manager.InMemoryHistoryManager;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryHistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void testAddAndGetHistory() {
        Task task1 = new Task(1, "Task 1", "Description 1", null);
        Task task2 = new Task(2, "Task 2", "Description 2", null);

        historyManager.add(task1);
        historyManager.add(task2);

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
    }

    @Test
    void testAddDuplicateTaskMovesToEnd() {
        Task task1 = new Task(1, "Task 1", "Description 1", null);
        Task task2 = new Task(2, "Task 2", "Description 2", null);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1); // добавляем task1 повторно

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task1, history.get(1));
    }

    @Test
    void testRemoveById() {
        Task task1 = new Task(1, "Task 1", "Description 1", null);
        Task task2 = new Task(2, "Task 2", "Description 2", null);

        historyManager.add(task1);
        historyManager.add(task2);

        historyManager.remove(1);

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task2, history.get(0));
    }

    @Test
    void testRemoveNonExistentId() {
        Task task1 = new Task(1, "Task 1", "Description 1", null);

        historyManager.add(task1);

        historyManager.remove(2); // несуществующий id

        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }

    @Test
    void testEmptyHistory() {
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }
}

