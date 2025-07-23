package manager;

import exceptions.ManagerSaveException;
import model.Epic;
import model.Subtask;
import model.Task;
import util.Status;
import util.TaskType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.function.Function;

import static util.Status.NEW;

public class FileBackedTaskManager extends InMemoryTaskManager {
    static final int MIN_REQUIRED_FIELDS = 7;
    static final int SUBTASK_REQUIRED_FIELDS = 8;
    static final int NO_EPIC_ID = -1;
    static final int ID_INDEX = 0;
    static final int TYPE_INDEX = 1;
    static final int NAME_INDEX = 2;
    static final int STATUS_INDEX = 3;
    static final int DESCRIPTION_INDEX = 4;
    static final int START_TIME_INDEX = 5;
    static final int DURATION_INDEX = 6;
    static final int EPIC_ID_INDEX = 7;

    private final String filePath;

    /**
     * Конструктор для создания нового менеджера задач с указанием пути к файлу
     */
    public FileBackedTaskManager(String filePath) {
        super();
        this.filePath = filePath;
        loadFromFile(); // Загрузка данных из файла в память при инициализации
    }

    /**
     * Статический метод для создания нового экземпляра FileBackedTaskManager
     * с предварительной загрузкой данных из указанного файла.
     */
    public static FileBackedTaskManager loadFromFile(File file) {
        if (!file.exists() || !file.canRead()) {
            throw new ManagerSaveException("Не удалось открыть файл для чтения: " + file.getAbsolutePath());
        }

        FileBackedTaskManager manager = new FileBackedTaskManager(file.getAbsolutePath());
        manager.loadFromFile(); // Дополнительная загрузка (если файл изменился после создания)
        return manager;
    }

    /**
     * Обертка вокруг метода fromString
     */
    private Optional<Task> safeFromString(String line) {
        try {
            return Optional.of(fromString(line));
        } catch (IllegalArgumentException e) {
            System.out.println("Некорректная строка: " + line + ". Пропуск.");
            return Optional.empty();
        }
    }

    /**
     * Приватный метод для загрузки задач из файла в текущий менеджер.
     * Если файл не существует или пуст — ничего не происходит.
     */
    private void loadFromFile() {
        File file = new File(filePath);
        if (!file.exists() || file.length() == 0) return;

        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\\R");

            List<Task> tasks = Arrays.stream(lines)
                    .skip(1)
                    .filter(line -> !line.trim().isEmpty())
                    .filter(line -> line.matches("^\\d+.*"))
                    .map(this::safeFromString)
                    .flatMap(Optional::stream)
                    .toList();
            tasks.forEach(task -> {
                if (task instanceof Epic epic) internalAddEpic(epic);
                else if (task instanceof Subtask subtask) internalAddSubtask(subtask);
                else internalAddTask(task);
            });

        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }

