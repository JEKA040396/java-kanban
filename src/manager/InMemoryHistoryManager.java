package manager;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final HashMap<Integer, Node> nodeMap = new HashMap<>();
    private Node head;
    private Node tail;

    private void linkLast(Task task) {
        Node newNode = new Node(tail, task, null);

        if (tail != null) {
            tail.next = newNode;
        }
        tail = newNode;

        if (head == null) {
            head = newNode;
        }

        nodeMap.put(task.getId(), newNode);
    }

    private void removeNode(Node node) {
        if (node == null) {
            return;
        }

        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            // Удаляется голова списка
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            // Удаляется хвост списка
            tail = node.prev;
        }

        nodeMap.remove(node.task.getId());
    }

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        // При добавлении удаляем дубликат (если есть)
        remove(task.getId());
        // Добавляем задачу в конец списка
        linkLast(task);
    }


    @Override
    public void remove(int id) {
        Node node = nodeMap.get(id);
        removeNode(node);
    }

    @Override
    public List<Task> getHistory() {
        List<Task> history = new ArrayList<>();
        Node current = head;
        while (current != null) {
            history.add(current.task);
            current = current.next;
        }
        return history;
    }

    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Node prev, Task task, Node next) {
            this.prev = prev;
            this.task = task;
            this.next = next;
        }
    }

}