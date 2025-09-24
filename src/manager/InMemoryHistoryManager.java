package manager;

import models.Task;
import models.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryHistoryManager implements HistoryManager {
    private Node first;
    private Node last;
    Map<Integer, Node> historyMap = new HashMap<>();


    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }

        final int id = task.getID();
        if (historyMap.containsKey(id)) {
            removeNode(id);
        }
        linkLast(task);
    }

    @Override
    public void remove(int id) {
        removeNode(id);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private void linkLast(Task task) {
        final Node newNode = new Node(last, task, null);

        if (first == null) {
            first = newNode;
        } else {
            last.setNext(newNode);
        }

        last = newNode;
        historyMap.put(task.getID(), newNode);
    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node currentNode = first;

        while (currentNode != null) {
            tasks.add(currentNode.getTask());
            currentNode = currentNode.getNext();
        }
        return tasks;
    }

    private void removeNode(int id) {
        final Node node = historyMap.get(id);

        if (node == null) {
            return;
        }

        if (first == last) {
            first = null;
            last = null;
            historyMap.remove(id);
            return;
        }

        historyMap.remove(id);

        if (node == first) {
            first = node.getNext();
            if (first != null) {
                first.setPrev(null);
            }
        } else if (node == last) {
            last = node.getPrev();
            if (last != null) {
                last.setNext(null);
            }
        } else {
            node.getPrev().setNext(node.getNext());
            node.getNext().setPrev(node.getPrev());
        }
    }
}