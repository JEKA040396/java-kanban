import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private List<Subtask> subtasks = new ArrayList<>();

    public Epic(int id, String title, String description, Status status) {
        super(id, title, description, status);
    }

    public List<Subtask> getSubtasks() {
        return subtasks;
    }

    public void addSubtask(Subtask subtask) {
        subtasks.add(subtask);
    }

    public void removeSubtask(Subtask subtask) {
        subtasks.remove(subtask);
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
            Status s = subtask.getStatus();
            if (s != Status.NEW) {
                allNew = false;
            }
            if (s != Status.DONE) {
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
        return "Epic{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                ", subtasks=" + subtasks.size() +
                '}';
    }
}
