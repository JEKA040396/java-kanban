import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TaskManager {

    private int nextId = 1;

    private HashMap<Integer, Task> tasks = new HashMap<>();
    private HashMap<Integer, Epic> epics = new HashMap<>();
    private HashMap<Integer, Subtask> subtasks = new HashMap<>();

    // Создание задачи - присваивается уникальный ID
    public Task createTask(Task task) {
        task = assignId(task);
        tasks.put(task.getId(), task);
        return task;
    }

    public Epic createEpic(Epic epic) {
        epic = (Epic) assignId(epic);
        epics.put(epic.getId(), epic);
        return epic;
    }

    public Subtask createSubtask(Subtask subtask) {
        subtask = (Subtask) assignId(subtask);
        subtasks.put(subtask.getId(), subtask);
        // Добавляем подзадачу в эпик
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subtask);
            epic.updateStatus();
        }
        return subtask;
    }

    private Task assignId(Task task) {
        task = copyTaskWithId(task, nextId);
        nextId++;
        return task;
    }

    // Копируем объект с новым ID, для простоты сделаем в базовом классе Task метод clone с новым ID,
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

    // Получить задачи всех типов
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Получить задачу по ID
    public Task getTaskById(int id) {
        if (tasks.containsKey(id)) {
            return tasks.get(id);
        } else if (epics.containsKey(id)) {
            return epics.get(id);
        } else {
            return subtasks.get(id);
        }
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

    // Удалить задачу по ID
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

    // Обновление задачи по ID (полностью заменить)
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

    // Получение подзадач эпика
    public List<Subtask> getSubtasksOfEpic(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic != null) {
            return new ArrayList<>(epic.getSubtasks());
        }
        return new ArrayList<>();
    }
}
