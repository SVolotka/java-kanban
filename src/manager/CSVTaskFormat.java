package manager;

import models.Status;
import models.Task;
import models.Epic;
import models.Subtask;
import models.TaskType;

public final class CSVTaskFormat {

    public static String getHeader() {
        return "id,type,name,status,description,epic";
    }

    public static String createStringFromTask(Task task) {
        TaskType taskType = determineTaskType(task);
        return switch (taskType) {
            case TASK, EPIC -> createStringFromTask(task, taskType);
            case SUBTASK -> createSubtaskString((Subtask) task, taskType);
        };
    }

    public static Task createTaskFromString(String csvLine) {
        String[] fields = csvLine.split(",");
        String name = fields[2];
        String description = fields[4];
        Status status = Status.NEW;

        status = switch (fields[3]) {
            case "IN_PROGRESS" -> Status.IN_PROGRESS;
            case "DONE" -> Status.DONE;
            default -> status;
        };

        return switch (fields[1]) {
            case "TASK" -> new Task(name, description, status);
            case "EPIC" -> new Epic(name, description, status);
            case "SUBTASK" -> {
                int epicID = Integer.parseInt(fields[5]);
                yield new Subtask(name, description, status, epicID);
            }
            default -> null;
        };
    }

    public static TaskType determineTaskType(Task task) {
        if (task instanceof Epic) {
            return TaskType.EPIC;
        } else if (task instanceof Subtask) {
            return TaskType.SUBTASK;
        } else {
            return TaskType.TASK;
        }
    }

    private static String createStringFromTask(Task task, TaskType taskType) {
        return String.join(",", String.valueOf(task.getID()), taskType.name(), task.getName(),
                task.getStatus().name(), task.getDescription());
    }

    private static String createSubtaskString(Subtask subtask, TaskType taskType) {
        return String.join(",", String.valueOf(subtask.getID()), taskType.name(), subtask.getName(),
                subtask.getStatus().name(), subtask.getDescription(), String.valueOf(subtask.getEpicID()));
    }
}