package models;

public class Node {
    Task task;
    Node prev;
    Node next;

    public Node(Node prev, Task task, Node next) {
        this.task = task;
        this.prev = prev;
        this.next = next;
    }

    public Task getTask() {
        return task;
    }

    public Node getPrev() {
        return prev;
    }

    public Node getNext() {
        return next;
    }

    public void setPrev(Node node) {
        prev = node;
    }

    public void setNext(Node node) {
        next = node;
    }
}