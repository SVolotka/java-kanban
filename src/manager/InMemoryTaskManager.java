package manager;

import exceptions.TaskValidationException;
import models.Epic;
import models.Status;
import models.Subtask;
import models.Task;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class InMemoryTaskManager implements TaskManager {
    private int identifier = 1;
    protected Map<Integer, Task> tasks = new HashMap<>();
    protected Map<Integer, Epic> epics = new HashMap<>();
    protected Map<Integer, Subtask> subtasks = new HashMap<>();
    private final HistoryManager historyManager;
    protected Set<Task> prioritizedTasks = new TreeSet<>(Comparator.comparing(Task::getStartTime));


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
        addToPrioritizedTasksList(task);
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
            addToPrioritizedTasksList(subtask);

            Epic epic = epics.get(subtask.getEpicID());
            if (epic.getSubtaskIDs() != null) {
                ArrayList<Integer> newSubtasksByEpic = epic.getSubtaskIDs();
                newSubtasksByEpic.add(subtask.getID());
                epic.setSubtaskIDs(newSubtasksByEpic);
                checkEpicStatus(epic, epic.getSubtaskIDs());
                updateEpicTimesFields(epic);
            } else {
                ArrayList<Integer> newSubtasksByEpic = new ArrayList<>();
                newSubtasksByEpic.add(subtask.getEpicID());
                epic.setSubtaskIDs(newSubtasksByEpic);
                checkEpicStatus(epic, epic.getSubtaskIDs());
                updateEpicTimesFields(epic);
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
        for (Task task : tasks.values()) {
            historyManager.remove(task.getID());
            prioritizedTasks.remove(task);
        }
        tasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getID());
            prioritizedTasks.remove(subtask);
        }
        subtasks.clear();

        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getID());
            prioritizedTasks.remove(epic);
        }
        epics.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getID());
            prioritizedTasks.remove(subtask);
        }
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.setSubtaskIDs(new ArrayList<>());
            checkEpicStatus(epic, epic.getSubtaskIDs());
            updateEpicTimesFields(epic);
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
            updateEpicTimesFields(existingEpic);
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
            checkEpicStatus(epic, epic.getSubtaskIDs());
            updateEpicTimesFields(epic);
        }
    }

    @Override
    public Task deleteTaskByID(int id) {
        if (tasks.get(id) != null) {
            prioritizedTasks.remove(tasks.get(id));
            historyManager.remove(id);
            return tasks.remove(id);
        }
        System.out.println("Задачи с таким id нет!");
        return null;
    }

    @Override
    public Epic deleteEpicByID(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            prioritizedTasks.remove(epic);

            for (Integer subtaskID : epic.getSubtaskIDs()) {
                Subtask subtask = subtasks.get(subtaskID);
                if (subtask != null) {
                    prioritizedTasks.remove(subtask);
                }
                historyManager.remove(subtaskID);
                subtasks.remove(subtaskID);
            }

            historyManager.remove(id);
            epics.remove(id);
            return epic;
        }
        System.out.println("Задачи с таким id нет!");
        return null;
    }

    @Override
    public Subtask deleteSubtaskByID(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            prioritizedTasks.remove(subtask);
            historyManager.remove(id);
            subtasks.remove(id);

            Epic epic = epics.get(subtask.getEpicID());
            if (epic != null) {
                ArrayList<Integer> newSubtasksByEpic = epic.getSubtaskIDs();
                newSubtasksByEpic.remove((Integer) id);
                epic.setSubtaskIDs(newSubtasksByEpic);
                checkEpicStatus(epic, newSubtasksByEpic);
                updateEpicTimesFields(epic);
            }
            return subtask;
        }
        System.out.println("Задачи с таким id нет!");
        return null;
    }

    @Override
    public ArrayList<Subtask> getSubtasksByEpic(int idEpic) {
        Epic foundedEpic = getEpicByID(idEpic);
        if (foundedEpic == null) {
            System.out.println("Эпика с таким id нет!");
            return new ArrayList<>();
        }

        ArrayList<Integer> subtasksID = foundedEpic.getSubtaskIDs();
        if (subtasksID == null) {
            return new ArrayList<>();
        }

        return subtasksID.stream()
                .map(subtasks::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(ArrayList::new));
    }


    @Override
    public List<Task> getHistory() {
        return new ArrayList<>(historyManager.getHistory());
    }

    private void checkEpicStatus(Epic epic, ArrayList<Integer> subtaskIDs) {
        if (subtaskIDs == null) {
            epic.setStatus(Status.NEW);
            return;
        }

        List<Subtask> subtasksByEpic = subtaskIDs.stream()
                .flatMap(id -> subtasks.values().stream().filter(subtask -> subtask.getID() == id))
                .toList();

        boolean allNew = subtasksByEpic.stream().allMatch(st -> st.getStatus() == Status.NEW);
        boolean allDone = subtasksByEpic.stream().allMatch(st -> st.getStatus() == Status.DONE);

        if (allNew) {
            epic.setStatus(Status.NEW);
        } else if (allDone) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    private void updateEpicTimesFields(Epic epic) {
        if (epic == null) {
            return;
        }

        List<Subtask> subtasks = getSubtasksByEpic(epic.getID());
        epic.updateEpicTimesFields(Objects.requireNonNullElseGet(subtasks, ArrayList::new));
    }

    public void updateIdentifier(int newIdentifier) {
        this.identifier = newIdentifier;
    }

    private void addToPrioritizedTasksList(Task newTask) {
        if (newTask.getStartTime() == null || newTask.getEndTime() == null) {
            return;
        }

        prioritizedTasks.stream()
                .filter(existingTask -> isOverlapping(newTask, existingTask))
                .findFirst()
                .ifPresentOrElse(overlappedTask -> {
                            String message = "Задача пересекается с id= " + overlappedTask.getID() +
                                    " c " + overlappedTask.getStartTime() + " по " + overlappedTask.getEndTime();
                            throw new TaskValidationException(message);
                        },
                        () -> prioritizedTasks.add(newTask)
                );
    }

    public boolean isOverlapping(Task task1, Task task2) {
        LocalDateTime start1 = task1.getStartTime();
        LocalDateTime end1 = task1.getEndTime();

        LocalDateTime start2 = task2.getStartTime();
        LocalDateTime end2 = task2.getEndTime();

        return start1.isBefore(end2) && start2.isBefore(end1);
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }
}