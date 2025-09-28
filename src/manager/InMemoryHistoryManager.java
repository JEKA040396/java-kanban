package manager;

import model.Task;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class InMemoryHistoryManager implements HistoryManager {
    private final int MAX_HISTORY_SIZE = 10;
    private final List<Task> history = new LinkedList<>();

    @Override
    public void add(Task task) {
        history.remove(task);
        if (history.size() >= MAX_HISTORY_SIZE) {
            history.remove(0);
        }
        history.add(task);
    }

    public void remove(int id) {
        history.removeIf(task -> task.getId() == id);
    }

    @Override
    public List<Task> getHistory() {

        return new ArrayList<>(history);
    }
}