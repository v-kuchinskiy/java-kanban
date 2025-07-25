package manager;

import exceptions.TimeConflictException;
import model.Epic;
import model.Subtask;
import model.Task;
import util.Managers;
import util.Status;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int idCounter;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Subtask> subtasks;
    private final HashMap<Integer, Epic> epics;
    private final HistoryManager historyManager;

    private boolean isUpdatingStatus = false;
    private final Set<Task> prioritizedTasks = new TreeSet<>(
            Comparator.comparing(
                    Task::getStartTime,
                    Comparator.nullsLast(Comparator.naturalOrder())
            )
    );

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.idCounter = 1;
        this.tasks = new HashMap<>();
        this.subtasks = new HashMap<>();
        this.epics = new HashMap<>();
        this.historyManager = historyManager;
    }

    public InMemoryTaskManager() {
        this(Managers.getDefaultHistory());
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        subtasks.clear();
        epics.values().forEach(epic -> epic.setSubtaskIds(new ArrayList<>()));
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);

        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);

        if (subtask != null) {
            historyManager.add(subtask);
        }
        return subtask;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);

        if (epic != null) {
            historyManager.add(epic);
        }
        return epic;
    }

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistory());
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    @Override
    public void addTask(Task task) throws TimeConflictException {
        if (hasTimeOverlap(task)) {
            throw new TimeConflictException("Задача пересекается по времени с существующей.");
        }
        task.setId(generateId());
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void addSubtask(Subtask subtask) throws TimeConflictException {
        if (hasTimeOverlap(subtask)) {
            throw new TimeConflictException("Подзадача пересекается по времени с существующей.");
        }
        subtask.setId(generateId());
        if (subtask.getEpicId() == subtask.getId()) {
            throw new IllegalArgumentException("Подзадача не может быть эпиком для себя");
        }
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subtask.getId());
            updateEpicStatus(subtask.getEpicId());
        }
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }

    @Override
    public void updateEpicStatus(int epicId) {
        if (isUpdatingStatus) return;
        isUpdatingStatus = true;
        try {
            Epic epic = epics.get(epicId);
            if (epic == null) {
                return;
            }
            List<Subtask> actualSubtasks = getAllSubtasksByEpicId(epicId);
            if (actualSubtasks.isEmpty()) {
                epic.setStatus(Status.NEW);
                return;
            }
            boolean isAllDone = areAllSubtasks(actualSubtasks, Status.DONE);
            boolean isAllNew = areAllSubtasks(actualSubtasks, Status.NEW);

            epic.setStatus(
                    isAllDone ? Status.DONE :
                            isAllNew ? Status.NEW :
                                    Status.IN_PROGRESS
            );
        } finally {
            isUpdatingStatus = false;
        }
    }

    @Override
    public void updateTask(Task task) throws TimeConflictException {
        if (hasTimeOverlap(task)) {
            throw new TimeConflictException("Задача пересекается по времени с существующей.");
        }
        prioritizedTasks.remove(task);
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public void updateSubtask(Subtask subtask) throws TimeConflictException {
        Subtask old = subtasks.get(subtask.getId());
        if (old != null && !old.equals(subtask)) {
            if (hasTimeOverlap(subtask)) {
                throw new TimeConflictException("Подзадача пересекается по времени");
            }
        }
        subtasks.put(subtask.getId(), subtask);
        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            updateEpicStatus(epic.getId());
        }
        prioritizedTasks.remove(subtask);
        if (subtask.getStartTime() != null) {
            prioritizedTasks.add(subtask);
        }
    }

    public void updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);
    }

    @Override
    public void deleteTask(int id) {
        tasks.remove(id);
        historyManager.remove(id);
        prioritizedTasks.remove(tasks.get(id));
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            int epicId = subtask.getEpicId();
            Epic epic = epics.get(epicId);
            if (epic != null) {
                if (epic.getSubtaskIds().contains(id)) {
                    epic.getSubtaskIds().remove(Integer.valueOf(id));
                }
                updateEpicStatus(epic.getId());
            }
        }
        subtasks.remove(id);
        historyManager.remove(id);
        prioritizedTasks.remove(subtask);
    }

    @Override
    public void deleteEpic(int id) {
        Optional.ofNullable(epics.get(id))
                .map(Epic::getSubtaskIds)
                .ifPresent(ids -> ids.forEach(this::deleteSubtask));
        epics.remove(id);
        historyManager.remove(id);
    }

    @Override
    public List<Subtask> getAllSubtasksByEpicId(int epicId) {
        return Optional.ofNullable(epics.get(epicId))
                .map(Epic::getSubtaskIds)
                .stream()
                .flatMap(List::stream)
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Проверка пересечения времени (линейная сложность O(n))
     */
    @Override
    public boolean hasTimeOverlap(Task newTask) {
        if (newTask.getStartTime() == null) {
            return false;
        }
        return prioritizedTasks.stream()
                .anyMatch(existingTask -> existingTask.isOverlapping(newTask));
    }

    private int generateId() {
        return idCounter++;
    }

    private boolean areAllSubtasks(List<Subtask> subtasks, Status status) {
        return subtasks.stream().allMatch(task -> task.getStatus() == status);
    }

    /**
     * Добавление задач при загрузке из файла
     * Данные уже были добавлены при вызове addTask или addSubtask
     * Дополнительная проверка в этом случае избыточна
     */
    protected void internalAddTask(Task task) {
        task.setId(generateId());
        tasks.put(task.getId(), task);
        if (task.getStartTime() != null) {
            prioritizedTasks.add(task);
        }
    }

    protected void internalAddSubtask(Subtask subtask) {
        subtask.setId(generateId());
        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());
        if (epic != null) {
            epic.addSubtask(subtask.getId());
            updateEpicStatus(subtask.getEpicId());
        }
    }

    protected void internalAddEpic(Epic epic) {
        epic.setId(generateId());
        epics.put(epic.getId(), epic);
    }
}