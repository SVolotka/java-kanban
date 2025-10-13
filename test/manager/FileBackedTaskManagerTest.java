package manager;

import models.Epic;
import models.Status;
import models.Subtask;
import models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileBackedTaskManagerTest {
    FileBackedTaskManager manager;
    File testFile;

    String nameTask = "Test addNewTask";
    String descriptionTask = "Test addNewTask description";
    Status status = Status.NEW;

    String nameEpic = "Test addNewEpic";
    String descriptionEpic = "Test addNewEpic description";

    String nameSubtask = "Test addNewSubtask";
    String descriptionSubtask = "Test addNewSubtask description";

    @BeforeEach
    void init() throws IOException {
        testFile = File.createTempFile("test_tasks", ".csv");
        manager = new FileBackedTaskManager(new InMemoryHistoryManager(), testFile);
    }

    @Test
    void shouldSaveAndLoadEmptyFile() {
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);

        assertTrue(loadedManager.getAllTasks().isEmpty(), "Список задач должен быть пустой");
        assertTrue(loadedManager.getAllEpics().isEmpty(), "Список эпиков должен быть пустой");
        assertTrue(loadedManager.getAllSubtasks().isEmpty(), "Список сабтасков должен быть пустой");

        assertTrue(testFile.exists(), "Файл не найден");
    }

    @Test
    void shouldAddTasks() throws IOException {
        manager.addTask(new Task(nameTask, descriptionTask, status));
        int epicID = manager.addEpic(new Epic(nameEpic, descriptionEpic, status));
        manager.addSubtask(new Subtask(nameSubtask, descriptionSubtask, status, epicID));
        List<String> lines = Files.readAllLines(testFile.toPath());

        assertEquals(1, manager.getAllTasks().size(), "Неверное количество задач");
        assertEquals(1, manager.getAllEpics().size(), "Неверное количество эпиков");
        assertEquals(1, manager.getAllSubtasks().size(), "Неверное количество сабтасков");
        assertEquals(4, lines.size(), "Количество линий в файле не совпадают");
    }

    @Test
    void shouldLoadTasks() {
        manager.addTask(new Task(nameTask, descriptionTask, status));
        manager.addTask(new Task("Second Task", "Second Description", Status.IN_PROGRESS));
        int epicID = manager.addEpic(new Epic(nameEpic, descriptionEpic, status));
        manager.addSubtask(new Subtask(nameSubtask, descriptionSubtask, status, epicID));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);
        assertEquals(2, loadedManager.getAllTasks().size(), "Неверное количество задач");
        assertEquals(1, loadedManager.getAllEpics().size(), "Неверное количество эпиков");
        assertEquals(1, loadedManager.getAllSubtasks().size(), "Неверное количество сабтасков");
        assertEquals("Second Task", loadedManager.getTaskByID(2).getName(), "Имя задачи не совпадает");
    }

    @Test
    void shouldSaveChangesToFile() {
        int taskID = manager.addTask(new Task(nameTask, descriptionTask, Status.NEW));
        Task updatedTask = new Task(nameTask, descriptionTask, Status.IN_PROGRESS);

        updatedTask.setID(taskID);
        manager.updateTask(updatedTask);
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);
        Task loadedTask = loadedManager.getTaskByID(taskID);

        assertNotNull(loadedManager, "Задача не загружена");
        assertEquals(Status.IN_PROGRESS, loadedTask.getStatus(), "Статусы не равны");
    }

    @Test
    void shouldSaveDeletionsToFile() {
        int firstID = manager.addTask(new Task(nameTask, descriptionTask, status));
        int secondID = manager.addTask(new Task("Second Task", "Second Description", Status.IN_PROGRESS));
        int numberOfTasksBeforeDeletions = manager.tasks.size();

        manager.deleteTaskByID(firstID);
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);
        int numberOfTasksAfterDeletions = loadedManager.tasks.size();

        assertEquals(2, numberOfTasksBeforeDeletions, "Неверное количество задач");
        assertEquals(1, numberOfTasksAfterDeletions, "Неверное количество задач. Задача не удалилась");
        assertNull(loadedManager.getTaskByID(firstID), "Задача не удалилась");
        assertEquals(manager.getTaskByID(secondID), loadedManager.getTaskByID(secondID), "Задачи не равны");
    }
}