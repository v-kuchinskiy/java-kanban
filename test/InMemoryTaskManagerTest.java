import manager.InMemoryTaskManager;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Status;
import util.TestData;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    LocalDateTime baseTime = TestData.BASE_TIME;

    @BeforeEach
    void createManager() {
        manager = new InMemoryTaskManager();
    }

    @Test
    void addAllTaskTypesAndFindsById() {
        InMemoryTaskManager tm = new InMemoryTaskManager();

        // Создаём задачи
        Task task = new Task("T1", "D1", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        Epic epic = new Epic("E1", "D1");
        Subtask subtask = new Subtask("S1", "D1", Status.NEW, epic.getId(),
                Duration.ofMinutes(30), baseTime.plusHours(1));

        tm.addTask(task);
        tm.addEpic(epic);
        tm.addSubtask(subtask);

        Task loadedTask = tm.getTaskById(task.getId());
        assertNotNull(loadedTask, "Задача не должна быть null");
        assertEquals(task, loadedTask);

        Subtask loadedSubtask = tm.getSubtaskById(subtask.getId());
        assertNotNull(loadedSubtask, "Подзадача не должна быть null");
        assertEquals(subtask, loadedSubtask);

        Epic loadedEpic = tm.getEpicById(epic.getId());
        assertNotNull(loadedEpic, "Эпик не должен быть null");
        assertEquals(epic, loadedEpic);
    }

    @Test
    void testNoIdConflicts() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Task task1 = new Task("T1", "D1", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        tm.addTask(task1);

        Task loaded = tm.getTaskById(task1.getId());
        assertNotNull(loaded, "Задача не должна быть null");

        assertEquals(task1.getName(), loaded.getName());
        assertEquals(task1.getDescription(), loaded.getDescription());
        assertEquals(task1.getStatus(), loaded.getStatus());
        assertEquals(task1.getId(), loaded.getId());
        assertEquals(task1.getDuration(), loaded.getDuration());
        assertEquals(task1.getStartTime(), loaded.getStartTime());
    }

    @Test
    void taskUnchangedAfterAdding() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Subtask sub1 = new Subtask("S1", "D1", Status.NEW, 2,
                Duration.ofMinutes(30), baseTime);
        tm.addSubtask(sub1);
        Subtask sub2 = tm.getSubtaskById(sub1.getId());

        assert sub2 != null;
        assertEquals(sub1.getName(), sub2.getName());
        assertEquals(sub1.getDescription(), sub2.getDescription());
        assertEquals(sub1.getStatus(), sub2.getStatus());
        assertEquals(sub1.getEpicId(), sub2.getEpicId());
        assertEquals(sub1.getId(), sub2.getId());
    }

    @Test
    void testUniqueIdGeneration() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Task task1 = new Task("T1", "D1", Status.NEW,
                Duration.ofMinutes(30), baseTime);
        Task task2 = new Task("T2", "D2", Status.NEW,
                Duration.ofMinutes(30), baseTime.plusHours(1));
        tm.addTask(task1);
        tm.addTask(task2);
        assertEquals(task1.getId() + 1, task2.getId());
    }

    @Test
    void testGetPrioritizedTasks_SortsByStartTime() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Task t1 = new Task("T1", "D1", Status.NEW, Duration.ofMinutes(30), baseTime.plusHours(2));
        Task t2 = new Task("T2", "D2", Status.NEW, Duration.ofMinutes(30), baseTime);
        Task t3 = new Task("T3", "D3", Status.NEW, Duration.ofMinutes(30), baseTime.plusHours(1));
        Task t4 = new Task("T4", "D4", Status.NEW, Duration.ofMinutes(30), baseTime.plusHours(3));
        tm.addTask(t2);
        tm.addTask(t3);
        tm.addTask(t1);
        tm.addTask(t4);
        List<Task> prioritized = tm.getPrioritizedTasks();
        assertEquals(Arrays.asList(t2, t3, t1, t4), prioritized);
    }
}