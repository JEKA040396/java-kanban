package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private static String toString(Task task) {
        String type;
        String epicId = "";

        if (task instanceof Epic) {
            type = "EPIC";
        } else if (task instanceof Subtask subtask) {
            type = "SUBTASK";
            epicId = String.valueOf(subtask.getEpicId());
        } else {
            type = "TASK";
        }

        return String.format("%d,%s,%s,%s,%s,%s",
                task.getId(),
                type,
                task.getTitle(),
                task.getStatus(),
                task.getDescription(),
                epicId);
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",");
        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String title = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        switch (type) {
            case "TASK":
                return new Task(id, title, description, status);
            case "EPIC":
                return new Epic(id, title, description, status);
            case "SUBTASK":
                int epicId = Integer.parseInt(parts[5]);
                return new Subtask(id, title, description, status, epicId);
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (int i = 1; i < lines.size(); i++) { // пропускаем заголовок
                String line = lines.get(i);
                if (line.isEmpty()) continue;

                Task task = fromString(line);
                if (task instanceof Epic epic) {
                    manager.epics.put(epic.getId(), epic);
                } else if (task instanceof Subtask subtask) {
                    manager.subtasks.put(subtask.getId(), subtask);
                    Epic epic = manager.epics.get(subtask.getEpicId());
                    if (epic != null) {
                        epic.addSubtask(subtask);
                        epic.updateStatus();
                    }
                } else {
                    manager.tasks.put(task.getId(), task);
                }
                if (task.getId() >= manager.nextId) {
                    manager.nextId = task.getId() + 1;
                }
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке из файла", e);
        }
        return manager;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,epic\n");

            // Сохраняем все задачи
            for (Task task : getAllTasks()) {
                writer.write(toString(task) + "\n");
            }

            // Сохраняем все эпики
            for (Epic epic : getAllEpics()) {
                writer.write(toString(epic) + "\n");
            }

            // Сохраняем все подзадачи
            for (Subtask subtask : getAllSubtasks()) {
                writer.write(toString(subtask) + "\n");
            }

            // Можно расширить сохранение истории (например, через отдельную строку)

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл", e);
        }
    }

    // Переопределяем методы модификации, чтобы сохранить файл после каждой операции

    @Override
    public Task createTask(String title, String description, Status status) {
        Task task = super.createTask(title, description, status);
        save();
        return task;
    }

    @Override
    public Epic createEpic(String title, String description, Status status) {
        Epic epic = super.createEpic(title, description, status);
        save();
        return epic;
    }

    @Override
    public Subtask createSubtask(String title, String description, Status status, int epicId) {
        Subtask subtask = super.createSubtask(title, description, status, epicId);
        save();
        return subtask;
    }

    @Override
    public void removeById(int id) {
        super.removeById(id);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }
}


