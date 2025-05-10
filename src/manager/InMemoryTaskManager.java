package manager;

import models.Epic;
import models.Status;
import models.Subtask;
import models.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    private int identifier = 1;
    private Map<Integer, Task> tasks = new HashMap<>();
    private Map<Integer, Epic> epics = new HashMap<>();
    private Map<Integer, Subtask> subtasks = new HashMap<>();
    private HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }

    private int setIdentifier() {
        return identifier++;
    }

    @Override
    public int addTask(Task task) {
        int newID = setIdentifier();
        task.setID(newID);
        tasks.put(task.getID(), task);
        return newID;
    }

    @Override
    public int addEpic(Epic epic) {
        int newID = setIdentifier();
        epic.setID(newID);
        epics.put(epic.getID(), epic);
        return newID;
    }

    @Override
    public Integer addSubtask(Subtask subtask) {
        if (epics.containsKey(subtask.getEpicID())) {
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
            return newID;
        } else {
            return null;
        }
    }

    @Override
    public List<Task> getAllTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getAllEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public void deleteAllTasks() {
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
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

    @Override
    public Task getTaskByID(int id) {
        if (tasks.containsKey(id)) {
            Task task = tasks.get(id);
            Task taskForHistory = new Task(task.getName(), task.getDescription(), task.getStatus());
            taskForHistory.setID(task.getID());
            historyManager.add(taskForHistory);

            return tasks.get(id);
        } else {
            return null;
        }

    }

    @Override
    public Epic getEpicByID(int id) {
        if (epics.containsKey(id)) {
            Epic epic = epics.get(id);
            Epic epicForHistory = new Epic(epic.getName(), epic.getDescription(), epic.getStatus());
            epicForHistory.setID(epic.getID());
            historyManager.add(epicForHistory);

            return epics.get(id);
        } else {
            return null;
        }
    }

    @Override
    public Subtask getSubtaskByID(int id) {
        if (subtasks.containsKey(id)) {
            Subtask subtask = subtasks.get(id);
            Subtask subtaskForHistory = new Subtask(subtask.getName(), subtask.getDescription(), subtask.getStatus(), subtask.getEpicID());
            subtaskForHistory.setID(subtask.getID());
            historyManager.add(subtasks.get(id));

            return subtasks.get(id);
        } else {
            return null;
        }
    }

    @Override
    public void updateTask(Task task) {
        if (tasks.containsKey(task.getID())) {
            Task existingTask = tasks.get(task.getID());
            existingTask.setName(task.getName());
            existingTask.setDescription(task.getDescription());
            existingTask.setStatus(task.getStatus());
        }
    }

    @Override
    public void updateEpic(Epic epic) {
        if (epics.containsKey((epic.getID()))) {
            Epic existingEpic = epics.get(epic.getID());
            existingEpic.setName(epic.getName());
            existingEpic.setDescription(epic.getDescription());
            existingEpic.setStatus(epic.getStatus());
        }
    }

    @Override
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

    @Override
    public Task deleteTaskByID(int id) {
        return tasks.remove(id);
    }

    @Override
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

    @Override
    public Subtask deleteSubtaskByID(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            subtasks.remove(id);
            Epic epic = epics.get(subtask.getEpicID());
            ArrayList<Integer> newSubtasksByEpic = epic.getSubtasks();
            newSubtasksByEpic.remove(subtask);
            epic.setSubtasks(newSubtasksByEpic);
            checkEpicStatus(epic, newSubtasksByEpic);
            return subtask;
        } else {
            System.out.println("Задачи с таким id нет!");
        }
        return null;
    }

    @Override
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

    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistory());
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
