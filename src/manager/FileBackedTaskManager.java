package manager;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final File file;

    public FileBackedTaskManager(File file) {
        this.file = file;
    }

    private static String toString(Task task) {
        String type;
        String epicId = "";
        String duration = task.getDuration() != null ? String.valueOf(task.getDuration().toMinutes()) : "";
        String startTime = task.getStartTime() != null ? task.getStartTime().format(formatter) : "";

        if (task instanceof Epic) {
            type = "EPIC";
        } else if (task instanceof Subtask subtask) {
            type = "SUBTASK";
            epicId = String.valueOf(subtask.getEpicId());
        } else {
            type = "TASK";
        }

        return String.format("%d,%s,%s,%s,%s,%s,%s,%s",
                task.getId(),
                type,
                task.getTitle(),
                task.getStatus(),
                task.getDescription(),
                epicId,
                duration,
                startTime);
    }

    private static Task fromString(String value) {
        String[] parts = value.split(",");

        // Базовые поля, которые есть в старом и новом формате
        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String title = parts[2];
        Status status = Status.valueOf(parts[3]);
        String description = parts[4];

        // Обработка в зависимости от количества полей
        if (parts.length == 5) {
            // Старый формат без временных полей и epicId для TASK
            switch (type) {
                case "TASK":
                    return new Task(id, title, description, status);
                case "EPIC":
                    return new Epic(id, title, description, status);
                case "SUBTASK":
                    // В старом формате не должно быть SUBTASK без epicId
                    throw new IllegalArgumentException("Неверный формат данных для подзадачи: " + value);
                default:
                    throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
            }
        }

        String epicIdStr = parts[5];

        // Для нового формата с временными полями
        String durationStr = "";
        String startTimeStr = "";

        if (parts.length > 6) {
            durationStr = parts[6];
        }
        if (parts.length > 7) {
            startTimeStr = parts[7];
        }

        Duration duration = durationStr.isEmpty() ? null : Duration.ofMinutes(Long.parseLong(durationStr));
        LocalDateTime startTime = startTimeStr.isEmpty() ? null : LocalDateTime.parse(startTimeStr, formatter);

        switch (type) {
            case "TASK":
                Task task = new Task(id, title, description, status);
                task.setDuration(duration);
                task.setStartTime(startTime);
                return task;
            case "EPIC":
                Epic epic = new Epic(id, title, description, status);
                return epic;
            case "SUBTASK":
                int epicId;
                try {
                    epicId = Integer.parseInt(epicIdStr);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Неверный формат epicId: " + epicIdStr);
                }
                Subtask subtask = new Subtask(id, title, description, status, epicId);
                subtask.setDuration(duration);
                subtask.setStartTime(startTime);
                return subtask;
            default:
                throw new IllegalArgumentException("Неизвестный тип задачи: " + type);
        }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file);
        try {
            if (!file.exists()) {
                return manager;
            }

            List<String> lines = Files.readAllLines(file.toPath());

            // Пропускаем заголовок и проверяем, что есть данные
            if (lines.size() <= 1) {
                return manager;
            }

            // Сначала загружаем все эпики
            List<String> subtaskLines = new ArrayList<>();

            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.isEmpty()) continue;

                try {
                    Task task = fromString(line);
                    if (task instanceof Epic epic) {
                        manager.epics.put(epic.getId(), epic);
                    } else if (task instanceof Subtask) {
                        // Откладываем обработку подзадач до загрузки всех эпиков
                        subtaskLines.add(line);
                    } else {
                        manager.tasks.put(task.getId(), task);
                        if (task.getStartTime() != null) {
                            manager.prioritizedTasks.add(task);
                        }
                    }

                    if (task.getId() >= manager.nextId) {
                        manager.nextId = task.getId() + 1;
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка при загрузке строки: " + line + ", ошибка: " + e.getMessage());
                }
            }

            // Теперь обрабатываем подзадачи
            for (String line : subtaskLines) {
                try {
                    Task task = fromString(line);
                    if (task instanceof Subtask subtask) {
                        manager.subtasks.put(subtask.getId(), subtask);
                        Epic epic = manager.epics.get(subtask.getEpicId());
                        if (epic != null) {
                            epic.addSubtask(subtask);
                            epic.updateStatus();
                            epic.updateTimeFields();
                        }
                        if (subtask.getStartTime() != null) {
                            manager.prioritizedTasks.add(subtask);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Ошибка при загрузке подзадачи: " + line + ", ошибка: " + e.getMessage());
                }
            }

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при загрузке из файла", e);
        }
        return manager;
    }

    public void save() {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("id,type,name,status,description,epic,duration,startTime\n");

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

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при сохранении в файл", e);
        }
    }

    // Переопределяем методы для обратной совместимости

    @Override
    public Task createTask(String title, String description, Status status) {
        Task task = super.createTask(title, description, status);
        save();
        return task;
    }

    @Override
    public Task createTask(String title, String description, Status status, Duration duration, LocalDateTime startTime) {
        Task task = super.createTask(title, description, status, duration, startTime);
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
    public Subtask createSubtask(String title, String description, Status status, int epicId,
                                 Duration duration, LocalDateTime startTime) {
        Subtask subtask = super.createSubtask(title, description, status, epicId, duration, startTime);
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

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeAllSubtasks() {
        super.removeAllSubtasks();
        save();
    }

    @Override
    public void removeAllEpics() {
        super.removeAllEpics();
        save();
    }
}