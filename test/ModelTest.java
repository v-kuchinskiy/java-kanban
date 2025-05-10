import manager.InMemoryTaskManager;
import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ModelTest {

    /**
     * Проверяет, что задачи (Task) с разными идентификаторами (id) не считаются равными,
     * даже если все остальные поля (название, описание, статус) совпадают.
     */
    @Test
    void tasksAreNotEqualIfDifferentIds() {
        Task task1 = new Task("Task", "Description", Status.NEW);
        Task task2 = new Task("Task", "Description", Status.NEW);
        task1.setId(1);
        task2.setId(2);
        assertNotEquals(task1, task2, "Задачи с разными id не должны быть равны");
    }

    /**
     * Тест проверяет, что две задачи (Task) считаются равными, если их идентификаторы (id) совпадают,
     * даже если остальные поля (название, описание) различаются.
     */
    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task1", "Description 1", Status.NEW);
        Task task2 = new Task("Task2", "Description 2", Status.IN_PROGRESS);
        task1.setId(1);
        task2.setId(1);
        assertEquals(task1, task2, "Задачи с одинаковым id должны быть равны");
    }

    /**
     * Проверяет, что задачи разных типов (Task, Subtask, Epic)
     * считаются равными, если их id совпадают.
     */
    @Test
    void tasksWithSameIdAreEqualRegardlessOfType() {
        Task task1 = new Task("Task1", "Description", Status.NEW);
        Subtask task2 = new Subtask("Task2", "Description", Status.NEW, 1);
        Epic task3 = new Epic("Task3", "Description");
        task1.setId(1);
        task2.setId(1);
        task3.setId(1);
        assertEquals(task1, task2, "Task и Subtask с одинаковым id должны быть равны");
        assertEquals(task1, task3, "Task и Epic с одинаковым id должны быть равны");
    }

    /**
     * Тест проверяет, что подзадача не может быть собственной эпик-задачей.
     * Создается подзадача с ID, совпадающим с ID эпика (1).
     * При попытке добавить такую подзадачу будет выброшено IllegalArgumentException.
     */
    @Test
    void subtaskCanNotBeSelfEpic() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, 1);
        subtask.setId(1);
        assertThrows(IllegalArgumentException.class, () -> tm.addSubtask(subtask));
    }

    /**
     * Тест проверяет, что эпик не может быть подзадачей для самого себя.
     * Создается эпик с идентификатором 1 и попытка добавить его же ID в список подзадач.
     * Ожидается, что список подзадач останется пустым (размер = 0).
     */
    @Test
    void canNotAddSelfAsSubtask() {
        Epic epic = new Epic("Epic", "Description");
        epic.setId(1);
        epic.addSubtask(1);
        assertEquals(0, epic.getSubtaskIds().size());
    }

    /**
     * Тест проверяет, что нельзя добавить дубликат подзадачи в эпик.
     * Создается эпик и подзадача, которая добавляется в менеджер и привязывается к эпику.
     * При попытке повторно добавить ту же подзажду в эпик ожидается, что список подзадач
     * эпика останется неизменным (размер списка = 1).
     */
    @Test
    void canNotAddDuplicateSubtask() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Epic epic = new Epic("Epic", "Description");
        tm.addEpic(epic);
        Subtask sub = new Subtask("Subtask", "Description", Status.NEW, 1);
        tm.addSubtask(sub);
        epic.addSubtask(1);
        epic.addSubtask(1);
        assertEquals(1, epic.getSubtaskIds().size(),
                "При добавлении дубликата подзадачи размер списка подзадач эпика должен остаться 1");
    }
}