import manager.InMemoryHistoryManager;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InMemoryHistoryManagerTest {

    private Task task1;
    private Task task2;
    private Task task3;
    private InMemoryHistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();

        task1 = new Task(1, "Task 1", "Description 1", null);
        task2 = new Task(2, "Task 2", "Description 2", null);
        task3 = new Task(3, "Task 3", "Description 3", null);

        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
    }

    @Test
    void testAddAndGetHistory() {
        assertEquals(List.of(task1, task2, task3), historyManager.getHistory());
    }

    @Test
    void testAddDuplicateTaskMovesToEnd() {
        historyManager.add(task1); // добавляем task1 повторно

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task3, history.get(1));
        assertEquals(task1, history.get(2));
    }

    @Test
    void testRemoveById() {
        historyManager.remove(task1.getId());

        List<Task> history = historyManager.getHistory();
        assertEquals(2, history.size());
        assertEquals(List.of(task2, task3), history);
    }

    @Test
    void testRemoveNonExistentId() {
        historyManager.remove(999); // несуществующий id

        List<Task> history = historyManager.getHistory();
        assertEquals(3, history.size());
        assertEquals(List.of(task1, task2, task3), history);
    }

    @Test
    void testEmptyHistory() {
        historyManager = new InMemoryHistoryManager(); // очистим историю
        List<Task> history = historyManager.getHistory();
        assertTrue(history.isEmpty());
    }

    @Test
    void testRemoveFirst() {
        historyManager.remove(task1.getId());
        assertEquals(List.of(task2, task3), historyManager.getHistory());
    }

    @Test
    void testRemoveMiddle() {
        historyManager.remove(task2.getId());
        assertEquals(List.of(task1, task3), historyManager.getHistory());
    }

    @Test
    void testRemoveLast() {
        historyManager.remove(task3.getId());
        assertEquals(List.of(task1, task2), historyManager.getHistory());
    }
}
