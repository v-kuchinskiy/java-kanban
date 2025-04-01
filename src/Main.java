import manager.TaskManager;
import model.*;

public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = new TaskManager();

        Task task1 = new Task(0, "Убраться в комнате", "Разложить все вещи", Status.DONE);
        taskManager.createTask(task1);

        Task task2 = new Task(0, "Сходить в магазин", "Составить список", Status.NEW);
        taskManager.createTask(task2);

//        Epic epic1 = new Epic(0, "Приготовить завтрак", "Завтра утром");
//        manager.createEpic(epic1);

//        Subtask subtask1 = new Subtask(0, "Достать продукты из холодильника",
//                "Только нужные ингредиенты", Status.DONE, epic1.getId());
//        manager.createSubtask(subtask1);
//
//        Subtask subtask2 = new Subtask(0, "Помыть посуду",
//                "После завтрака", Status.IN_PROGRESS, epic1.getId());
//        manager.createSubtask(subtask2);

        Epic epic1 = new Epic(0, "Разработать приложение", "Создать новое мобильное приложение");
        Epic createdEpic = taskManager.createEpic(epic1);

        Subtask subtask1 = new Subtask(0, "Дизайн интерфейса", "Создать макеты экранов",
                Status.NEW, createdEpic.getId());
        Subtask subtask2 = new Subtask(0, "Реализация API", "Написать backend часть",
                Status.NEW, createdEpic.getId());

        Subtask createdSubtask1 = taskManager.createSubtask(subtask1);
        Subtask createdSubtask2 = taskManager.createSubtask(subtask2);

        Epic epic2 = new Epic(0, "Собраться на работу", "Встать пораньше");
        taskManager.createEpic(epic2);

        Subtask subtask3 = new Subtask(0, "Выбрать одежду по погоде",
                "Утром может быть прохладно", Status.NEW, epic2.getId());
        taskManager.createSubtask(subtask3);

        System.out.println(taskManager.getAllTasks());
        System.out.println(taskManager.getAllEpics());
        System.out.println(taskManager.getAllSubtasks());

        System.out.println("Количество эпиков: " + taskManager.getAllEpics().size());
    }
}