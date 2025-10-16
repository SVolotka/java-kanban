package manager;

import exceptions.FileInitializationException;
import models.Epic;
import models.Status;
import models.Subtask;
import models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    File testFile;

    @BeforeEach
    void init() {
        taskManager = (FileBackedTaskManager) getTaskManager();
    }

    @Override
    TaskManager getTaskManager() {
      try {
          testFile = File.createTempFile("test_tasks", ".csv");
      } catch (IOException exc) {
          throw new FileInitializationException(exc.getMessage());
      }
        return new FileBackedTaskManager(testFile, new InMemoryHistoryManager());
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
        taskManager.addTask(new Task(nameTask, descriptionTask, status));
        int epicID = taskManager.addEpic(new Epic(nameEpic, descriptionEpic, status));
        taskManager.addSubtask(new Subtask(nameSubtask, descriptionSubtask, status, epicID));
        List<String> lines = Files.readAllLines(testFile.toPath());

        assertEquals(1, taskManager.getAllTasks().size(), "Неверное количество задач");
        assertEquals(1, taskManager.getAllEpics().size(), "Неверное количество эпиков");
        assertEquals(1, taskManager.getAllSubtasks().size(), "Неверное количество сабтасков");
        assertEquals(4, lines.size(), "Количество линий в файле не совпадают");
    }

    @Test
    void shouldLoadTasks() {
        taskManager.addTask(new Task(nameTask, descriptionTask, status));
        taskManager.addTask(new Task("Second Task", "Second Description", Status.IN_PROGRESS));
        int epicID = taskManager.addEpic(new Epic(nameEpic, descriptionEpic, status));
        taskManager.addSubtask(new Subtask(nameSubtask, descriptionSubtask, status, epicID));

        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);
        assertEquals(2, loadedManager.getAllTasks().size(), "Неверное количество задач");
        assertEquals(1, loadedManager.getAllEpics().size(), "Неверное количество эпиков");
        assertEquals(1, loadedManager.getAllSubtasks().size(), "Неверное количество сабтасков");
        assertEquals("Second Task", loadedManager.getTaskByID(2).getName(), "Имя задачи не совпадает");
    }

    @Test
    void shouldSaveChangesToFile() {
        int taskID = taskManager.addTask(new Task(nameTask, descriptionTask, Status.NEW));
        Task updatedTask = new Task(nameTask, descriptionTask, Status.IN_PROGRESS);

        updatedTask.setID(taskID);
        taskManager.updateTask(updatedTask);
        FileBackedTaskManager loadedManager = FileBackedTaskManager.loadFromFile(testFile);
        Task loadedTask = loadedManager.getTaskByID(taskID);

        assertNotNull(loadedManager, "Задача не загружена");
        assertEquals(Status.IN_PROGRESS, loadedTask.getStatus(), "Статусы не равны");
    }



    @Test
    void shouldCreateTaskWithTimeFromString() {
        String taskCsv = "1,TASK,Test Task,NEW,Description,01.01.25: 10:00,120,-";

        Task task = CSVTaskFormat.createTaskFromString(taskCsv);

        assertNotNull(task, "Задача не должна быть null");
        assertEquals("Test Task", task.getName());
        assertEquals("Description", task.getDescription());
        assertEquals(Status.NEW, task.getStatus());
        assertEquals(LocalDateTime.of(2025, 1, 1, 10, 0), task.getStartTime());
        assertEquals(Duration.ofMinutes(120), task.getDuration());
        assertEquals(LocalDateTime.of(2025, 1, 1, 12, 0), task.getEndTime());
    }

    @Test
    void shouldCreateEpicWithTimeFromString() {
        String epicCsv = "2,EPIC,Test Epic,NEW,Epic Description,01.01.25: 09:00,480,-";

        Task epic = CSVTaskFormat.createTaskFromString(epicCsv);

        assertNotNull(epic, "Эпик не должен быть null");
        assertEquals("Test Epic", epic.getName());
        assertEquals("Epic Description", epic.getDescription());
        assertEquals(Status.NEW, epic.getStatus());
        assertEquals(LocalDateTime.of(2025, 1, 1, 9, 0), epic.getStartTime());
        assertEquals(Duration.ofMinutes(480), epic.getDuration());
        assertInstanceOf(Epic.class, epic, "Должен быть экземпляром Epic");
    }

    @Test
    void shouldCreateSubtaskWithTimeFromString() {
        String subtaskCsv = "3,SUBTASK,Test Subtask,IN_PROGRESS,Subtask Description,01.01.25: 14:00,60,2";

        Task subtask = CSVTaskFormat.createTaskFromString(subtaskCsv);

        assertNotNull(subtask, "Подзадача не должна быть null");
        assertEquals("Test Subtask", subtask.getName());
        assertEquals("Subtask Description", subtask.getDescription());
        assertEquals(Status.IN_PROGRESS, subtask.getStatus());
        assertEquals(LocalDateTime.of(2025, 1, 1, 14, 0), subtask.getStartTime());
        assertEquals(Duration.ofMinutes(60), subtask.getDuration());
        assertEquals(LocalDateTime.of(2025, 1, 1, 15, 0), subtask.getEndTime());
        assertInstanceOf(Subtask.class, subtask, "Должен быть экземпляром Subtask");
        assertEquals(2, ((Subtask) subtask).getEpicID(), "EpicID должен совпадать");
    }

    @Test
    void shouldCreateTaskWithoutTimeFromString() {
        String taskCsv = "4,TASK,No Time Task,DONE,No Time Description,-,-,-";

        Task task = CSVTaskFormat.createTaskFromString(taskCsv);

        assertNotNull(task, "Задача не должна быть null");
        assertEquals("No Time Task", task.getName());
        assertEquals("No Time Description", task.getDescription());
        assertEquals(Status.DONE, task.getStatus());
        assertNull(task.getStartTime(), "StartTime должен быть null");
        assertNull(task.getDuration(), "Duration должен быть null");
        assertNull(task.getEndTime(), "EndTime должен быть null");
    }

    @Test
    void shouldCreateTaskWithDifferentStatusesFromString() {
        String newTaskCsv = "5,TASK,New Task,NEW,Description,-,-,-";
        String inProgressTaskCsv = "6,TASK,In Progress Task,IN_PROGRESS,Description,-,-,-";
        String doneTaskCsv = "7,TASK,Done Task,DONE,Description,-,-,-";

        Task newTask = CSVTaskFormat.createTaskFromString(newTaskCsv);
        Task inProgressTask = CSVTaskFormat.createTaskFromString(inProgressTaskCsv);
        Task doneTask = CSVTaskFormat.createTaskFromString(doneTaskCsv);

        assertEquals(Status.NEW, newTask.getStatus());
        assertEquals(Status.IN_PROGRESS, inProgressTask.getStatus());
        assertEquals(Status.DONE, doneTask.getStatus());
    }

    @Test
    void shouldCreateTaskWithPartialTimeFromString() {
        String onlyStartTimeCsv = "8,TASK,Only Start Task,NEW,Description,01.01.25: 12:00,-,-";
        String onlyDurationCsv = "9,TASK,Only Duration Task,NEW,Description,-,90,-";

        Task onlyStartTask = CSVTaskFormat.createTaskFromString(onlyStartTimeCsv);
        Task onlyDurationTask = CSVTaskFormat.createTaskFromString(onlyDurationCsv);

        assertNotNull(onlyStartTask.getStartTime(), "StartTime должен быть установлен");
        assertNull(onlyStartTask.getDuration(), "Duration должен быть null");
        assertNull(onlyStartTask.getEndTime(), "EndTime должен быть null");

        assertNull(onlyDurationTask.getStartTime(), "StartTime должен быть null");
        assertNotNull(onlyDurationTask.getDuration(), "Duration должен быть установлен");
        assertNull(onlyDurationTask.getEndTime(), "EndTime должен быть null");
    }
}