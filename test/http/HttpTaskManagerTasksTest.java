package http;

import com.google.gson.Gson;
import manager.Managers;
import manager.TaskManager;
import models.Epic;
import models.Status;
import models.Subtask;
import models.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class HttpTaskManagerTasksTest {

    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = Managers.getDefault();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = HttpTaskServer.getGson();

    public HttpTaskManagerTasksTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteAllTasks();
        manager.deleteAllSubtasks();
        manager.deleteAllEpics();
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testAddTask() throws IOException, InterruptedException {
        // создаём задачу
        Task task = new Task("Test", "Testing task",
                Status.NEW);
        task.setDuration(Duration.ofMinutes(5));
        task.setStartTime(LocalDateTime.now());
        // конвертируем её в JSON
        String taskJson = gson.toJson(task);

        // создаём HTTP-клиент и запрос
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        // вызываем рест, отвечающий за создание задач
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        // проверяем код ответа
        assertEquals(201, response.statusCode());

        // проверяем, что создалась одна задача с корректным именем
        List<Task> tasksFromManager = manager.getAllTasks();

        assertNotNull(tasksFromManager, "Задачи не возвращаются");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test", tasksFromManager.getFirst().getName(), "Некорректное имя задачи");
    }


    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Test", "Testing epic", Status.NEW);
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = manager.getAllEpics();

        assertNotNull(epicsFromManager, "Задачи не возвращаются");
        assertEquals(1, epicsFromManager.size(), "Некорректное количество задач");
        assertEquals("Test", epicsFromManager.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {

        Epic epic = new Epic("Epic", "For testing subtask", Status.NEW);
        int epicID = manager.addEpic(epic);
        Subtask subtask = new Subtask("Test", "Testing subtask",
                Status.NEW, epicID);
        subtask.setDuration(Duration.ofMinutes(5));
        subtask.setStartTime(LocalDateTime.now());
        String subtaskJson = gson.toJson(subtask);


        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = manager.getAllSubtasks();

        assertNotNull(subtasksFromManager, "Сабтаски не возвращаются");
        assertEquals(1, subtasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Test", subtasksFromManager.getFirst().getName(), "Некорректное имя задачи");
    }

    @Test
    public void testGetAllTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", Status.IN_PROGRESS);
        manager.addTask(task1);
        manager.addTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] tasks = gson.fromJson(response.body(), Task[].class);
        assertNotNull(tasks, "Задачи не возвращаются");
        assertEquals(2, tasks.length, "Некорректное количество задач");
        assertEquals("Task 1", tasks[0].getName(), "Некорректное имя первой задачи");
        assertEquals("Task 2", tasks[1].getName(), "Некорректное имя второй задачи");
    }

    @Test
    public void testGetTaskById() throws IOException, InterruptedException {
        Task task = new Task("Task", "Description", Status.NEW);
        int taskId = manager.addTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task responseTask = gson.fromJson(response.body(), Task.class);
        assertNotNull(responseTask, "Задача не возвращается");
        assertEquals(taskId, responseTask.getID(), "Некорректный ID задачи");
        assertEquals("Task", responseTask.getName(), "Некорректное имя задачи");
        assertEquals(Status.NEW, responseTask.getStatus(), "Некорректный статус задачи");
    }

    @Test
    public void testGetTaskByIdNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Должен возвращаться 404 для несуществующей задачи");
    }

    @Test
    public void testGetAllEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Description 1", Status.NEW);
        Epic epic2 = new Epic("Epic 2", "Description 2", Status.NEW);
        manager.addEpic(epic1);
        manager.addEpic(epic2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic[] epics = gson.fromJson(response.body(), Epic[].class);
        assertNotNull(epics, "Эпики не возвращаются");
        assertEquals(2, epics.length, "Некорректное количество эпиков");
        assertEquals("Epic 1", epics[0].getName(), "Некорректное имя первого эпика");
        assertEquals("Epic 2", epics[1].getName(), "Некорректное имя второго эпика");
    }

    @Test
    public void testGetEpicById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description", Status.NEW);
        int epicId = manager.addEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic responseEpic = gson.fromJson(response.body(), Epic.class);
        assertNotNull(responseEpic, "Эпик не возвращается");
        assertEquals(epicId, responseEpic.getID(), "Некорректный ID эпика");
        assertEquals("Epic", responseEpic.getName(), "Некорректное имя эпика");
    }

    @Test
    public void testGetEpicSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Test Epic", "Description", Status.NEW);
        int epicId = manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.IN_PROGRESS, epicId);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicId + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertNotNull(subtasks, "Подзадачи не возвращаются");
        assertEquals(2, subtasks.length, "Некорректное количество подзадач");
        assertEquals("Subtask 1", subtasks[0].getName(), "Некорректное имя первой подзадачи");
        assertEquals("Subtask 2", subtasks[1].getName(), "Некорректное имя второй подзадачи");
    }

    @Test
    public void testGetAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "For testing", Status.NEW);
        int epicId = manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, epicId);
        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.DONE, epicId);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Некорректный статус код");

        Subtask[] subtasks = gson.fromJson(response.body(), Subtask[].class);
        assertNotNull(subtasks, "Подзадачи не возвращаются");
        assertEquals(2, subtasks.length, "Некорректное количество подзадач");
        assertEquals("Subtask 1", subtasks[0].getName(), "Некорректное имя первой подзадачи");
        assertEquals("Subtask 2", subtasks[1].getName(), "Некорректное имя второй подзадачи");
    }

    @Test
    public void testGetSubtaskById() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description", Status.NEW);
        int epicId = manager.addEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, epicId);
        int subtaskId = manager.addSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask responseSubtask = gson.fromJson(response.body(), Subtask.class);
        assertNotNull(responseSubtask, "Подзадача не возвращается");
        assertEquals(subtaskId, responseSubtask.getID(), "Некорректный ID подзадачи");
        assertEquals("Subtask", responseSubtask.getName(), "Некорректное имя подзадачи");
        assertEquals(epicId, responseSubtask.getEpicID(), "Некорректный ID эпика");
    }

    @Test
    public void testGetEpicByIdNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode());
    }

    @Test
    public void testGetEpicSubtasksNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Должен возвращаться 404 для несуществующего эпика");
    }

    @Test
    public void testDeleteTask() throws IOException, InterruptedException {
        Task task = new Task("Task", "Description", Status.NEW);
        task.setStartTime(LocalDateTime.now());
        task.setDuration(Duration.ofMinutes(30));
        int taskId = manager.addTask(task);

        assertNotNull(manager.getTaskByID(taskId), "Задача должна существовать перед удалением");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        assertNull(manager.getTaskByID(taskId), "Задача должна быть удалена из менеджера");
        assertTrue(response.body().contains("Задача с id " + taskId + " успешно удалена!"), "Ответ должен содержать сообщение об успешном удалении");
    }

    @Test
    public void testDeleteEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description", Status.NEW);
        int epicId = manager.addEpic(epic);

        assertNotNull(manager.getEpicByID(epicId), "Эпик должен существовать перед удалением");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Некорректный статус код при удалении эпика");
        assertNull(manager.getEpicByID(epicId), "Эпик должен быть удален из менеджера");
        assertTrue(response.body().contains("Эпик с id " + epicId + " успешно удален!"), "Ответ должен содержать сообщение об успешном удалении эпика");
    }

    @Test
    public void testDeleteSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description", Status.NEW);
        int epicId = manager.addEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, epicId);
        subtask.setStartTime(LocalDateTime.now());
        subtask.setDuration(Duration.ofMinutes(20));
        int subtaskId = manager.addSubtask(subtask);

        assertNotNull(manager.getSubtaskByID(subtaskId), "Подзадача должна существовать перед удалением");

        assertEquals(1, manager.getSubtasksByEpic(epicId).size(), "Эпик должен содержать подзадачу");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertNull(manager.getSubtaskByID(subtaskId), "Подзадача должна быть удалена из менеджера");
        assertEquals(0, manager.getSubtasksByEpic(epicId).size(), "Эпик не должен содержать подзадач после удаления");
        assertTrue(response.body().contains("Cабтаск с id " + subtaskId + " успешно удален!"), "Ответ должен содержать сообщение об успешном удалении подзадачи");
    }


    @Test
    public void testGetHistory() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        Task task2 = new Task("Task 2", "Description 2", Status.IN_PROGRESS);
        int task1Id = manager.addTask(task1);
        int task2Id = manager.addTask(task2);

        manager.getTaskByID(task1Id);
        manager.getTaskByID(task2Id);
        manager.getTaskByID(task1Id);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertNotNull(history, "История не возвращается");
        assertEquals(2, history.length, "Некорректное количество задач в истории");
        assertEquals("Task 2", history[0].getName(), "Некорректная первая задача в истории");
        assertEquals("Task 1", history[1].getName(), "Некорректная вторая задача в истории");
    }

    @Test
    public void testGetEmptyHistory() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] history = gson.fromJson(response.body(), Task[].class);
        assertNotNull(history, "История не возвращается");
        assertEquals(0, history.length, "История должна быть пустой");
    }

    @Test
    public void testGetPrioritizedTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Description 1", Status.NEW);
        task1.setStartTime(LocalDateTime.of(2024, 1, 15, 10, 0));
        task1.setDuration(Duration.ofMinutes(30));

        Task task2 = new Task("Task 2", "Description 2", Status.IN_PROGRESS);
        task2.setStartTime(LocalDateTime.of(2024, 1, 15, 9, 0)); // Раньше чем task1
        task2.setDuration(Duration.ofMinutes(45));

        manager.addTask(task1);
        manager.addTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] prioritizedTasks = gson.fromJson(response.body(), Task[].class);
        assertNotNull(prioritizedTasks, "Приоритетные задачи не возвращаются");
        assertEquals(2, prioritizedTasks.length, "Некорректное количество приоритетных задач");

        assertEquals("Task 2", prioritizedTasks[0].getName(), "Задача с более ранним временем должна быть первой");
        assertEquals("Task 1", prioritizedTasks[1].getName(), "Задача с более поздним временем должна быть второй");
    }

    @Test
    public void testGetPrioritizedTasksWithEpicsAndSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description", Status.NEW);
        int epicId = manager.addEpic(epic);

        Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, epicId);
        subtask1.setStartTime(LocalDateTime.of(2024, 1, 15, 11, 0));
        subtask1.setDuration(Duration.ofMinutes(20));

        Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.DONE, epicId);
        subtask2.setStartTime(LocalDateTime.of(2024, 1, 15, 10, 0)); // Раньше чем subtask1
        subtask2.setDuration(Duration.ofMinutes(30));

        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] prioritizedTasks = gson.fromJson(response.body(), Task[].class);
        assertNotNull(prioritizedTasks, "Приоритетные задачи не возвращаются");
        assertEquals(2, prioritizedTasks.length, "Некорректное количество приоритетных задач");
        assertEquals("Subtask 2", prioritizedTasks[0].getName(), "Некорректная первая подзадача в приоритетном списке");
        assertEquals("Subtask 1", prioritizedTasks[1].getName(), "Некорректная вторая подзадача в приоритетном списке");
    }

    @Test
    public void testGetEmptyPrioritizedTasks() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task[] prioritizedTasks = gson.fromJson(response.body(), Task[].class);
        assertNotNull(prioritizedTasks, "Приоритетные задачи не возвращаются");
        assertEquals(0, prioritizedTasks.length, "Приоритетный список должен быть пустым");
    }

    @Test
    public void testUpdateTask() throws IOException, InterruptedException {
        Task task = new Task("Task", "Description", Status.NEW);
        task.setStartTime(LocalDateTime.of(2024, 1, 15, 10, 0));
        task.setDuration(Duration.ofMinutes(30));
        int taskId = manager.addTask(task);

        Task updatedTask = new Task("Updated Task", "Updated Description", Status.IN_PROGRESS);
        updatedTask.setID(taskId);
        updatedTask.setStartTime(LocalDateTime.of(2024, 1, 15, 11, 0));
        updatedTask.setDuration(Duration.ofMinutes(45));

        String updatedTaskJson = gson.toJson(updatedTask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + taskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedTaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Task taskFromManager = manager.getTaskByID(taskId);
        assertNotNull(taskFromManager, "Задача должна существовать после обновления");
        assertEquals("Updated Task", taskFromManager.getName(), "Имя задачи не обновилось");
        assertEquals("Updated Description", taskFromManager.getDescription(), "Описание задачи не обновилось");
        assertEquals(Status.IN_PROGRESS, taskFromManager.getStatus(), "Статус задачи не обновился");

        Task responseTask = gson.fromJson(response.body(), Task.class);
        assertEquals("Updated Task", responseTask.getName(), "Ответ сервера содержит некорректное имя задачи");
    }

    @Test
    public void testUpdateEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description", Status.NEW);
        int epicId = manager.addEpic(epic);

        Epic updatedEpic = new Epic("Updated Epic", "Updated Description", Status.NEW);
        updatedEpic.setID(epicId);

        String updatedEpicJson = gson.toJson(updatedEpic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedEpicJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Epic epicFromManager = manager.getEpicByID(epicId);
        assertNotNull(epicFromManager, "Эпик должен существовать после обновления");
        assertEquals("Updated Epic", epicFromManager.getName(), "Имя эпика не обновилось");
        assertEquals("Updated Description", epicFromManager.getDescription(), "Описание эпика не обновилось");
    }

    @Test
    public void testUpdateSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Description", Status.NEW);
        int epicId = manager.addEpic(epic);

        Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, epicId);
        subtask.setStartTime(LocalDateTime.of(2024, 1, 15, 10, 0));
        subtask.setDuration(Duration.ofMinutes(30));
        int subtaskId = manager.addSubtask(subtask);

        Subtask updatedSubtask = new Subtask("Updated Subtask", "Updated Description", Status.DONE, epicId);
        updatedSubtask.setID(subtaskId);
        updatedSubtask.setStartTime(LocalDateTime.of(2024, 1, 15, 11, 0));
        updatedSubtask.setDuration(Duration.ofMinutes(45));

        String updatedSubtaskJson = gson.toJson(updatedSubtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + subtaskId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedSubtaskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        Subtask subtaskFromManager = manager.getSubtaskByID(subtaskId);
        assertNotNull(subtaskFromManager, "Подзадача должна существовать после обновления");
        assertEquals("Updated Subtask", subtaskFromManager.getName(), "Имя подзадачи не обновилось");
        assertEquals("Updated Description", subtaskFromManager.getDescription(), "Описание подзадачи не обновилось");
        assertEquals(Status.DONE, subtaskFromManager.getStatus(), "Статус подзадачи не обновился");

        Epic epicFromManager = manager.getEpicByID(epicId);
        assertEquals(Status.DONE, epicFromManager.getStatus(), "Статус эпика должен обновиться после изменения подзадачи");
    }

    @Test
    public void testUpdateNonExistentTask() throws IOException, InterruptedException {
        Task task = new Task("Non-existent Task", "Description", Status.NEW);
        task.setID(999);

        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, response.statusCode(), "Должен возвращаться 404 для несуществующей задачи");
    }
}