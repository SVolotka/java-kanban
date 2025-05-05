package models;

import java.util.ArrayList;

public class Epic extends Task {
    ArrayList<Integer> subtasks = new ArrayList<>();

    public Epic(String name, String description, Status status) {
        super(name, description, status);
    }

    public ArrayList<Integer> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(ArrayList<Integer> subtasks) {
        this.subtasks = subtasks;
    }
}
