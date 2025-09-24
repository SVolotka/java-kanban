package manager;

import models.Epic;
import models.Status;
import models.Subtask;
import models.Task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class InMemoryTaskManagerTest {
    private TaskManager taskManager;
    String nameTask = "Test addNewTask";
    String descriptionTask = "Test addNewTask description";
    Status status = Status.NEW;

    String nameEpic = "Test addNewEpic";
    String descriptionEpic = "Test addNewEpic description";

    String nameSubtask = "Test addNewSubtask";
    String descriptionSubtask = "Test addNewSubtask description";

    @BeforeEach
    public void init() {
        taskManager = Managers.getDefault();
    }

    @Test
    void shouldAddTaskManager() {
        TaskManager secondTaskManager = Managers.getDefault();
        HistoryManager secondHistoryManger = Managers.getDefaultHistory();

        assertNotNull(taskManager);
        assertNotNull(secondTaskManager);
        assertNotNull(secondHistoryManger);
    }

    @Test
    void shouldAddTask() {
        Task task = new Task(nameTask, descriptionTask, status);

        final int taskID = taskManager.addTask(task);
        final Task savedTask = taskManager.getTaskByID(taskID);

        assertNotNull(savedTask, "Задача не найдена.");
        assertEquals(task, savedTask, "Задачи не совпадают.");
        assertEquals(task.getName(), savedTask.getName(), "name не совпадают.");
        assertEquals(task.getDescription(), savedTask.getDescription(), "description не совпадают.");
        assertEquals(task.getStatus(), savedTask.getStatus(), "status не совпадают.");
    }

    @Test
    void shouldAddEpic() {
        Epic epic = new Epic(nameEpic, descriptionEpic, status);

        final int epicID = taskManager.addEpic(epic);
        final Epic savedEpic = taskManager.getEpicByID(epicID);

        assertNotNull(savedEpic, "Эпик не найдена.");
        assertEquals(epic, savedEpic, "Эпики не совпадают.");
        assertEquals(epic.getName(), savedEpic.getName(), "name не совпадают.");
        assertEquals(epic.getDescription(), savedEpic.getDescription(), "description не совпадают.");
        assertEquals(epic.getStatus(), savedEpic.getStatus(), "status не совпадают");
    }

    @Test
    void shouldAddSubtask() {
        Epic epic = new Epic(nameEpic, descriptionEpic, status);
        final int epicID = taskManager.addEpic(epic);
        Subtask subtask = new Subtask(nameSubtask, descriptionSubtask, status, epicID);

        final int subtaskId = taskManager.addSubtask(subtask);
        final Subtask savedSubtask = taskManager.getSubtaskByID(subtaskId);

        assertNotNull(savedSubtask, "Cабтаск не найдена.");
        assertEquals(subtask, savedSubtask, "Сабтаски не совпадают.");
        assertEquals(subtask.getName(), savedSubtask.getName(), "name не совпадают");
        assertEquals(subtask.getDescription(), savedSubtask.getDescription(), "description не совпадают.");
        assertEquals(subtask.getStatus(), savedSubtask.getStatus());
    }

    @Test
    void shouldBeEqualsTasksIfIdIsSame() {
        Task task = new Task(nameTask, descriptionTask, status);
        final int taskID = taskManager.addTask(task);

        final Task firstTask = taskManager.getTaskByID(taskID);
        final Task secondTask = taskManager.getTaskByID(taskID);

        assertEquals(firstTask, secondTask, "Таски не равны");
    }

    @Test
    void shouldBeEqualsEpicsIfIdIsSame() {
        Epic epic = new Epic(nameEpic, descriptionEpic, status);
        final int epicID = taskManager.addEpic(epic);

        final Epic firstEpic = taskManager.getEpicByID(epicID);
        final Epic secondEpic = taskManager.getEpicByID(epicID);

        assertEquals(firstEpic, secondEpic, "Эпики не равны");
    }

    @Test
    void shouldBeEqualsSubtasksIfIdIsSame() {
        Epic epic = new Epic(nameEpic, descriptionEpic, status);
        final int epicID = taskManager.addEpic(epic);
        Subtask subtask = new Subtask(nameSubtask, descriptionSubtask, status, epicID);
        final int subtaskId = taskManager.addSubtask(subtask);

        final Subtask firstSubtask = taskManager.getSubtaskByID(subtaskId);
        final Subtask secondSubtask = taskManager.getSubtaskByID(subtaskId);

        assertEquals(firstSubtask, secondSubtask, "Сабтаски не равны");
    }

    @Test
    void shouldNotAddSubtaskIdAsEpic() {
        Epic epic = new Epic(nameEpic, descriptionEpic, status);
        final int epicID = taskManager.addEpic(epic);
        Subtask subtask = new Subtask(nameSubtask, descriptionSubtask, status, epicID);
        final int subtaskId = taskManager.addSubtask(subtask);

        Subtask checkingSubtask = new Subtask(nameSubtask, descriptionSubtask, status, subtaskId);
        final Integer checkingSubtaskID = taskManager.addSubtask(checkingSubtask);

        assertNull(checkingSubtaskID);
    }

    @Test
    void shouldGetAllTasks() {
        Task task = new Task(nameTask, descriptionTask, status);
        taskManager.addTask(task);

        final List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task, tasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void shouldGetAllEpics() {
        Epic epic = new Epic(nameEpic, descriptionEpic, status);
        taskManager.addEpic(epic);
        final List<Epic> epics = taskManager.getAllEpics();

        assertNotNull(epics, "Задачи не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество задач.");
        assertEquals(epic, epics.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void shouldGetAllSubtasks() {
        Epic epic = new Epic(nameEpic, descriptionEpic, status);
        final int epicID = taskManager.addEpic(epic);
        Subtask subtask = new Subtask(nameSubtask, descriptionSubtask, status, epicID);
        taskManager.addSubtask(subtask);

        final List<Subtask> subtasks = taskManager.getAllSubtasks();

        assertNotNull(subtasks, "Задачи не возвращаются.");
        assertEquals(1, subtasks.size(), "Неверное количество задач.");
        assertEquals(subtask, subtasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void shouldDeleteAllTasks() {
        Task task = new Task(nameTask, descriptionTask, status);
        taskManager.addTask(task);

        taskManager.deleteAllTasks();
        List<Task> tasks = taskManager.getAllTasks();
        int tasksSize = tasks.size();

        assertEquals(0, tasksSize, "Список задач не обнулился.");
    }

    @Test
    void shouldDeleteAllEpics() {
        Epic epic = new Epic(nameEpic, descriptionEpic, status);
        taskManager.addEpic(epic);

        taskManager.deleteAllEpics();
        List<Epic> epics = taskManager.getAllEpics();
        int epicsSize = epics.size();

        assertEquals(0, epicsSize, "Список эпиков не обнулился.");
    }

    @Test
    void shouldDeleteAllSubtasks() {
        Epic epic = new Epic(nameEpic, descriptionEpic, status);
        final int epicId = taskManager.addEpic(epic);
        Subtask subtask = new Subtask(nameSubtask, descriptionSubtask, status, epicId);
        taskManager.addSubtask(subtask);

        taskManager.deleteAllSubtasks();
        List<Subtask> subtasks = taskManager.getAllSubtasks();
        int subtasksSize = subtasks.size();

        assertEquals(0, subtasksSize, "Список сабтасков не обнулился.");
    }

    @Test
    void shouldGetTaskByID() {
        Task task = new Task(nameTask, descriptionTask, status);
        final int taskID = taskManager.addTask(task);

        Task taskByID = taskManager.getTaskByID(taskID);

        assertNotNull(taskByID, "Задача не возвращается");
    }

    @Test
    void shouldGetEpicByID() {
        Epic epic = new Epic(nameEpic, descriptionEpic, status);
        final int epicID = taskManager.addEpic(epic);

        Epic epicByID = taskManager.getEpicByID(epicID);

        assertNotNull(epicByID, "Эпик не возвращается");
    }

    @Test
    void shouldGetSubtaskByID() {
        Epic epic = new Epic(nameEpic, descriptionEpic, status);
        final int epicID = taskManager.addEpic(epic);
        Subtask subtask = new Subtask(nameSubtask, descriptionSubtask, status, epicID);
        final int subtaskID = taskManager.addSubtask(subtask);

        Subtask subtaskByID = taskManager.getSubtaskByID(subtaskID);

        assertNotNull(subtaskByID, "Сабтаск не возвращается");
    }

    @Test
    void shouldUpdateTask() {
        Task task = new Task(nameTask, descriptionTask, status);
        final int taskID = taskManager.addTask(task);
        Task taskBeforeUpdate = taskManager.getTaskByID(taskID);
        Status statusBeforeUpdate = taskBeforeUpdate.getStatus();

        taskBeforeUpdate.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(taskBeforeUpdate);
        Task taskAfterUpdate = taskManager.getTaskByID(taskID);
        Status statusAfterUpdate = taskAfterUpdate.getStatus();

        assertNotEquals(statusBeforeUpdate, statusAfterUpdate, "Статусы равны.");
    }

    @Test
    void shouldUpdateEpic() {
        Epic epic = new Epic(nameEpic, descriptionEpic, status);
        final int epicID = taskManager.addEpic(epic);
        Epic epicBeforeUpdate = taskManager.getEpicByID(epicID);
        Status statusBeforeUpdate = epicBeforeUpdate.getStatus();

        epicBeforeUpdate.setStatus(Status.IN_PROGRESS);
        taskManager.updateEpic(epicBeforeUpdate);
        Epic epicAfterUpdate = taskManager.getEpicByID(epicID);
        Status statusAfterUpdate = epicAfterUpdate.getStatus();

        assertNotEquals(statusBeforeUpdate, statusAfterUpdate, "Статусы равны.");
    }

    @Test
    void shouldUpdateSubtask() {
        Epic epic = new Epic(nameEpic, descriptionEpic, status);
        final int epicID = taskManager.addEpic(epic);
        Subtask subtask = new Subtask(nameSubtask, descriptionSubtask, status, epicID);
        final int subtaskID = taskManager.addSubtask(subtask);
        Subtask subtaskBeforeUpdate = taskManager.getSubtaskByID(subtaskID);
        Status statusBeforeUpdate = subtaskBeforeUpdate.getStatus();

        subtaskBeforeUpdate.setStatus(Status.IN_PROGRESS);
        taskManager.updateSubtask(subtaskBeforeUpdate);
        Subtask subtaskAfterUpdate = taskManager.getSubtaskByID(subtaskID);
        Status statusAfterUpdate = subtaskAfterUpdate.getStatus();

        assertNotEquals(statusBeforeUpdate, statusAfterUpdate, "Статусы равны.");
    }

    @Test
    void shouldDeleteTaskByID() {
        Task task = new Task(nameTask, descriptionTask, status);
        final int taskID = taskManager.addTask(task);

        taskManager.deleteTaskByID(taskID);
        Task deletedTask = taskManager.getTaskByID(taskID);

        assertNull(deletedTask, "Задача не удалилась.");
    }

    @Test
    void shouldDeleteEpicByID() {
        Epic epic = new Epic(nameEpic, descriptionEpic, status);
        final int epicID = taskManager.addEpic(epic);

        taskManager.deleteEpicByID(epicID);
        Epic deletedEpic = taskManager.deleteEpicByID(epicID);

        assertNull(deletedEpic, "Эпик не удалился.");
    }

    @Test
    void shouldDeleteSubtaskByID() {
        Epic epic = new Epic(nameEpic, descriptionEpic, status);
        final int epicID = taskManager.addEpic(epic);
        Subtask subtask = new Subtask(nameSubtask, descriptionSubtask, status, epicID);
        final int subtaskID = taskManager.addSubtask(subtask);

        taskManager.deleteSubtaskByID(subtaskID);
        Subtask deletedSubtask = taskManager.getSubtaskByID(subtaskID);

        assertNull(deletedSubtask, "Сабтаск не удалился.");
    }

    @Test
    void shouldRemoveSubtaskFromEpicWhenSubtaskIsDeleted() {
        Epic epic = new Epic(nameEpic, descriptionEpic, status);
        final int epicID = taskManager.addEpic(epic);
        Subtask firstSubtask = new Subtask(nameSubtask, descriptionSubtask, status, epicID);
        final int firstSubtaskID = taskManager.addSubtask(firstSubtask);
        Subtask secondSubtask = new Subtask(nameSubtask + 2, descriptionSubtask + 2, status, epicID);
        final int secondSubtaskID = taskManager.addSubtask(secondSubtask);

        taskManager.deleteSubtaskByID(firstSubtaskID);
        ArrayList<Integer> subtasksInEpic = epic.getSubtaskIDs();
        boolean isFirstSubtaskInList = subtasksInEpic.contains(firstSubtaskID);
        boolean isSecondSubtaskInList = subtasksInEpic.contains(secondSubtaskID);
        Subtask deletedSubtask = taskManager.getSubtaskByID(firstSubtaskID);

        assertNull(deletedSubtask, "Сабтаск не удалился.");
        assertTrue(isSecondSubtaskInList, "Сабтаска нет в списке у Эпика");
        assertFalse(isFirstSubtaskInList, "Сабтаск не удалился из списка Эпика");
    }

    @Test
    void getSubtasksByEpic() {
        Epic epic = new Epic(nameEpic, descriptionEpic, status);
        final int epicID = taskManager.addEpic(epic);
        Subtask firstSubtask = new Subtask(nameSubtask, descriptionSubtask, status, epicID);
        taskManager.addSubtask(firstSubtask);
        Subtask secondSubtask = new Subtask("Test addSecondSubtask",
                "Test addSecondSubtask description", status, epicID);
        taskManager.addSubtask(secondSubtask);

        ArrayList<Subtask> subtasksByEpic = taskManager.getSubtasksByEpic(epicID);
        int subtasksListSize = subtasksByEpic.size();

        assertNotNull(subtasksByEpic, "Сабтаски не возвращается");
        assertEquals(2, subtasksListSize, "Неверное количество сабтасков.");
    }

    @Test
    void getHistory() {
        Task task = new Task(nameTask, descriptionTask, status);
        final int taskID = taskManager.addTask(task);
        taskManager.getTaskByID(taskID);
        Epic epic = new Epic(nameEpic, descriptionEpic, status);
        final int epicID = taskManager.addEpic(epic);
        taskManager.getEpicByID(epicID);

        List<Task> history = taskManager.getHistory();
        Task taskInHistory = history.getFirst();
        Epic epicInHistory = (Epic) history.getLast();
        int historyListSize = history.size();

        assertNotNull(taskInHistory, "Таск не возвращается");
        assertNotNull(epicInHistory, "Эпик не возвращается");
        assertEquals(2, historyListSize, "Неверное количество задач.");
    }

    @Test
    void taskInHistoryListShouldNotBeUpdateAfterTaskUpdate() {
        Task task = new Task(nameTask, descriptionTask, status);
        taskManager.addTask(task);
        taskManager.getTaskByID(task.getID());
        Task taskInHistoryBeforeUpdate = taskManager.getHistory().getFirst();
        Status statusInHistoryBeforeUpdate = taskInHistoryBeforeUpdate.getStatus();

        task.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task);
        Task taskInHistoryAfterUpdate = taskManager.getHistory().getFirst();
        Status statusInHistoryAfterUpdate = taskInHistoryAfterUpdate.getStatus();

        assertEquals(statusInHistoryAfterUpdate, statusInHistoryBeforeUpdate, "Статусы не равны.");
    }
}
