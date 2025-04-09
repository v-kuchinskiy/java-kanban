package tests;

import model.*;
import manager.*;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ModelTest {
    @Test
    void tasksAreNotEqualIfDifferentIds() {
        Task task1 = new Task("Task1", "Desc1", Status.NEW);
        Task task2 = new Task("Task2", "Desc2", Status.NEW);
        task1.setId(1);
        task2.setId(2);
        assertNotEquals(task1, task2);
    }

    @Test
    void tasksWithSameIdShouldBeEqual() {
        Task task1 = new Task("Task1", "Desc1", Status.NEW);
        Task task2 = new Task("Task2", "Desc2", Status.NEW);
        task1.setId(1);
        task2.setId(1);
        assertEquals(task1, task2);
    }

    @Test
    void tasksWithSameIdAreEqualRegardlessOfType() {
        Task task1 = new Task("Task1", "Desc1", Status.NEW);
        Subtask task2 = new Subtask("Task2", "Desc2", Status.NEW, 1);
        Epic task3 = new Epic("Task3", "Desc3");
        task1.setId(1);
        task2.setId(1);
        task3.setId(1);
        assertEquals(task1, task2);
        assertEquals(task1, task3);
    }

    @Test
    void subtaskCanNotBeSelfEpic() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Subtask subtask = new Subtask("Subtask", "Desc", Status.NEW, 1);
        subtask.setId(1);
        assertThrows(IllegalArgumentException.class, () -> tm.addSubtask(subtask));
    }

    @Test
    void canNotAddSelfAsSubtask() {
        Epic epic = new Epic("Epic", "Desc");
        epic.setId(1);
        epic.addSubtask(1);
        assertEquals(0, epic.getSubtaskIds().size());
    }

    @Test
    void canNotAddDuplicateSubtask() {
        InMemoryTaskManager tm = new InMemoryTaskManager();
        Epic epic = new Epic("Epic", "Desc");
        tm.addEpic(epic);
        Subtask sub = new Subtask("Subtask", "Desc", Status.NEW, 1);
        tm.addSubtask(sub);
        epic.addSubtask(1);
        epic.addSubtask(1);
        assertEquals(1, epic.getSubtaskIds().size());
    }
}