    /**
     * Преобразование строки CSV в объект задачи
     */
    private Task fromString(String value) {
        String[] fields = parseCsvLine(value);

        if (fields.length < MIN_REQUIRED_FIELDS) {
            throw new ManagerSaveException("Некорректная строка в файле: " + value);
        }

        int id;
        try {
            id = Integer.parseInt(fields[ID_INDEX]);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректный ID: " + fields[ID_INDEX]);
        }

        TaskType type;
        try {
            type = TaskType.valueOf(fields[TYPE_INDEX]);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Некорректный тип задачи: " + fields[TYPE_INDEX]);
        }

        String name = fields[NAME_INDEX];
        Status status = NEW;
        if (!fields[STATUS_INDEX].isEmpty()) {
            try {
                status = Status.valueOf(fields[STATUS_INDEX]);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Некорректный статус: " + fields[STATUS_INDEX]);
            }
        }

        String description = fields[DESCRIPTION_INDEX].isEmpty() ? "" : fields[DESCRIPTION_INDEX]
                .replace("\"\"", "\"");

        LocalDateTime startTime = null;
        if (!fields[START_TIME_INDEX].isEmpty()) {
            try {
                startTime = LocalDateTime.parse(fields[START_TIME_INDEX], DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Некорректный формат времени: " + fields[START_TIME_INDEX]);
            }
        }

        Duration duration = null;
        if (!fields[DURATION_INDEX].isEmpty()) {
            try {
                duration = Duration.ofMinutes(Long.parseLong(fields[DURATION_INDEX]));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Некорректная продолжительность: " + fields[DURATION_INDEX]);
            }
        }

        int epicId = NO_EPIC_ID;
        if (type == TaskType.SUBTASK) {
            if (fields.length < SUBTASK_REQUIRED_FIELDS || fields[EPIC_ID_INDEX].isEmpty()) {
                throw new ManagerSaveException("Отсутствует epicId для подзадачи");
            }
            try {
                epicId = Integer.parseInt(fields[EPIC_ID_INDEX]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Некорректный epicId: " + fields[EPIC_ID_INDEX]);
            }
        }

        switch (type) {
            case TASK:
                Task task = new Task(name, description, status, duration, startTime);
                task.setId(id);
                task.setStatus(status);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                Subtask subtask = new Subtask(name, description, status, epicId, duration, startTime);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            default:
                throw new ManagerSaveException("Неизвестный тип задачи: " + type);
        }
    }

    /**
     * Преобразование задач в строку CSV
     */
    private String toString(Task task) {
        String description = task.getDescription() != null
                ? task.getDescription().replace("\"", "\"\"")
                : "";

        TaskType type = task instanceof Epic ? TaskType.EPIC :
                task instanceof Subtask ? TaskType.SUBTASK : TaskType.TASK;

        String startTimeString = task.getStartTime() != null
                ? task.getStartTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                : "";
        String durationString = task.getDuration() != null
                ? String.valueOf(task.getDuration().toMinutes())
                : "";

        String baseFormat = String.format("%d,%s,%s,%s,\"%s\", %s, %s",
                task.getId(),
                type,
                task.getName(),
                task.getStatus() != null ? task.getStatus() : NEW,
                description,
                startTimeString,
                durationString);

        return baseFormat + "," + (task instanceof Subtask ? ((Subtask) task).getEpicId() : "");
    }

    /**
     * Метод для разбора CSV-строки с учетом кавычек
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;
        boolean isEscaped = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                if (isEscaped) {
                    currentField.append(c);
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                fields.add(currentField.toString().trim());
                currentField.setLength(0);
            } else if (c == '\\' && inQuotes) {
                isEscaped = true;
            } else {
                currentField.append(c);
            }
        }

        fields.add(currentField.toString().trim());

        while (fields.size() < 6) {
            fields.add("");
        }

        return fields.toArray(new String[0]);
    }

    /**
     * Метод для сохранения текущего состояния в файл
     */
    private void save() {
        Set<Integer> writtenIds = new HashSet<>();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("id,type,name,status,description,start_time,duration,epic\n");

            writeTasks(writer, writtenIds, getAllTasks(), this::toString);
            writeTasks(writer, writtenIds, getAllEpics(), this::toString);
            writeTasks(writer, writtenIds, getAllSubtasks(), this::toString);

            writer.flush();

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при записи в файл");
        }
    }

    /**
     * Метод для записи задач в файл
     */
    private <T extends Task> void writeTasks(
            BufferedWriter writer,
            Set<Integer> writtenIds,
            List<T> tasks, Function<T, String> toStringFunction) {

        tasks.stream()
                .filter(task -> writtenIds.add(task.getId()))
                .map(toStringFunction)
                .forEach(line -> {
                    try {
                        writer.write(line + "\n");
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     * Переопределенные методы для автоматического сохранения при изменении данных
     * Вызывают super-метод для изменения коллекции и save() для сохранения в файл
     */

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void addSubtask(Subtask subtask) {
        super.addSubtask(subtask);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    /**
     * Метод для тестирования работы FileBackedTaskManager.
     * Создаёт задачи, сохраняет их в файл и проверяет корректность загрузки.
     */
    public static void main(String[] args) {
        try {

            File tempFile = File.createTempFile("scenario-task", ".csv");
            System.out.println("Файл создан: " + tempFile.getAbsolutePath());


            FileBackedTaskManager manager1 = new FileBackedTaskManager(tempFile.getAbsolutePath());

            Task task1 = new Task("Task 1", "Description 1", Status.NEW,
                    Duration.ofMinutes(30),
                    LocalDateTime.of(2023, 1, 1, 10, 0));
            manager1.addTask(task1);

            Epic epic1 = new Epic("Epic 1", "Description 1");
            manager1.addEpic(epic1);

            Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW,
                    epic1.getId(),
                    Duration.ofMinutes(60),
                    LocalDateTime.of(2023, 1, 1, 11, 0));
            manager1.addSubtask(subtask1);

            FileBackedTaskManager manager2 = new FileBackedTaskManager(tempFile.getAbsolutePath());

            System.out.println("\nПроверка данных в новом менеджере:");

            assert manager2.getAllTasks().size() == 1 : "Задачи не совпадают";
            Task loadedTask = manager2.getTaskById(task1.getId());
            assert loadedTask != null : "Задача 1 не найдена";
            assert loadedTask.getName().equals("Task 1") : "Неверное название задачи";
            assert loadedTask.getDuration().equals(Duration.ofMinutes(30)) : "Неверная продолжительность задачи";
            assert loadedTask.getStartTime().equals(LocalDateTime.of(
                    2023, 1, 1, 10, 0)) : "Неверное время начала задачи";

            assert manager2.getAllEpics().size() == 1 : "Эпики не совпадают";
            Epic loadedEpic = manager2.getEpicById(epic1.getId());
            assert loadedEpic != null && loadedEpic.getName().equals("Epic 1") : "Эпик 1 не найден";

            assert loadedEpic.getStartTime().equals(subtask1.getStartTime()) : "Неверное время начала эпика";
            assert loadedEpic.getDuration().equals(subtask1.getDuration()) : "Неверная продолжительность эпика";
            assert loadedEpic.getEndTime().equals(subtask1.getEndTime()) : "Неверное время окончания эпика";

            assert manager2.getAllSubtasks().size() == 1 : "Подзадачи не совпадают";
            Subtask loadedSubtask = manager2.getSubtaskById(subtask1.getId());
            assert loadedSubtask != null && loadedSubtask.getName().equals("Subtask 1") : "Подзадача 1 не найдена";
            assert loadedSubtask.getEpicId() == epic1.getId() : "Неверная привязка к эпику";
            assert loadedSubtask.getDuration().equals(Duration.ofMinutes(60)) : "Неверная продолжительность подзадачи";
            assert loadedSubtask.getStartTime().equals(LocalDateTime.of(
                    2023, 1, 1, 11, 0)) : "Неверное время начала подзадачи";

            System.out.println("Все проверки пройдены успешно!");

            tempFile.deleteOnExit();

        } catch (IOException e) {
            System.out.println("Ошибка при работе с файлом: " + e.getMessage());
        }
    }
}