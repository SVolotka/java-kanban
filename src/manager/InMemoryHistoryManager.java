package manager;

import models.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager {
    private static final int MAX_HISTORY_LIST_SIZE = 10;
    private ArrayList<Task> historyList = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (task != null) {
            if (historyList.size() == MAX_HISTORY_LIST_SIZE) {
                historyList.removeFirst();
                historyList.add(task);
            }
            historyList.add(task);
        }
    }

    @Override
    public ArrayList<Task> getHistory() {
        return new ArrayList<>(historyList);
    }
}
