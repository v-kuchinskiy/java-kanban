import manager.TaskManager;
import model.*;

public class Main {

    public static void main(String[] args) {

        TaskManager manager = new TaskManager();

        Task task1 = new Task("Убраться в комнате", "Разложить все вещи", Status.DONE, 0);
        manager.createTask(task1);

        Task task2 = new Task("Сходить в магазин", "Составить список", Status.NEW, 0);
        manager.createTask(task2);

        Epic epic1 = new Epic("Приготовить завтрак", "Завтра утром", 0);
        manager.createEpic(epic1);

        Subtask subtask1 = new Subtask("Достать продукты из холодильника",
                "Только нужные ингредиенты", Status.DONE, epic1.getId(), 0);
        manager.createSubtask(subtask1);

        Subtask subtask2 = new Subtask("Помыть посуду",
                "После завтрака", Status.IN_PROGRESS, epic1.getId(), 0);
        manager.createSubtask(subtask2);

        Epic epic2 = new Epic("Собраться на работу", "Встать пораньше", 0);
        manager.createEpic(epic2);

        Subtask subtask3 = new Subtask("Выбрать одежду по погоде",
                "Утром может быть прохладно", Status.NEW, epic2.getId(), 0);
        manager.createSubtask(subtask3);

        System.out.println(manager.getAllTasks());
        System.out.println(manager.getAllEpics());
        System.out.println(manager.getAllSubtasks());
    }
}