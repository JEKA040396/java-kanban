package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

public interface TaskManager {

    List<Task> getPrioritizedTasks();

    // Старые методы (оставляем для обратной совместимости)
    Task createTask(String title, String description, Status status);
    Subtask createSubtask(String title, String description, Status status, int epicId);

    // Новые методы с временными параметрами
    Task createTask(String title, String description, Status status, Duration duration, LocalDateTime startTime);
    Subtask createSubtask(String title, String description, Status status, int epicId,
                          Duration duration, LocalDateTime startTime);

    Epic createEpic(String title, String description, Status status);

    // Методы для получения по id
    Task getTaskById(int id);
    Epic getEpicById(int id);
    Subtask getSubtaskById(int id);

    // Получить задачи всех типов
    List<Task> getAllTasks();
    List<Epic> getAllEpics();
    List<Subtask> getAllSubtasks();

    // Удалить задачу по ID
    void removeById(int id);

    // Обновление задачи по ID (полностью заменить)
    void updateTask(Task task);

    void removeAllSubtasks();
    void removeAllEpics();
    void removeAllTasks();

    // Получение подзадач эпика
    List<Subtask> getSubtasksOfEpic(int epicId);

    List<Task> getHistory();
}