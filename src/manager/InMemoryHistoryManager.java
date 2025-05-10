package manager;

import model.Task;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private final Map<Integer, Node> nodeMap = new LinkedHashMap<>();
    private Node head;
    private Node tail;

    /**
     * Узел двусвязного списка, хранящий задачу (Task)
     * и ссылки на предыдущий и следующий узлы.
     */
    private static class Node {
        Task task;
        Node prev;
        Node next;

        Node(Task task) {
            this.task = task;
        }
    }

    /**
     * Добавляет задачу в конец связного списка и сохраняет ссылку на неё в nodeMap для быстрого доступа.
     * Если переданная задача равна null, метод завершается без добавления.
     */
    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        Node newNode = new Node(task);
        linkLast(newNode);
        nodeMap.put(task.getId(), newNode);
    }

    /**
     * Удаляет задачу с указанным ID из списка и мапы.
     * Если задача с таким ID не найдена, метод ниччего не делает.
     */
    @Override
    public void remove(int id) {
        Node node = nodeMap.get(id);

        if (node != null) {
            removeNode(node);
        }
    }

    /**
     * Возвращает список всех задач в порядке их хранения в связном списке.
     */
    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    /**
     * Вспомогательный метод, формирующий список задач путем обхода связного списка.
     */
    private List<Task> getTasks() {
        List<Task> task = new ArrayList<>();
        Node current = head;

        while (current != null) {
            task.add(current.task);
            current = current.next;
        }
        return task;
    }

    /**
     * Добавляет узел в конец двусвязного списка, предварительно удаляя существующий узел с таким же ID задачи.
     * Если список пуст, узел становится и головой (head), и хвостом (tail) списка.
     * Иначе узел добавляется после текущего хвоста и становится новым хвостом.
     */
    private void linkLast(Node node) {
        remove(node.task.getId());

        if (head == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            node.prev = tail;
            tail = node;
        }
    }

    /**
     * Удаляет указанный узел из двусвязного списка и мапы.
     * Обрабатывает обновление связей между узлами.
     */
    private void removeNode(Node node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }

        nodeMap.remove(node.task.getId());
    }
}