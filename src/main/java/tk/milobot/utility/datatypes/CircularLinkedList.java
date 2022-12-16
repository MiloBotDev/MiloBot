package tk.milobot.utility.datatypes;

import java.util.ArrayList;
import java.util.List;

public class CircularLinkedList<T> {

    private Node<T> head;

    private static class Node<T> {
        T data;
        Node<T> prev;
        Node<T> next;

        Node(T d) {
            data = d;
        }
    }

    public void append(T new_data) {
        Node<T> new_node = new Node<>(new_data);
        if(head == null) {
            head = new_node;
            head.next = head;
            head.prev = head;
        } else {
            new_node.prev = head.prev;
            new_node.next = head;
            head.prev.next = new_node;
            head.prev = new_node;
        }
    }

    public void removeToNext() {
        if(head == head.next) {
            head = null;
        } else {
            head.next.prev = head.prev;
            head.prev.next = head.next;
            head = head.next;
        }
    }

    public void removeToPrevious() {
        if(head == head.next) {
            head = null;
        } else {
            head.next.prev = head.prev;
            head.prev.next = head.next;
            head = head.prev;
        }
    }

    public void goToNext() {
        head = head.next;
    }

    public void goToPrevious() {
        head = head.prev;
    }

    public List<T> toList() {
        List<T> list = new ArrayList<>();
        Node<T> temp = head;
        do {
            list.add(temp.data);
            temp = temp.next;
        } while(temp != head);
        return list;
    }

    public T get() {
        return head.data;
    }

    public int size() {
        int size = 0;
        Node<T> temp = head;
        do {
            size++;
            temp = temp.next;
        } while(temp != head);
        return size;
    }
}