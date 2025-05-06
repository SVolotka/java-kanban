package manager;

import models.Epic;
import models.Status;
import models.Subtask;
import models.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskManager {
    private int identifier = 1;
    private Map<Integer, Task> tasks = new HashMap<>();
    private Map<Integer, Epic> epics = new HashMap<>();
    private Map<Integer, Subtask> subtasks = new HashMap<>();

    private int setIdentifier() {
        return identifier++;
    }

    public void addTask(Task task) {
        int newID = setIdentifier();
        task.setID(newID);
        tasks.put(task.getID(), task);
    }

    public void addEpic(Epic epic) {
        int newID = setIdentifier();
        epic.setID(newID);
        epics.put(epic.getID(), epic);
    }

    public void addSubtask(Subtask subtask) {
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
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    public List<Subtask> getAllSubtasks() {
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

        for (Integer epicID : epics.keySet()) {
            Epic epic = epics.get(epicID);
            if (epic.getSubtasks() != null) {
                epic.setSubtasks(null);
                checkEpicStatus(epic, epic.getSubtasks());
            }
        }
    }

    public Task getTaskByID(int id) {
        return tasks.get(id);
    }

    public Epic getEpicByID(int id) {
        return epics.get(id);
    }

    public Subtask getSubtaskByID(int id) {
        return subtasks.get(id);
    }

    public void updateTask(Task task) {
        if (tasks.containsKey(task.getID())) {
            Task existingTask = tasks.get(task.getID());
            existingTask.setName(task.getName());
            existingTask.setDescription(task.getDescription());
            existingTask.setStatus(task.getStatus());
        }
    }

    public void updateEpic(Epic epic) {
        if (epics.containsKey((epic.getID()))) {
            Epic existingEpic = epics.get(epic.getID());
            existingEpic.setName(epic.getName());
            existingEpic.setDescription(epic.getDescription());
            existingEpic.setStatus(epic.getStatus());
        }
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
        if (subtasksID == null) {
            epic.setStatus(Status.NEW);
            return;
        }

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
