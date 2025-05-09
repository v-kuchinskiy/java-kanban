import manager.InMemoryHistoryManager;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryHistoryManagerTest {

    private InMemoryHistoryManager historyManager;
    private Task task1;
    private Task task2;
    private Task task3;

    @BeforeEach
    void setUp() {
        historyManager = new InMemoryHistoryManager();
        task1 = new Task("Task 1", "Description 1");
        task1.setId(1);
        task2 = new Task("Task 2", "Description 2");
        task2.setId(2);
        task3 = new Task("Task 3", "Description 3");
        task3.setId(3);
    }

    // Проверяет, что задача успешно добавляется в историю просмотров
    @Test
    void addShouldAddTaskToHistory() {
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
    }

    // Проверяет, что попытка добавить null не приводит к добавлению в историю
    @Test
    void addShouldNotAddNullTask() {
        historyManager.add(null);
        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty());
    }

    // Проверяет, что при повторном добавлении задачи она перемещается в конец истории
    @Test
    void addShouldMoveExistingTaskToEnd() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);
        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task2, history.get(0));
        assertEquals(task1, history.get(1));
    }

    // Проверяет корректность удаления задачи из истории
    @Test
    void removeShouldDeleteTaskFromHistory() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(task1.getId());
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task2, history.get(0));
    }

    // Проверяет, что попытка удалить несуществующую задачу не влияет на историю
    @Test
    void removeShouldDoNothingWhenTaskNotExists() {
        historyManager.add(task1);
        historyManager.remove(999); // несуществующий ID
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
    }

    // Проверяет, что для пустой истории возвращается пустой список
    @Test
    void getHistoryShouldReturnEmptyListWhenNoTasks() {
        List<Task> history = historyManager.getHistory();

        assertTrue(history.isEmpty());
    }

    // Проверяет, что задачи возвращаются в порядке их добавления
    @Test
    void getHistoryShouldReturnTasksInOrderOfAddition() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task2, history.get(1));
        assertEquals(task3, history.get(2));
    }

    // Проверяет, что в истории не сохраняются дубликаты задач
    @Test
    void historyShouldNotContainDuplicates() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task1);
        historyManager.add(task3);
        historyManager.add(task2);
        List<Task> history = historyManager.getHistory();

        assertEquals(3, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
        assertEquals(task2, history.get(2));
    }

    // Проверяет корректность удаления головного узла двусвязного списка
    @Test
    void removeShouldWorkCorrectlyForHeadNode() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(task1.getId());
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task2, history.get(0));
        assertFalse(history.contains(task1));
    }

    // Проверяет корректность удаления хвостового узла двусвязного списка
    @Test
    void removeShouldWorkCorrectlyForTailNode() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.remove(task2.getId());
        List<Task> history = historyManager.getHistory();

        assertEquals(1, history.size());
        assertEquals(task1, history.get(0));
        assertFalse(history.contains(task2));
    }

    // Проверяет корректность удаления среднего узла двусвязного списка
    @Test
    void removeShouldWorkCorrectlyForMiddleNode() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(task3);
        historyManager.remove(task2.getId());
        List<Task> history = historyManager.getHistory();

        assertEquals(2, history.size());
        assertEquals(task1, history.get(0));
        assertEquals(task3, history.get(1));
        assertFalse(history.contains(task2));
    }
}