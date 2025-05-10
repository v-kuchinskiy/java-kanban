import manager.InMemoryTaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryTaskManagerTest {
    @Test
    void addAllTaskTypesAndFindsById() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Task task = new Task("Task1", "Desc", Status.NEW);
        taskManager.addTask(task);

        Subtask subtask = new Subtask("Subtask1", "Desc", Status.NEW, 1);
        taskManager.addSubtask(subtask);

        Epic epic = new Epic("Epic1", "Desc");
        taskManager.addEpic(epic);

        assertEquals(task, taskManager.getTaskById(task.getId()));
        assertEquals(subtask, taskManager.getSubtaskById(subtask.getId()));
        assertEquals(epic, taskManager.getEpicById(epic.getId()));
    }

    @Test
    void testNoIdConflicts() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Task task1 = new Task("Task1", "Desc1", Status.NEW);
        taskManager.addTask(task1);

        assertEquals(task1, taskManager.getTaskById(task1.getId()));
    }

    @Test
    void taskNotChangedAfterAdding() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Subtask sub1 = new Subtask("Subtask1", "Desc1", Status.NEW, 2);
        taskManager.addSubtask(sub1);

        Subtask sub2 = taskManager.getSubtaskById(sub1.getId());

        assertEquals(sub1.getName(), sub2.getName());
        assertEquals(sub1.getDescription(), sub2.getDescription());
        assertEquals(sub1.getStatus(), sub2.getStatus());
        assertEquals(sub1.getEpicId(), sub2.getEpicId());
        assertEquals(sub1.getId(), sub2.getId());
    }

    @Test
    void testUniqueIdGeneration() {
        InMemoryTaskManager taskManager = new InMemoryTaskManager();

        Task task1 = new Task("Task1", "Desc1", Status.NEW);
        taskManager.addTask(task1);

        Task task2 = new Task("Task2", "Desc2", Status.NEW);
        taskManager.addTask(task2);

        assertEquals(task1.getId() + 1, task2.getId());
    }
}