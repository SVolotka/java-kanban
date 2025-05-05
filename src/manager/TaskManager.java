package manager;

import models.Epic;
import models.Status;
import models.Subtask;
import models.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private int identifier = 1;
    private HashMap<Integer, Task> tasks;
    private HashMap<Integer, Epic> epics;
    private HashMap<Integer, Subtask> subtasks;

    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
    }

    private int setIdentifier() {
        return identifier++;
    }

    public Task addTask(Task task) {
        int newID = setIdentifier();
        task.setID(newID);
        tasks.put(task.getID(), task);
        return task;
    }

    public Epic addEpic(Epic epic) {
        int newID = setIdentifier();
        epic.setID(newID);
        epics.put(epic.getID(), epic);
        return epic;
    }

    public Subtask addSubtask(Subtask subtask) {
        int newID = setIdentifier();
        subtask.setID(newID);
        subtasks.put(subtask.getID(), subtask);

        Epic epic = epics.get(subtask.getEpicID());
        if (epic.getSubtasks() != null) {
            ArrayList<Integer> newSubtasksByEpic = epic.getSubtasks();
            newSubtasksByEpic.add(subtask.getID());
            epic.setSubtasks(newSubtasksByEpic);
            checkEpicStatus(epic, epic.getSubtasks());
        } else {
            ArrayList<Integer> newSubtasksByEpic = new ArrayList<>();
            newSubtasksByEpic.add(subtask.getEpicID());
            epic.setSubtasks(newSubtasksByEpic);
            checkEpicStatus(epic, epic.getSubtasks());
        }
        return subtask;
    }

    public ArrayList<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    public void deleteAllTasks() {
        tasks.clear();
    }

    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    public void deleteAllSubtasks() {
        subtasks.clear();
        epics.clear();
    }

    public Task getTaskByID(int id) {
        if (tasks.containsKey(id)) {
            Task existingTask = tasks.get(id);
            System.out.println(existingTask.toString());
            return existingTask;
        }
        System.out.println("Задачи с таким id нет!");
        return null;
    }

    public Epic getEpicByID(int id) {
        if (epics.containsKey(id)) {
            return epics.get(id);
        }
        System.out.println("Эпика с таким id нет!");
        return null;
    }

    public Subtask getSubtaskByID(int id) {
        if (subtasks.containsKey(id)) {
            return subtasks.get(id);
        }
        System.out.println("Подзадачи с таким id нет!");
        return null;
    }

    public Task updateTask(Task task) {
        if (tasks.containsKey(task.getID())) {
            Task existingTask = tasks.get(task.getID());
            existingTask.setName(task.getName());
            existingTask.setDescription(task.getDescription());
            existingTask.setStatus(task.getStatus());
        }
        return task;
    }

    public Epic updateEpic(Epic epic) {
        if (epics.containsKey((epic.getID()))) {
            Epic existingEpic = epics.get(epic.getID());
            existingEpic.setName(epic.getName());
            existingEpic.setDescription(epic.getDescription());
            existingEpic.setStatus(epic.getStatus());
        }
        return epic;
    }

    public void updateSubtask(Subtask subtask) {
        if (subtasks.containsKey(subtask.getID())) {
            Subtask existingSubtask = subtasks.get(subtask.getID());
            existingSubtask.setName(subtask.getName());
            existingSubtask.setDescription(subtask.getDescription());
            existingSubtask.setStatus(subtask.getStatus());
            Epic epic = epics.get(subtask.getEpicID());
            checkEpicStatus(epic, epic.getSubtasks());
        }
    }

    public Task deleteTaskByID(int id) {
        return tasks.remove(id);
    }

    public Epic deleteEpicByID(int id) {
        Epic epic = epics.get(id);
        epics.remove(id);
        if (epic != null) {
            for (Integer subtaskID : epic.getSubtasks()) {
                subtasks.remove(subtaskID);
            }
            return epic;
        }
        System.out.println("Задачи с таким id нет!");
        return null;
    }

    public Subtask deleteSubtaskByID(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            subtasks.remove(id);
            Epic epic = epics.get(subtask.getEpicID());
            ArrayList<Integer> newSubtasksByEpic = epic.getSubtasks();
            newSubtasksByEpic.remove(id);
            epic.setSubtasks(newSubtasksByEpic);
            checkEpicStatus(epic, newSubtasksByEpic);
            return subtask;
        } else {
            System.out.println("Задачи с таким id нет!");
        }
        return null;
    }

    public ArrayList<Subtask> getSubtasksByEpic(int idEpic) {
        Epic foundedEpic = getEpicByID(idEpic);
        if (foundedEpic != null) {
            ArrayList<Integer> subtasksID = foundedEpic.getSubtasks();
            ArrayList<Subtask> subtasksByID = new ArrayList<>();
            for (Integer subtaskID : subtasksID) {
                subtasksByID.add(subtasks.get(subtaskID));
            }
            return subtasksByID;
        } else {
            System.out.println("Эпика с таким id нет!");
            return null;
        }
    }

    private void checkEpicStatus(Epic epic, ArrayList<Integer> subtasksID) {
        int newCounter = 0;
        int doneCounter = 0;
        int subtasksSize = subtasksID.size();
        ArrayList<Subtask> subtasksByEpic = new ArrayList<>();

        for (Integer integer : subtasksID) {
            for (Subtask value : subtasks.values()) {
                if (value.getID() == integer) {
                    subtasksByEpic.add(value);
                }
            }
        }

        for (Subtask subtask : subtasksByEpic) {
            Status subtaskStatus = subtask.getStatus();
            switch (subtaskStatus) {
                case NEW:
                    newCounter += 1;
                    break;
                case DONE:
                    doneCounter += 1;
                    break;
                case IN_PROGRESS:
                    epic.setStatus(Status.IN_PROGRESS);
                    return;
            }
        }

        if (newCounter == subtasksSize) {
            epic.setStatus(Status.NEW);
        } else if (doneCounter == subtasksSize) {
            epic.setStatus(Status.DONE);
        }
    }
}
