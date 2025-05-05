import manager.TaskManager;
import models.Epic;
import models.Status;
import models.Subtask;
import models.Task;

import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {
        TaskManager manager = new TaskManager();

        // Testing
        //Создайте две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей.
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
        Subtask thirdSubtask = new Subtask("Сходить в Читай-город", "выбрать книгу", Status.NEW, secondEpic.getID());
        manager.addSubtask(firstSubtask);
        manager.addSubtask(secondSubtask);
        manager.addSubtask(thirdSubtask);

        //Распечатайте списки эпиков, задач и подзадач через System.out.println(..).
        ArrayList<Task> tasks = manager.getAllTasks();
        ArrayList<Epic> epics = manager.getAllEpics();
        ArrayList<Subtask> subtasks = manager.getAllSubtasks();

        System.out.println("Таски:");
        System.out.println(tasks);
        System.out.println("Эпики");
        System.out.println(epics);
        System.out.println("Подзадачи");
        System.out.println(subtasks);

        System.out.println("-".repeat(20));

        //Измените статусы созданных объектов, распечатайте их. Проверьте, что статус задачи и подзадачи сохранился,
        // а статус эпика рассчитался по статусам подзадач.
        firstTask.setStatus(Status.IN_PROGRESS);
        secondTask.setStatus(Status.DONE);
        firstSubtask.setStatus(Status.IN_PROGRESS);
        secondSubtask.setStatus(Status.DONE);
        thirdSubtask.setStatus(Status.DONE);

        manager.updateTask(firstTask);
        manager.updateTask(secondTask);
        manager.updateSubtask(firstSubtask);
        manager.updateSubtask(secondSubtask);
        manager.updateSubtask(thirdSubtask);

        System.out.println("Таски:");
        System.out.println(tasks);
        System.out.println("Эпики");
        System.out.println(epics);
        System.out.println("Подзадачи");
        System.out.println(subtasks);

        System.out.println("-".repeat(20));

        //И, наконец, попробуйте удалить одну из задач и один из эпиков.
        manager.deleteTaskByID(firstTask.getID());
        manager.deleteEpicByID(secondEpic.getID());

        epics = manager.getAllEpics();

        System.out.println("Таски:");
        System.out.println(manager.getAllTasks());
        System.out.println("Эпики");
        System.out.println(epics);
        System.out.println("Подзадачи");
        System.out.println(manager.getAllSubtasks());

        manager.deleteAllTasks();
        System.out.println(manager.getAllTasks());
    }
}