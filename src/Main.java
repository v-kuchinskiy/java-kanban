import model.*;
import manager.*;

public class Main {
    public static void main(String[] args) {
        TaskManager taskManager = Managers.getDefault();

        Task task1 = new Task("Убраться в комнате", "Разложить все вещи", Status.DONE);
        taskManager.addTask(task1);

        Task task2 = new Task("Сходить в магазин", "Составить список", Status.NEW);
        taskManager.addTask(task2);

        Epic epic1 = new Epic("Приготовить завтрак", "Завтра утром");
        taskManager.addEpic(epic1);

        Subtask subtask1 = new Subtask("Достать продукты из холодильника",
                "Только нужные ингредиенты",
                Status.DONE, epic1.getId());
        taskManager.addSubtask(subtask1);

        Subtask subtask2 = new Subtask("Помыть посуду", "После завтрака",
                Status.IN_PROGRESS, epic1.getId());
        taskManager.addSubtask(subtask2);

        Epic epic2 = new Epic("Собраться на работу", "Встать пораньше");
        taskManager.addEpic(epic2);

        Subtask subtask3 = new Subtask("Выбрать одежду по погоде",
                "Утром может быть прохладно",
                Status.NEW, epic2.getId());
        taskManager.addSubtask(subtask3);

        printAllTasks(taskManager);
    }

    private static void printAllTasks(TaskManager taskManager) {
        System.out.println("Задачи:");
        for (Task task : taskManager.getAllTasks()) {
            System.out.println(task);
        }

        System.out.println("Эпики:");
        for (Task epic : taskManager.getAllEpics()) {
            System.out.println(epic);

            for (Task task : taskManager.getAllSubtasksByEpicId(epic.getId())) {
                System.out.println("--> " + task);
            }
        }

        System.out.println("Подзадачи:");
        for (Task subtask : taskManager.getAllSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : taskManager.getHistory()) {
            System.out.println(task);
        }
    }
}