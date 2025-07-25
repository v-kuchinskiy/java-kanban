package manager;

import exceptions.TimeConflictException;
import model.Epic;
import model.Subtask;
import model.Task;

import java.util.List;

public interface TaskManager {

    List<Task> getAllTasks();

    List<Subtask> getAllSubtasks();

    List<Epic> getAllEpics();

    void deleteAllTasks();

    void deleteAllSubtasks();

    void deleteAllEpics();

    Task getTaskById(int id);

    Subtask getSubtaskById(int id);

    Epic getEpicById(int id);

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();

    void addTask(Task task) throws TimeConflictException;

    void addSubtask(Subtask subtask) throws TimeConflictException;

    void addEpic(Epic epic);

    void updateEpicStatus(int epicId);

    void updateTask(Task task) throws TimeConflictException;

    void updateSubtask(Subtask subtask) throws TimeConflictException;

    void updateEpic(Epic epic);

    void deleteTask(int id);

    void deleteSubtask(int id);

    void deleteEpic(int id);

    boolean hasTimeOverlap(Task task);

    List<Subtask> getAllSubtasksByEpicId(int epicId);
}