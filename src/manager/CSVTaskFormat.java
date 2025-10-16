package manager;

import models.Status;
import models.Task;
import models.Epic;
import models.Subtask;
import models.TaskType;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class CSVTaskFormat {

    public static String getHeader() {
        return "id,type,name,status,description,startTime,duration,epic";
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

        TaskType taskType = determineTaskTypeFromString(fields[1]);
        Task task = switch (taskType) {
            case TASK -> new Task(name, description, status);
            case EPIC -> new Epic(name, description, status);
            case SUBTASK -> {
                int epicID = Integer.parseInt(fields[7]);

                yield new Subtask(name, description, status, epicID);
            }
        };
        if (!fields[5].equals("-")) {
            LocalDateTime startTime = LocalDateTime.parse(fields[5], DateTimeFormatter.ofPattern("dd.MM.yy: HH:mm"));
            task.setStartTime(startTime);
        }

        if (!fields[6].equals("-")) {
            Duration duration = Duration.ofMinutes(Long.parseLong(fields[6]));
            task.setDuration(duration);
        }

        return task;
    }

    public static TaskType determineTaskType(Task task) {
        return task.getType();
    }

    public static TaskType determineTaskTypeFromString(String str) {
        return TaskType.valueOf(str);
    }

    private static String createStringFromTask(Task task, TaskType taskType) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy: HH:mm");

        String startTime = task.getStartTime() != null ?
                task.getStartTime().format(formatter) : "-";

        String duration = task.getDuration() != null ?
                String.valueOf(task.getDuration().toMinutes()) : "-";

        String epic = "-";

        return String.join(",",
                String.valueOf(task.getID()),
                taskType.name(),
                task.getName(),
                task.getStatus().name(),
                task.getDescription(),
                startTime,
                duration,
                epic
        );
    }

    private static String createSubtaskString(Subtask subtask, TaskType taskType) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy: HH:mm");

        String startTime = subtask.getStartTime() != null ?
                subtask.getStartTime().format(formatter) : "-";

        String duration = subtask.getDuration() != null ?
                String.valueOf(subtask.getDuration().toMinutes()) : "-";

        return String.join(",",
                String.valueOf(subtask.getID()),
                taskType.name(),
                subtask.getName(),
                subtask.getStatus().name(),
                subtask.getDescription(),
                startTime,
                duration,
                String.valueOf(subtask.getEpicID())
        );
    }
}