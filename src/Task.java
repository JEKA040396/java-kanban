public class Task {

    private final int id;
    private String title;
    private String description;
    private Status status;

    public Task(int id, String title, String description, Status status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        int result = Integer.hashCode(id);

        if (title != null) {
            result = 31 * result + title.hashCode();
        } else {
            result = 31 * result;
        }

        if (description != null) {
            result = 31 * result + description.hashCode();
        } else {
            result = 31 * result;
        }

        if (status != null) {
            result = 31 * result + status.hashCode();
        } else {
            result = 31 * result;
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Task other = (Task) obj;

        if (id != other.id) {
            return false;
        }
        if (title == null) {
            if (other.title != null) {
                return false;
            }
        } else {
            if (!title.equals(other.title)) {
                return false;
            }
        }
        if (description == null) {
            if (other.description != null) {
                return false;
            }
        } else {
            if (!description.equals(other.description)) {
                return false;
            }
        }
        if (status == null) {
            return other.status == null;
        } else {
            return status.equals(other.status);
        }
    }


    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", title='" + title + '\'' +
                ", status=" + status +
                '}';
    }
}

