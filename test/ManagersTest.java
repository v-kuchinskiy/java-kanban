import manager.HistoryManager;
import manager.Managers;
import manager.TaskManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ManagersTest {

    @Test
    void getDefaultHistoryReturnsInitializedHistory() {
        HistoryManager historyManager = Managers.getDefaultHistory();

        assertNotNull(historyManager);
        assertTrue(historyManager.getHistory().isEmpty());
    }

    @Test
    void getDefaultTaskManagerIsInitialized() {
        TaskManager taskManager = Managers.getDefault();

        assertNotNull(taskManager);
        assertTrue(taskManager.getAllTasks().isEmpty());
    }
}