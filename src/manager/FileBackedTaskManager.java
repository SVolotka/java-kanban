package manager;

import exceptions.FileInitializationException;
import exceptions.ManagerSaveException;
import models.Epic;
import models.Status;
import models.Subtask;
import models.Task;
import models.TaskType;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final File file;

    public FileBackedTaskManager(File file, HistoryManager historyManager) {
        super(historyManager);
        this.file = file;
    }

    public static void main(String[] args) throws IOException {
        File testFile = java.io.File.createTempFile("test_tasks", ".csv");
        FileBackedTaskManager manager = new FileBackedTaskManager(testFile, new InMemoryHistoryManager());

        // Заведите несколько разных задач, эпиков и подзадач.
        Task firstTask = new Task("Сдать ФЗ4", "Финальное задание 4 спринта", Status.NEW);
        Task secondTask = new Task("Сдать ФЗ5", "Финальное задание 5 спринта", Status.NEW);
        int firstTaskID = manager.addTask(firstTask);
        int secondTaskID = manager.addTask(secondTask);

        Epic firstEpic = new Epic("Купить продукты", "Молоко и Хлеб", Status.NEW);
        Epic secondEpic = new Epic("Купить книгу", "Желательно научную фантастику", Status.NEW);
        int firstEpicID = manager.addEpic(firstEpic);
        manager.addEpic(secondEpic);

        Subtask firstSubtask = new Subtask("Выбрать молоко", "жирность 2.5%", Status.NEW, firstEpic.getID());
        Subtask secondSubtask = new Subtask("Выбрать хлеб", "цельнозерновой", Status.NEW, firstEpic.getID());
        Subtask thirdSubtask = new Subtask("Выбрать чай", "черный из китая", Status.NEW, firstEpic.getID());
        int firstSubtaskID = manager.addSubtask(firstSubtask);
        manager.addSubtask(secondSubtask);
        manager.addSubtask(thirdSubtask);

        // Создайте новый FileBackedTaskManager-менеджер из этого же файла.
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        // Проверьте, что все задачи, эпики, подзадачи, которые были в старом менеджере, есть в новом.
        int numberOfTasksInManager = manager.tasks.size();
        int numberOfEpicsInManager = manager.epics.size();
        int numberOfSubtasksInManager = manager.subtasks.size();

        int numberOfTasksInLoadedManager = loadedManager.tasks.size();
        int numberOfEpicsInLoadedManager = loadedManager.epics.size();
        int numberOfSubtasksInLoadedManager = loadedManager.subtasks.size();

        System.out.println(numberOfTasksInManager + " = " + numberOfTasksInLoadedManager + " -> " +
                (numberOfTasksInManager == numberOfTasksInLoadedManager));
        System.out.println(numberOfEpicsInManager + " = " + numberOfEpicsInLoadedManager + " -> " +
                (numberOfEpicsInManager == numberOfEpicsInLoadedManager));
        System.out.println(numberOfSubtasksInManager + " = " + numberOfSubtasksInLoadedManager + " -> " +
                (numberOfSubtasksInManager == numberOfSubtasksInLoadedManager));

        System.out.println("-".repeat(20));

        System.out.println("true -> " + manager.tasks.get(firstTaskID).equals(loadedManager.tasks.get(firstTaskID)));
        System.out.println("true -> " + manager.epics.get(firstEpicID).equals(loadedManager.epics.get(firstEpicID)));
        System.out.println("true -> " + manager.subtasks.get(firstSubtaskID).equals(loadedManager.subtasks.get(firstSubtaskID)));
        System.out.println("false -> " + manager.tasks.get(firstTaskID).equals(loadedManager.tasks.get(secondTaskID)));
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        try {
            FileBackedTaskManager fileBackedTaskManager = new FileBackedTaskManager(file, new InMemoryHistoryManager());
            int maxID = 0;

            if (!file.exists() || file.length() == 0) {
                return fileBackedTaskManager;
            }

            List<String> allLines = Files.readAllLines(file.toPath());

            for (int i = 1; i < allLines.size(); i++) {
                String line = allLines.get(i);
                if (line.trim().isEmpty()) continue;

                int id = fileBackedTaskManager.setIDFromString(allLines.get(i));
                if (id > maxID) {
                    maxID = id;
                }
                Task currentTask = CSVTaskFormat.createTaskFromString(allLines.get(i));
                TaskType taskType = CSVTaskFormat.determineTaskType(currentTask);
                switch (taskType) {
                    case TASK -> fileBackedTaskManager.addTaskWithoutSave(currentTask);
                    case EPIC -> fileBackedTaskManager.addEpicWithoutSave((Epic) currentTask);
                    case SUBTASK -> fileBackedTaskManager.addSubtaskWithoutSave((Subtask) currentTask);
                }
            }
            fileBackedTaskManager.setID(maxID + 1);

            return fileBackedTaskManager;
        } catch (IOException exc) {
            throw new FileInitializationException("Ошибка загрузки файла: " + exc.getMessage());
        }
    }

    private void save() {
        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
            bufferedWriter.write(CSVTaskFormat.getHeader());
            bufferedWriter.newLine();

            for (Task task : tasks.values()) {
                bufferedWriter.write(taskToString(task));
                bufferedWriter.newLine();
            }

            for (Epic epic : epics.values()) {
                bufferedWriter.write(taskToString(epic));
                bufferedWriter.newLine();
            }

            for (Subtask subtask : subtasks.values()) {
                bufferedWriter.write(taskToString(subtask));
                bufferedWriter.newLine();
            }
        } catch (IOException exc) {
            throw new ManagerSaveException("Ошибка при сохранении в файл: " + exc.getMessage());
        }
    }

    private String taskToString(Task task) {
        return CSVTaskFormat.createStringFromTask(task);
    }

    @Override
    public int addTask(Task task) {
        int id = super.addTask(task);
        save();
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        int id = super.addEpic(epic);
        save();
        return id;
    }

    @Override
    public Integer addSubtask(Subtask subtask) {
        int id = super.addSubtask(subtask);
        save();
        return id;
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

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
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
    public Task deleteTaskByID(int id) {
        Task task = super.deleteTaskByID(id);
        save();
        return task;
    }

    @Override
    public Epic deleteEpicByID(int id) {
        Epic epic = super.deleteEpicByID(id);
        save();
        return epic;
    }

    @Override
    public Subtask deleteSubtaskByID(int id) {
        Subtask subtask = super.deleteSubtaskByID(id);
        save();
        return subtask;
    }

    public void addTaskWithoutSave(Task task) {
        super.addTask(task);
    }

    public void addEpicWithoutSave(Epic epic) {
        super.addEpic(epic);
    }

    public void addSubtaskWithoutSave(Subtask subtask) {
        super.addSubtask(subtask);
    }

    private int setIDFromString(String csvLine) {
        String[] fields = csvLine.split(",");
        int id = Integer.parseInt(fields[0]);
        super.updateIdentifier(id);
        return id;
    }

    private void setID(int id) {
        super.updateIdentifier(id);
    }
}