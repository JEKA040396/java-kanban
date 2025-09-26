import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface TaskManager {

    Task createTask(String title, String description, Status status);

    Epic createEpic(String title, String description, Status status);

    Subtask createSubtask(String title, String description, Status status, int epicId);


    // Получить задачи всех типов
    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    // Удалить задачу по ID
    void removeById(int id);

    // Обновление задачи по ID (полностью заменить)
    void updateTask(Task task);

    // Получение подзадач эпика
    List<Subtask> getSubtasksOfEpic(int epicId);

    List<Task> getHistory();

}
