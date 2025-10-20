package models;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    ArrayList<Integer> subtaskIDs = new ArrayList<>();
    private LocalDateTime endTime;

    public Epic(String name, String description, Status status) {
        super(name, description, status);
    }

    public ArrayList<Integer> getSubtaskIDs() {
        return subtaskIDs;
    }

    public void setSubtaskIDs(ArrayList<Integer> subtaskIDs) {
        this.subtaskIDs = subtaskIDs;
    }

    @Override
    public TaskType getType() {
        return TaskType.EPIC;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void updateEpicTimesFields(List<Subtask> subtasks) {

        if (subtasks == null || subtasks.isEmpty()) {
            this.setStartTime(null);
            this.endTime = null;
            this.setDuration(Duration.ZERO);
            return;
        }

        List<Subtask> subtasksWithTime = subtasks.stream()
                .filter(subtask -> subtask.getStartTime() != null && subtask.getDuration() != null)
                .toList();

        if (subtasksWithTime.isEmpty()) {
            this.setStartTime(null);
            this.endTime = null;
            this.setDuration(Duration.ZERO);
        } else {
            LocalDateTime newStartTime = subtasksWithTime.stream()
                    .map(Subtask::getStartTime)
                    .min(LocalDateTime::compareTo)
                    .orElse(null);
            this.setStartTime(newStartTime);

            this.endTime = subtasksWithTime.stream()
                    .map(Subtask::getEndTime)
                    .max(LocalDateTime::compareTo)
                    .orElse(null);

            Duration newDuration = subtasksWithTime.stream()
                    .map(Subtask::getDuration)
                    .reduce(Duration.ZERO, Duration::plus);
            this.setDuration(newDuration);
        }
    }
}
