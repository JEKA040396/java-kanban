import manager.HistoryManager;
import manager.InMemoryHistoryManager;
import model.Status;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class HistoryManagerTest {

    private HistoryManager historyManager;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void add() {
        Task task = new Task(1, "Title", "Description", Status.NEW);
        historyManager.add(task);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task, history.get(0));
    }


    @Test
    void shouldLimitHistorySize() {
        for (int i = 1; i <= 15; i++) {
            historyManager.add(new Task(i, "Task " + i, "Description " + i, null));
        }
        assertEquals(15, historyManager.getHistory().size());
    }


    @Test
    void remove() {
        Task task1 = new Task(1, "Title1", "Desc1", Status.NEW);
        Task task2 = new Task(2, "Title2", "Desc2", Status.NEW);
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(1);
        List<Task> history = historyManager.getHistory();
        assertEquals(1, history.size());
        assertEquals(task2, history.get(0));
    }
}