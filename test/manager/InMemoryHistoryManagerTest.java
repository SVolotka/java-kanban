package manager;

import models.Status;
import models.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    InMemoryHistoryManager historyManager;
    Task firstTask;
    Task secondTask;
    Task thirdTask;


    @BeforeEach
    public void init() {
        historyManager = new InMemoryHistoryManager();
        firstTask = new Task("Task 1", "Description 1", Status.NEW);
        secondTask = new Task("Task 2", "Description 2", Status.IN_PROGRESS);
        thirdTask = new Task("Task 3", "Description 3", Status.NEW);
        firstTask.setID(1);
        secondTask.setID(2);
        thirdTask.setID(3);
    }

    @Test
    void shouldAddTask() {
        historyManager.add(firstTask);

        List<Task> history = historyManager.getHistory();
        Task taskInHistory = history.getFirst();
        int historyListSize = history.size();

        Assertions.assertNotNull(taskInHistory, "Task возвращает null");
        Assertions.assertEquals(1, historyListSize, "Количество задач не совпадают");
        Assertions.assertEquals(firstTask, taskInHistory, "Таски не равны");
    }

    @Test
    void shouldNotAddNullTask() {
        historyManager.add(null);

        List<Task> history = historyManager.getHistory();

        Assertions.assertTrue(history.isEmpty(), "Список не пуст");
    }

    @Test
    void shouldReturnTasksInCorrectOrder() {
        historyManager.add(firstTask);
        historyManager.add(secondTask);
        historyManager.add(thirdTask);

        List<Task> history = historyManager.getHistory();
        int historyListSize = history.size();
        Task firstTaskInHistory = history.getFirst();
        Task secondTaskInHistory = history.get(1);
        Task thirdTaskInHistory = history.getLast();

        Assertions.assertEquals(3, historyListSize, "Количество задач не совпадают");
        Assertions.assertEquals(firstTask, firstTaskInHistory, "Таски не равны");
        Assertions.assertEquals(secondTask, secondTaskInHistory, "Таски не равны");
        Assertions.assertEquals(thirdTask, thirdTaskInHistory, "Таски не равны");
    }

    @Test
    void shouldMoveDuplicateToTheEnd() {
        historyManager.add(firstTask);
        historyManager.add(secondTask);
        historyManager.add(firstTask);

        List<Task> history = historyManager.getHistory();
        int historyListSize = history.size();
        Task firstTaskInHistory = history.getFirst();
        Task lastTaskInHistory = history.getLast();

        Assertions.assertEquals(2, historyListSize, "Количество задач не совпадают");
        Assertions.assertEquals(secondTask, firstTaskInHistory, "Таски не равны");
        Assertions.assertEquals(firstTask, lastTaskInHistory, "Таски не равны");
    }

    @Test
    void shouldRemoveTaskFromHistory() {
        historyManager.add(firstTask);
        historyManager.add(secondTask);

        historyManager.remove(firstTask.getID());
        List<Task> history = historyManager.getHistory();
        int historyListSize = history.size();
        Task firstTaskInHistory = history.getFirst();

        Assertions.assertEquals(1, historyListSize, "Количество задач не совпадают");
        Assertions.assertEquals(secondTask, firstTaskInHistory, "Таски не равны");
    }
}