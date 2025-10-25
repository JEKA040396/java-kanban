package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {

    protected final HashMap<Integer, Task> tasks = new HashMap<>();
    protected final HashMap<Integer, Epic> epics = new HashMap<>();
    protected final HashMap<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    protected int nextId = 1;


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

    // Удалить все задачи
    public void removeAllTasks() {
        tasks.clear();
    }

    public void removeAllEpics() {
        epics.clear();
        subtasks.clear(); // При удалении эпиков удалим и подзадачи
    }

    public void removeAllSubtasks() {
        subtasks.clear();
        // И обновим статусы эпиков
        for (Epic epic : epics.values()) {
            epic.updateStatus();
        }
    }


    @Override
    public Task createTask(String title, String description, Status status) {

        Task task = new Task(nextId++, title, description, status);
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
    public Subtask createSubtask(String title, String description, Status status, int epicId) {

        Subtask subtask = new Subtask(nextId++, title, description, status, epicId);
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(epicId);
        if (epic != null) {
            epic.addSubtask(subtask);
            epic.updateStatus();
        }
        return subtask;
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
            tasks.remove(id);
        } else if (epics.containsKey(id)) {
            // При удалении эпика удаляем и подзадачи
            Epic epic = epics.get(id);
            for (Subtask st : epic.getSubtasks()) {
                subtasks.remove(st.getId());
            }
            epics.remove(id);
        } else if (subtasks.containsKey(id)) {
            Subtask st = subtasks.get(id);
            subtasks.remove(id);
            Epic epic = epics.get(st.getEpicId());
            if (epic != null) {
                epic.getSubtasks().remove(st);
                epic.updateStatus();
            }
        }
    }

    @Override
    public void updateTask(Task task) {

        int id = task.getId();
        if (tasks.containsKey(id)) {
            tasks.put(id, task);
        } else if (epics.containsKey(id)) {
            Epic epic = (Epic) task;
            epics.put(id, epic);
            epic.updateStatus();
        } else if (subtasks.containsKey(id)) {
            Subtask subtask = (Subtask) task;
            subtasks.put(id, subtask);
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.updateStatus();
            }
        }
    }

    @Override
    public List<Subtask> getSubtasksOfEpic(int epicId) {

        Epic epic = epics.get(epicId);
        if (epic != null) {
            return new ArrayList<>(epic.getSubtasks());
        }
        return new ArrayList<>();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }


    private Task assignId(Task task) {
        task = copyTaskWithId(task, nextId);
        nextId++;
        return task;
    }

    // Копируем объект с новым ID, для простоты сделаем в базовом классе model.Task метод clone с новым ID,
    // либо мы можем сделать конструктор, но для примера простой вариант:
    private Task copyTaskWithId(Task task, int id) {
        if (task instanceof Subtask st) {
            return new Subtask(id, st.getTitle(), st.getDescription(), st.getStatus(), st.getEpicId());
        } else if (task instanceof Epic ep) {
            return new Epic(id, ep.getTitle(), ep.getDescription(), ep.getStatus());
        } else {
            return new Task(id, task.getTitle(), task.getDescription(), task.getStatus());
        }
    }
}



