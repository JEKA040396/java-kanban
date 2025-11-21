package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    // Улучшенный компаратор с учетом ID для уникальности
    private final Comparator<Task> byStartTime = (t1, t2) -> {
        if (t1.getStartTime() == null && t2.getStartTime() == null) return Integer.compare(t1.getId(), t2.getId());
        if (t1.getStartTime() == null) return 1;
        if (t2.getStartTime() == null) return -1;

        int timeCompare = t1.getStartTime().compareTo(t2.getStartTime());
        return timeCompare != 0 ? timeCompare : Integer.compare(t1.getId(), t2.getId());
    };

    protected final TreeSet<Task> prioritizedTasks = new TreeSet<>(byStartTime);
    protected int nextId = 1;

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    // Старые методы для обратной совместимости
    @Override
    public Task createTask(String title, String description, Status status) {
        return createTask(title, description, status, null, null);
    }

    @Override
    public Subtask createSubtask(String title, String description, Status status, int epicId) {
        return createSubtask(title, description, status, epicId, null, null);
    }

    // Новые методы с временными параметрами
    @Override
    public Task createTask(String title, String description, Status status, Duration duration, LocalDateTime startTime) {
        Task task = new Task(nextId++, title, description, status);
        task.setDuration(duration);
        task.setStartTime(startTime);

        if (startTime != null) {
            if (isIntersectingWithAny(task)) {
                throw new IllegalArgumentException("Задача пересекается с другой задачей по времени");
            }
            prioritizedTasks.add(task);
        }
        tasks.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic createEpic(String title, String description, Status status) {
        Epic epic = new Epic(nextId++, title, description, status);
        epics.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask createSubtask(String title, String description, Status status, int epicId,
                                 Duration duration, LocalDateTime startTime) {
        Subtask subtask = new Subtask(nextId++, title, description, status, epicId);
        subtask.setDuration(duration);
        subtask.setStartTime(startTime);

        if (startTime != null) {
            if (isIntersectingWithAny(subtask)) {
                throw new IllegalArgumentException("Подзадача пересекается по времени с другой задачей");
            }
            prioritizedTasks.add(subtask);
        }
        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(epicId);
        if (epic != null) {
            epic.addSubtask(subtask);
            epic.updateStatus();
            epic.updateTimeFields();
        }
        return subtask;
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public void removeAllTasks() {
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
            prioritizedTasks.remove(task);
        }
        tasks.clear();
    }

    @Override
    public void removeAllSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
            prioritizedTasks.remove(subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.getSubtaskIds().remove(Integer.valueOf(subtask.getId()));
                epic.updateStatus();
                epic.updateTimeFields();
            }
        }
        subtasks.clear();
    }

    @Override
    public void removeAllEpics() {
        for (Epic epic : epics.values()) {
            List<Integer> subtasksToRemove = new ArrayList<>(epic.getSubtaskIds());
            for (Integer subtaskId : subtasksToRemove) {
                historyManager.remove(subtaskId);
                subtasks.remove(subtaskId);
                prioritizedTasks.removeIf(task -> task.getId() == subtaskId);
            }
            historyManager.remove(epic.getId());
        }
        epics.clear();
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void removeById(int id) {
        if (tasks.containsKey(id)) {
            Task removed = tasks.remove(id);
            prioritizedTasks.remove(removed);
            historyManager.remove(id);
        } else if (epics.containsKey(id)) {
            Epic epic = epics.remove(id);
            for (Subtask st : epic.getSubtasks()) {
                subtasks.remove(st.getId());
                prioritizedTasks.remove(st);
                historyManager.remove(st.getId());
            }
            historyManager.remove(id);
        } else if (subtasks.containsKey(id)) {
            Subtask st = subtasks.remove(id);
            prioritizedTasks.remove(st);
            Epic epic = epics.get(st.getEpicId());
            if (epic != null) {
                epic.getSubtasks().remove(st);
                epic.updateStatus();
                epic.updateTimeFields();
            }
            historyManager.remove(id);
        }
    }

    @Override
    public void updateTask(Task task) {
        int id = task.getId();

        // Проверяем пересечение только если у задачи есть startTime
        if (task.getStartTime() != null && isIntersectingWithAny(task)) {
            throw new IllegalArgumentException("Задача пересекается с другой задачей по времени");
        }

        if (tasks.containsKey(id)) {
            Task old = tasks.get(id);
            prioritizedTasks.remove(old);
            tasks.put(id, task);
            if (task.getStartTime() != null) {
                prioritizedTasks.add(task);
            }
        } else if (epics.containsKey(id)) {
            Epic epic = (Epic) task;
            epics.put(id, epic);
            epic.updateStatus();
            epic.updateTimeFields();
        } else if (subtasks.containsKey(id)) {
            Subtask oldSubtask = subtasks.get(id);
            prioritizedTasks.remove(oldSubtask);
            subtasks.put(id, (Subtask) task);
            if (task.getStartTime() != null) {
                prioritizedTasks.add(task);
            }
            Epic epic = epics.get(((Subtask) task).getEpicId());
            if (epic != null) {
                epic.updateStatus();
                epic.updateTimeFields();
            }
        }
    }

    public boolean isIntersecting(Task task1, Task task2) {
        if (task1.getStartTime() == null || task1.getEndTime() == null ||
                task2.getStartTime() == null || task2.getEndTime() == null) {
            return false;
        }
        return !(task1.getStartTime().isAfter(task2.getEndTime()) ||
                task1.getEndTime().isBefore(task2.getStartTime()));
    }

    public boolean isIntersectingWithAny(Task task) {
        if (task.getStartTime() == null || task.getEndTime() == null) {
            return false;
        }

        return getPrioritizedTasks().stream()
                .filter(t -> t.getId() != task.getId())
                .filter(t -> t.getStartTime() != null && t.getEndTime() != null)
                .anyMatch(t -> isIntersecting(task, t));
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            return new ArrayList<>(epic.getSubtasks());
        }
        return List.of();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }
}