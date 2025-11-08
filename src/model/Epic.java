package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Epic extends Task {

    private final List<Subtask> subtasks;
    private final List<Integer> subtaskIds;
    Duration duration;
    LocalDateTime startTime;
    LocalDateTime endTime;

    public Epic(int id, String title, String description, Status status) {
        super(id, title, description, status);
        this.subtasks = new ArrayList<>();
        this.subtaskIds = new ArrayList<>();
    }

    @Override
    public Duration getDuration() {
        return duration;
    }

    @Override
    public LocalDateTime getStartTime() {
        return startTime;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void updateTimeFields() {
        if (subtasks.isEmpty()) {
            duration = Duration.ZERO;
            startTime = null;
            endTime = null;
            return;
        }

        duration = Duration.ZERO;
        startTime = subtasks.stream()
                .map(Subtask::getStartTime)
                .filter(Objects::nonNull)
                .min(LocalDateTime::compareTo)
                .orElse(null);

        endTime = subtasks.stream()
                .map(subtask -> subtask.getEndTime())
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        for (Subtask subtask : subtasks) {
            Duration subDuration = subtask.getDuration();
            if (subDuration != null) {
                duration = duration.plus(subDuration);
            }
        }
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void addSubtaskId(int id) {
        subtaskIds.add(id);
    }

    public void removeSubtaskId(Integer id) {
        subtaskIds.remove(id);
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask);
        addSubtaskId(subtask.getId());
    }

    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask);
        removeSubtaskId(subtask.getId());
    }

    // Метод для обновления статуса эпика в зависимости от статусов подзадач
    public void updateStatus() {
        if (subtasks.isEmpty()) {
            setStatus(Status.NEW);
            return;
        }
        boolean allNew = true;
        boolean allDone = true;

        for (Subtask subtask : subtasks) {
            Status subtaskStatus = subtask.getStatus();
            if (subtaskStatus != Status.NEW) {
                allNew = false;
            }
            if (subtaskStatus != Status.DONE) {
                allDone = false;
            }
        }

        if (allNew) {
            setStatus(Status.NEW);
        } else if (allDone) {
            setStatus(Status.DONE);
        } else {
            setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public String toString() {
        return "model.Epic{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtasks=" + subtasks.size() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subtasks, epic.subtasks) &&
                Objects.equals(subtaskIds, epic.subtaskIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subtasks, subtaskIds);
    }
}
