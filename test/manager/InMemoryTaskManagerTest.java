package manager;

import exceptions.TaskValidationException;
import models.Subtask;
import models.Epic;
import models.Status;
import models.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

   @BeforeEach
   void init() {
      taskManager = (InMemoryTaskManager) getTaskManager();
   }

   @Override
   TaskManager getTaskManager() {
      return new InMemoryTaskManager(new InMemoryHistoryManager());
   }

   @Test
   void shouldCalculateOverlapCorrectly() {
      Task task1 = new Task("Task 1", "Description 1", Status.NEW);
      task1.setStartTime(LocalDateTime.of(2025, 10, 15, 10, 0));
      task1.setDuration(Duration.ofHours(2)); // 10:00-12:00

      Task overlapping = new Task("Overlap", "Description", Status.NEW); // 11:00-13:00
      overlapping.setStartTime(LocalDateTime.of(2025, 10, 15, 11, 0));
      overlapping.setDuration(Duration.ofHours(2));

      Task nonOverlapping = new Task("Non-overlap", "Description", Status.NEW); // 12:00-14:00
      nonOverlapping.setStartTime(LocalDateTime.of(2025, 10, 15, 12, 0));
      nonOverlapping.setDuration(Duration.ofHours(2));

      taskManager.addTask(task1);

      assertThrows(TaskValidationException.class, () -> taskManager.addTask(overlapping));
      assertDoesNotThrow(() -> taskManager.addTask(nonOverlapping));
   }


   @Test
   void shouldAddTasksToPrioritizedTasks() {
      Task task1 = new Task("Task 1", "Description 1", Status.NEW);
      task1.setStartTime(LocalDateTime.of(2025, 1, 15, 10, 0));
      task1.setDuration(Duration.ofHours(2));

      Task task2 = new Task("Task 2", "Description 2", Status.IN_PROGRESS);
      task2.setStartTime(LocalDateTime.of(2025, 1, 15, 8, 0));
      task2.setDuration(Duration.ofHours(1));

      taskManager.addTask(task1);
      taskManager.addTask(task2);

      assertEquals(2, taskManager.prioritizedTasks.size(),
              "В prioritizedTasks должно быть 2 задачи");
   }

   @Test
   void shouldNotAddTasksWithoutTimeToPrioritizedTasks() {
      Task task1 = new Task("Task 1", "Description 1", Status.NEW);
      Task task2 = new Task("Task 2", "Description 2", Status.IN_PROGRESS);

      taskManager.addTask(task1);
      taskManager.addTask(task2);

      assertEquals(0, taskManager.getPrioritizedTasks().size(),
              "В prioritizedTasks не должно быть задач без времени");
   }

   @Test
   void shouldOrderTasksByStartTimeInPrioritizedTasks() {
      Task task1 = new Task("Task 1", "Description 1", Status.NEW);
      task1.setStartTime(LocalDateTime.of(2025, 1, 15, 12, 0)); // Позднее
      task1.setDuration(Duration.ofHours(1));

      Task task2 = new Task("Task 2", "Description 2", Status.IN_PROGRESS);
      task2.setStartTime(LocalDateTime.of(2025, 1, 15, 9, 0));  // Ранее
      task2.setDuration(Duration.ofHours(2));

      taskManager.addTask(task1);
      taskManager.addTask(task2);

      List<Task> prioritizedList = new ArrayList<>(taskManager.getPrioritizedTasks());
      assertEquals(2, prioritizedList.size(), "Должно быть 2 задачи");

      assertEquals(task2, prioritizedList.get(0),
              "Первой должна быть задача с более ранним startTime");
      assertEquals(task1, prioritizedList.get(1),
              "Второй должна быть задача с более поздним startTime");
   }

   @Test
   void shouldRemoveTaskFromPrioritizedTasksWhenDeleted() {
      Task task1 = new Task("Task 1", "Description 1", Status.NEW);
      task1.setStartTime(LocalDateTime.of(2025, 1, 15, 10, 0));
      task1.setDuration(Duration.ofHours(2));

      int taskId = taskManager.addTask(task1);

      assertEquals(1, taskManager.getPrioritizedTasks().size(),
              "Задача должна быть в prioritizedTasks");

      taskManager.deleteTaskByID(taskId);

      assertEquals(0, taskManager.getPrioritizedTasks().size(),
              "Задача должна удалиться из prioritizedTasks");
   }


   @Test
   void shouldIncludeSubtasksInPrioritizedTasks() {
      Epic epic = new Epic("Epic", "Description", Status.NEW);
      int epicId = taskManager.addEpic(epic);

      Subtask subtask1 = new Subtask("Subtask 1", "Description 1", Status.NEW, epicId);
      subtask1.setStartTime(LocalDateTime.of(2025, 1, 15, 11, 0));
      subtask1.setDuration(Duration.ofHours(1));

      Subtask subtask2 = new Subtask("Subtask 2", "Description 2", Status.IN_PROGRESS, epicId);
      subtask2.setStartTime(LocalDateTime.of(2025, 1, 15, 9, 0));
      subtask2.setDuration(Duration.ofHours(2));

      taskManager.addSubtask(subtask1);
      taskManager.addSubtask(subtask2);

      assertEquals(2, taskManager.getPrioritizedTasks().size(),
              "Подзадачи должны быть в prioritizedTasks");

      List<Task> prioritizedList = new ArrayList<>(taskManager.getPrioritizedTasks());
      assertEquals(subtask2, prioritizedList.get(0),
              "Первой должна быть подзадача с более ранним startTime");
      assertEquals(subtask1, prioritizedList.get(1),
              "Второй должна быть подзадача с более поздним startTime");
   }

   @Test
   void shouldHandleMixedTasksAndSubtasksInPrioritizedTasks() {
      Task task = new Task("Task", "Description", Status.NEW);
      task.setStartTime(LocalDateTime.of(2025, 1, 15, 12, 0));
      task.setDuration(Duration.ofHours(1));

      Epic epic = new Epic("Epic", "Description", Status.NEW);
      int epicId = taskManager.addEpic(epic);

      Subtask subtask = new Subtask("Subtask", "Description", Status.NEW, epicId);
      subtask.setStartTime(LocalDateTime.of(2025, 1, 15, 10, 0));
      subtask.setDuration(Duration.ofHours(2));

      taskManager.addTask(task);
      taskManager.addSubtask(subtask);

      List<Task> prioritizedList = new ArrayList<>(taskManager.getPrioritizedTasks());
      assertEquals(2, prioritizedList.size());
      assertEquals(subtask, prioritizedList.get(0),
              "Первой должна быть подзадача с более ранним startTime");
      assertEquals(task, prioritizedList.get(1),
              "Второй должна быть задача с более поздним startTime");
   }

   @Test
   void shouldCalculatePrioritizedTasksCorrectlyAfterMultipleOperations() {
      Task task1 = new Task("Task 1", "Description 1", Status.NEW);
      task1.setStartTime(LocalDateTime.of(2025, 1, 15, 14, 0));
      task1.setDuration(Duration.ofHours(1));

      Task task2 = new Task("Task 2", "Description 2", Status.IN_PROGRESS);
      task2.setStartTime(LocalDateTime.of(2025, 1, 15, 10, 0));
      task2.setDuration(Duration.ofHours(2));

      taskManager.addTask(task1);
      taskManager.addTask(task2);

      assertEquals(2, taskManager.getPrioritizedTasks().size());

      taskManager.deleteTaskByID(task1.getID());
      assertEquals(1, taskManager.getPrioritizedTasks().size());

      Task task3 = new Task("Task 3", "Description 3", Status.DONE);
      taskManager.addTask(task3);

      assertEquals(1, taskManager.getPrioritizedTasks().size());
   }
}