package manager;

import model.*;
import java.util.*;

public class TaskManager {
    private int nextId = 1;
    private final Map<Integer, Task> tasks = new HashMap<>();
    private final Map<Integer, Subtask> subtasks = new HashMap<>();
    private final Map<Integer, Epic> epics = new HashMap<>();

    // Генерация нового ID
    private int generateId() {
        return nextId++;
    }

    // Получение списка всех задач
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    // Получение списка всех подзадач
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    // Получение списка всех эпиков
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    // Удаление всех задач
    public void deleteAllTasks() {
        tasks.clear();
    }

    // Удаление всех подзадач
    public void deleteAllSubtasks() {
        for (Epic epic : epics.values()) {
            epic.clearSubtasks();
            updateEpicStatus(epic.getId());
        }
        subtasks.clear();
    }

    // Удаление всех эпиков
    public void deleteAllEpics() {
        subtasks.clear(); // Удаляем все подзадачи, так как они не могут существовать без эпиков
        epics.clear();
    }

    // Получение задачи по ID
    public Task getTask(int id) {
        return tasks.get(id);
    }

    // Получение подзадачи по ID
    public Subtask getSubtask(int id) {
        return subtasks.get(id);
    }

    // Получение эпика по ID
    public Epic getEpic(int id) {
        return epics.get(id);
    }

    // Создание задачи
    public Task createTask(Task task) {
        int id = generateId();
        Task newTask = new Task(id, task.getName(), task.getDescription(), task.getStatus());
        tasks.put(id, newTask);
        return newTask;
    }

    // Создание подзадачи
    public Subtask createSubtask(Subtask subtask) {
        if (!epics.containsKey(subtask.getEpicId())) {
            return null; // Эпик не существует
        }

        int id = generateId();
        Subtask newSubtask = new Subtask(id, subtask.getName(), subtask.getDescription(),
                subtask.getStatus(), subtask.getEpicId());
        subtasks.put(id, newSubtask);
        Epic epic = epics.get(newSubtask.getEpicId());
        epic.addSubtask(newSubtask);
        updateEpicStatus(epic.getId());
        return newSubtask;
    }

    // Создание эпика
    public Epic createEpic(Epic epic) {
        int id = generateId();
        Epic newEpic = new Epic(id, epic.getName(), epic.getDescription());
        epics.put(id, newEpic);
        return newEpic;
    }

    // Обновление задачи
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getId())) {
            Task existingTask = tasks.get(task.getId());
            existingTask.setName(task.getName());
            existingTask.setDescription(task.getDescription());
            existingTask.setStatus(task.getStatus());
        }
    }

    // Обновление подзадачи
    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getId())) {
            Subtask existingSubtask = subtasks.get(subtask.getId());
            if (existingSubtask.getEpicId() != subtask.getEpicId()) {
                // Если изменился эпик, нужно обновить связи
                Epic oldEpic = epics.get(existingSubtask.getEpicId());
                if (oldEpic != null) {
                    oldEpic.removeSubtask(subtask.getId());
                    updateEpicStatus(oldEpic.getId());
                }

                Epic newEpic = epics.get(subtask.getEpicId());
                if (newEpic != null) {
                    newEpic.addSubtask(subtask);
                    updateEpicStatus(newEpic.getId());
                }
            }

            existingSubtask.setName(subtask.getName());
            existingSubtask.setDescription(subtask.getDescription());
            existingSubtask.setStatus(subtask.getStatus());
            existingSubtask.setEpicId(subtask.getEpicId());

            updateEpicStatus(subtask.getEpicId());
        }
    }

    // Обновление эпика
    public void updateEpic(Epic epic) {
        if (epics.containsKey(epic.getId())) {
            Epic existingEpic = epics.get(epic.getId());
            existingEpic.setName(epic.getName());
            existingEpic.setDescription(epic.getDescription());
            // Статус эпика не обновляется напрямую, он рассчитывается на основе подзадач
        }
    }

    // Удаление задачи по ID
    public void deleteTask(int id) {
        tasks.remove(id);
    }

    // Удаление подзадачи по ID
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask != null) {
            Epic epic = epics.get(subtask.getEpicId());
            if (epic != null) {
                epic.removeSubtask(id);
                updateEpicStatus(epic.getId());
            }
        }
    }

    // Удаление эпика по ID
    public void deleteEpic(int id) {
        Epic epic = epics.remove(id);
        if (epic != null) {
            // Удаляем все подзадачи этого эпика
            for (Subtask subtask : epic.getSubtasks()) {
                subtasks.remove(subtask.getId());
            }
        }
    }

    // Получение списка подзадач эпика
    public List<Subtask> getEpicSubtasks(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return new ArrayList<>();  // вместо Collections.emptyList()
        }
        return epic.getSubtasks();
    }

    // Обновление статуса эпика на основе его подзадач
    private void updateEpicStatus(int epicId) {
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }

        List<Subtask> epicSubtasks = getEpicSubtasks(epicId);
        if (epicSubtasks.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        boolean allDone = true;
        boolean allNew = true;

        for (Subtask subtask : epicSubtasks) {
            if (subtask.getStatus() != Status.DONE) {
                allDone = false;
            }
            if (subtask.getStatus() != Status.NEW) {
                allNew = false;
            }

            if (!allDone && !allNew) {
                break;
            }
        }

        if (allDone) {
            epic.setStatus(Status.DONE);
        } else if (allNew) {
            epic.setStatus(Status.NEW);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }
}