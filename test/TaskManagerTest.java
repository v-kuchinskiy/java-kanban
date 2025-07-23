import exceptions.TimeConflictException;
import manager.HistoryManager;
import manager.InMemoryHistoryManager;
import manager.TaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import util.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Абстрактный обобщенный класс для тестирования различных реализаций TaskManager.
 */
public abstract class TaskManagerTest<T extends TaskManager> {
    protected T manager;
    LocalDateTime baseTime = util.TestData.BASE_TIME;

    /**
     * Проверка корректности расчета статуса эпика в зависимости от статуса подзадач.
     */
    @Test
    public void testEpicStatusCalculation() {
        Epic epic = new Epic("E", "D");
        manager.addEpic(epic);

        // NEW
        Subtask subtask1 = new Subtask("S1", "D1", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), baseTime);
        manager.addSubtask(subtask1);
        assertEquals(Status.NEW, epic.getStatus());

        // DONE
        subtask1.setStatus(Status.DONE);
        manager.updateSubtask(subtask1);
        assertEquals(Status.DONE, epic.getStatus());

        // NEW и DONE
        Subtask subtask2 = new Subtask("S2", "D2", Status.NEW, epic.getId(),
                Duration.ofMinutes(60), baseTime.plusHours(1));
        manager.addSubtask(subtask2);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());

        // IN_PROGRESS
        subtask2.setStatus(Status.IN_PROGRESS);
        manager.updateSubtask(subtask2);
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    /**
     * Проверка, что при попытке добавить задачу с пересекающимся временем выполнения
     * будет выброшено исключение.
     */
    @Test
    public void testTimeOverlap() {
        Task task1 = new Task("T1", "D1", Status.NEW,
                Duration.ofHours(1), baseTime);
        manager.addTask(task1);
        Task task2 = new Task("T2", "D2", Status.NEW,
                Duration.ofHours(2), baseTime.plusMinutes(30));
        assertThrows(TimeConflictException.class, () -> manager.addTask(task2));
    }

    /**
     * Проверка, что deleteAllTasks() корректно очищает список всех задач.
     */
    @Test
    void testDeleteAllTasks() {
        Task task1 = new Task("T1", "D1");
        Task task2 = new Task("T2", "D2");
        manager.addTask(task1);
        manager.addTask(task2);
        Assertions.assertDoesNotThrow(() -> manager.deleteAllTasks(),
                "Удаление всех задач не должно вызывать исключений");
        assertTrue(manager.getAllTasks().isEmpty());
    }

    /**
     * Проверка правильности удаления задачи из середины истории просмотров.
     */
    @Test
    void testRemoveFromMiddle() {
        HistoryManager hm = new InMemoryHistoryManager();
        Task t1 = new Task("T1", "D1");
        t1.setId(1);
        Task t2 = new Task("T2", "D2");
        t2.setId(2);
        Task t3 = new Task("T3", "D3");
        t3.setId(3);
        hm.add(t1);
        hm.add(t2);
        hm.add(t3);
        Assertions.assertDoesNotThrow(() -> hm.remove(2),
                "Удаление задачи из истории не должно вызывать исключений");
        List<Task> history = hm.getHistory();
        assertEquals(List.of(t1, t3), history);
    }

    /**
     * Проверка, что хэш-код задачи зависит только от её содержимого, а не от ссылки на объект.
     */
    @Test
    void testHashCodeConsistency() {
        Task task1 = new Task("T", "D");
        task1.setId(1);
        Task task2 = new Task("T", "D");
        task2.setId(1);
        assertEquals(task1.hashCode(), task2.hashCode());
    }
}