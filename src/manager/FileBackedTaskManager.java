package manager;

import exceptions.ManagerSaveException;
import model.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import static model.Status.NEW;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final String filePath;

    public FileBackedTaskManager(String filePath) {
        super();
        this.filePath = filePath;
        loadFromFile();
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager manager = new FileBackedTaskManager(file.getAbsolutePath());
        manager.loadFromFile();
        return manager;
    }

    private void loadFromFile() {
        File file = new File(filePath);

        if (!file.exists()) return;

        try {
            String content = Files.readString(file.toPath());
            String[] lines = content.split("\\R");
            boolean headerPassed = false;

            for (String line : lines) {
                if (line.trim().isEmpty()) continue;

                if (!headerPassed) {
                    headerPassed = true;
                    continue;
                }

                if (!line.matches("^\\d+.*")) continue;

                Task task = fromString(line);
                if (task instanceof Epic) {
                    addEpic((Epic) task);
                } else if (task instanceof Subtask) {
                    addSubtask((Subtask) task);
                } else {
                    addTask(task);
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }

    private Task fromString(String line) {
        String[] fields = line.split(",");

        if (fields.length < 5) {
            throw new ManagerSaveException("Некорректная строка в файле: " + line);
        }

        int id;
        id = Integer.parseInt(fields[0]);

        TaskType type;
        type = TaskType.valueOf(fields[1]);

        String name = fields[2];

        Status status = NEW;
        if (!fields[3].isEmpty()) {
            status = Status.valueOf(fields[3]);
        }

        String description;
        if (fields.length > 4) {
            description = fields[4];
        } else {
            description = "";
        }

        int epicId = -1;
        if (type == TaskType.SUBTASK) {
            if (fields.length < 6 || fields[5].isEmpty()) {
                throw new ManagerSaveException("Отсутствует epicId для подзадачи");
            }
            epicId = Integer.parseInt(fields[5]);
        }

        switch (type) {
            case TASK:
                Task task = new Task(name, description);
                task.setId(id);
                task.setStatus(status);
                return task;
            case EPIC:
                Epic epic = new Epic(name, description);
                epic.setId(id);
                epic.setStatus(status);
                return epic;
            case SUBTASK:
                Subtask subtask = new Subtask(name, description, status, epicId);
                subtask.setId(id);
                subtask.setStatus(status);
                return subtask;
            default:
                throw new ManagerSaveException("Неизвестный тип задачи: " + type);
        }
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write("id,type,name,status,description,epic\n");
            Set<Integer> writtenIds = new HashSet<>();

            for (Task task : getAllTasks()) {
                if (!writtenIds.add(task.getId())) {
                    System.out.println("Дубликат ID в коллекции задач: " + task.getId() + ". Пропуск.");
                    continue;
                }
                writer.write(toString(task) + "\n");
            }

            for (Epic epic : getAllEpics()) {
                if (!writtenIds.add(epic.getId())) {
                    System.out.println("Дубликат ID в коллекции эпиков: " + epic.getId() + ". Пропуск.");
                    continue;
                }
                writer.write(toString(epic) + "\n");
            }

            for (Subtask subtask : getAllSubtasks()) {
                if (!writtenIds.add(subtask.getId())) {
                    System.out.println("Дубликат ID в коллекции подзадач: " + subtask.getId() + ". Пропуск.");
                    continue;
                }
                writer.write(toString(subtask) + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка при записи в файл");
        }
    }

    private String toString(Task task) {
        String description = task.getDescription() != null
                ? task.getDescription().replace("\"", "\"\"")
                : "";

        TaskType type = task instanceof Epic ? TaskType.EPIC :
                task instanceof Subtask ? TaskType.SUBTASK : TaskType.TASK;

        String baseFormat = String.format("%d,%s,%s,%s,\"%s\"",
                task.getId(),
                type,
                task.getName(),
                task.getStatus() != null ? task.getStatus() : NEW,
                description);

        return baseFormat + "," + (task instanceof Subtask ? ((Subtask) task).getEpicId() : "");
    }

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
}