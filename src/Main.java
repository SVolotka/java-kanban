import manager.Managers;
import manager.TaskManager;
import models.Epic;
import models.Status;
import models.Subtask;
import models.Task;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = Managers.getDefault();

        // Testing
        // Создайте две задачи, эпик с тремя подзадачами и эпик без подзадач.
        Task firstTask = new Task("Сдать ФЗ4", "Финальное задание 4 спринта", Status.NEW);
        Task secondTask = new Task("Сдать ФЗ5", "Финальное задание 5 спринта", Status.NEW);
        manager.addTask(firstTask);
        manager.addTask(secondTask);

        Epic firstEpic = new Epic("Купить продукты", "Молоко и Хлеб", Status.NEW);
        Epic secondEpic = new Epic("Купить книгу", "Желательно научную фантастику", Status.NEW);
        manager.addEpic(firstEpic);
        manager.addEpic(secondEpic);

        Subtask firstSubtask = new Subtask("Выбрать молоко", "жирность 2.5%", Status.NEW, firstEpic.getID());
        Subtask secondSubtask = new Subtask("Выбрать хлеб", "цельнозерновой", Status.NEW, firstEpic.getID());
        Subtask thirdSubtask = new Subtask("Выбрать чай", "черный из китая", Status.NEW, firstEpic.getID());
        manager.addSubtask(firstSubtask);
        manager.addSubtask(secondSubtask);
        manager.addSubtask(thirdSubtask);

        // Запросите созданные задачи несколько раз в разном порядке.
        manager.getTaskByID(firstTask.getID());
        manager.getTaskByID(secondTask.getID());
        manager.getEpicByID(firstEpic.getID());
        manager.getEpicByID(secondEpic.getID());
        manager.getSubtaskByID(firstSubtask.getID());
        manager.getSubtaskByID(secondSubtask.getID());
        manager.getSubtaskByID(thirdSubtask.getID());

        // После каждого запроса выведите историю и убедитесь, что в ней нет повторов.
        List<Task> history = manager.getHistory();

        for (int i = 0; i < history.size(); i++) {
            System.out.println(i + ": " + history.get(i));
        }

        System.out.println("_".repeat(20));

        manager.getEpicByID(firstEpic.getID());
        manager.getEpicByID(secondEpic.getID());
        manager.getSubtaskByID(firstSubtask.getID());

        history = manager.getHistory();

        for (int i = 0; i < history.size(); i++) {
            System.out.println(i + ": " + history.get(i));
        }

        System.out.println("_".repeat(20));

        manager.getSubtaskByID(thirdSubtask.getID());
        manager.getSubtaskByID(secondSubtask.getID());
        manager.getTaskByID(firstTask.getID());
        manager.getTaskByID(secondTask.getID());
        manager.getEpicByID(firstEpic.getID());

        history = manager.getHistory();
        for (int i = 0; i < history.size(); i++) {
            System.out.println(i + ": " + history.get(i));
        }

        System.out.println("_".repeat(20));

        // Удалите задачу, которая есть в истории, и проверьте, что при печати она не будет выводиться.
        manager.deleteTaskByID(firstTask.getID());

        history = manager.getHistory();

        for (int i = 0; i < history.size(); i++) {
            System.out.println(i + ": " + history.get(i));
        }
        System.out.println("_".repeat(20));

        // Удалите эпик с тремя подзадачами и убедитесь, что из истории удалился как сам эпик, так и все его подзадачи.
        manager.deleteEpicByID(firstEpic.getID());

        history = manager.getHistory();

        for (int i = 0; i < history.size(); i++) {
            System.out.println(i + ": " + history.get(i));
        }
        System.out.println("_".repeat(20));
    }
}