package manager;

import models.Epic;
import models.Subtask;
import models.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {

    int addTask(Task task);

    int addEpic(Epic epic);

    Integer addSubtask(Subtask subtask);

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    void deleteAllTasks();

    void deleteAllEpics();

    void deleteAllSubtasks();

    Task getTaskByID(int id);

    Epic getEpicByID(int id);

    Subtask getSubtaskByID(int id);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubtask(Subtask subtask);

    Task deleteTaskByID(int id);

    Epic deleteEpicByID(int id);

    Subtask deleteSubtaskByID(int id);

    ArrayList<Subtask> getSubtasksByEpic(int idEpic);

    List<Task> getHistory();
}
