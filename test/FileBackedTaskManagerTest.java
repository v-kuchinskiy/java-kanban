import exceptions.ManagerSaveException;
import exceptions.TimeConflictException;
import manager.FileBackedTaskManager;
import manager.InMemoryTaskManager;
import manager.TaskManager;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.Status;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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

    @Test
    void shouldThrowTimeConflictExceptionWhenTimeOverlaps() throws TimeConflictException {
        TaskManager manager = new InMemoryTaskManager();

        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.ofHours(2);

        // Создаем первую задачу (12:00 - 14:00)
        Task task1 = new Task(
                "T1",
                "Description",
                Status.NEW,
                duration,
                now
        );

        // Создаем вторую задачу (13:00 - 15:00) — пересекается с первой
        Task task2 = new Task(
                "T2",
                "Description",
                Status.NEW,
                duration,
                now.plusHours(1)
        );

        // Добавляем первую задачу
        manager.addTask(task1);

        // Пытаемся добавить вторую — должно выбросить исключение
        assertThrows(
                TimeConflictException.class,
                () -> manager.addTask(task2),
                "Ожидалось исключение при пересечении времени задач"
        );
    }

    @Test
    void shouldNotThrowWhenNoTimeOverlap() throws TimeConflictException {
        Task task1 = new Task("T1", "Description", Status.NEW,
                Duration.ofHours(2), LocalDateTime.now());
        Task task2 = new Task("T2", "Description", Status.NEW,
                Duration.ofHours(1), LocalDateTime.now().plusHours(3));

        manager.addTask(task1);
        assertDoesNotThrow(() -> manager.addTask(task2));
    }
}