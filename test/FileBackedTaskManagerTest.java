import exceptions.ManagerSaveException;
import manager.FileBackedTaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Тестовый класс для проверки реализации FileBackedTaskManager.
 * Проверка корректности работы с файлом: загрузка, сохранение и обработка ошибок.
 */
public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private File tempFile;

    @BeforeEach
    void setUp() throws Exception {

        tempFile = File.createTempFile("task-manager-test", ".csv");
        tempFile.deleteOnExit();

        manager = new FileBackedTaskManager(tempFile.getAbsolutePath());
    }

    @Test
    public void testFileOperations() {
        assertThrows(ManagerSaveException.class, () -> {
            File invalidFile = new File("invalid_path.csv");
            FileBackedTaskManager.loadFromFile(invalidFile);
        });
    }
}