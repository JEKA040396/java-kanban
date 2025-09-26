import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private final int MAX_HISTORY_SIZE = 10;
    private final List<Task> history = new ArrayList<>();
    private final HashMap<Integer, Task> taskMap = new HashMap<>();

    @Override
    public void add(Task task) {

        if (taskMap.containsKey(task.getId())) {
            remove(task.getId());
        }
        if (history.size() >= MAX_HISTORY_SIZE) {
            Task firstTask = history.get(0);
            history.remove(0);
            taskMap.remove(firstTask.getId());
        }
        history.add(task);
        taskMap.put(task.getId(), task);
    }

    public void remove(int id) {
        Task task = taskMap.remove(id);
        if (task != null) {
            history.remove(task);
        }
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(history);
    }
}