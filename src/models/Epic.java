package models;

import java.util.ArrayList;

public class Epic extends Task {
    ArrayList<Integer> subtaskIDs = new ArrayList<>();

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
}
