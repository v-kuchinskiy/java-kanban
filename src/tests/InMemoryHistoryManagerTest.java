package tests;

import model.*;
import manager.*;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryHistoryManagerTest {
    @Test
    void historyKeepsPreviousTaskVersions() {
        HistoryManager historyManager = new InMemoryHistoryManager();

        InMemoryTaskManager taskManager = new InMemoryTaskManager(historyManager);

        Task initialTask = new Task("Task", "Desc", Status.NEW);
        taskManager.addTask(initialTask);
        taskManager.getTaskById(initialTask.getId());
        List<Task> historyList = historyManager.getHistory();

        assertEquals(1, historyList.size());
        assertEquals(Status.NEW, historyList.get(0).getStatus());

        Task updatedTask = new Task("Task", "Desc", Status.IN_PROGRESS);
        updatedTask.setId(initialTask.getId());
        taskManager.updateTask(updatedTask);
        taskManager.getTaskById(updatedTask.getId());
        historyList = historyManager.getHistory();

        assertEquals(2, historyList.size());
        assertEquals(Status.NEW, historyList.get(0).getStatus());
        assertEquals(Status.IN_PROGRESS, historyList.get(1).getStatus());
    }
}