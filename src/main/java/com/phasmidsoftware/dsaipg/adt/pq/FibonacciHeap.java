package com.phasmidsoftware.dsaipg.adt.pq;

import java.util.HashMap;
import java.util.Map;

public class FibonacciHeap<T> {

    private Node<T> max;
    private int size;

    private static class Node<T> {
        T data;
        double key;
        Node<T> parent, child, left, right;
        int degree;
        boolean mark;

        Node(T data, double key) {
            this.data = data;
            this.key = key;
            this.left = this;
            this.right = this;
        }
    }

    // Insert a new key into the max-heap
    public void insert(T data, double key) {
        Node<T> node = new Node<>(data, key);
        max = mergeLists(max, node);
        size++;
    }

    // Extracts the maximum element
    public T extractMax() {
        Node<T> oldMax = max;
        if (max != null) {
            if (max.child != null) {
                Node<T> child = max.child;
                do {
                    child.parent = null;
                    child = child.right;
                } while (child != max.child);
                mergeLists(max, max.child);
            }
            removeNode(max);
            if (max == max.right) {
                max = null;
            } else {
                max = max.right;
                consolidate();
            }
            size--;
        }
        return oldMax != null ? oldMax.data : null;
    }

    // Consolidation process to maintain heap structure
    private void consolidate() {
        Map<Integer, Node<T>> degreeTable = new HashMap<>();
        Node<T> start = max, current = max;

        do {
            Node<T> x = current;
            int degree = x.degree;

            // Consolidate nodes with the same degree
            while (degreeTable.containsKey(degree)) {
                Node<T> y = degreeTable.get(degree);
                if (x.key < y.key) {  // Max-Heap condition
                    Node<T> temp = x;
                    x = y;
                    y = temp;
                }
                link(y, x);
                degreeTable.remove(degree);
                degree++;
            }

            // Put the node in the degree table
            degreeTable.put(degree, x);
            current = current.right;
        } while (current != start);

        max = null;
        // Rebuild the max list from the degree table
        for (Node<T> node : degreeTable.values()) {
            if (node != null) {
                max = mergeLists(max, node);
            }
        }
    }

    // Makes node y a child of node x
    private void link(Node<T> y, Node<T> x) {
        removeNode(y);
        y.left = y.right = y;
        y.parent = x;
        if (x.child == null) {
            x.child = y;
        } else {
            mergeLists(x.child, y);
        }
        x.degree++;
        y.mark = false;
    }

    // Merges two circular doubly linked lists (Fixing previous incorrect implementation)
    private Node<T> mergeLists(Node<T> a, Node<T> b) {
        if (a == null) return b;
        if (b == null) return a;

        Node<T> aNext = a.right;
        Node<T> bPrev = b.left;

        a.right = b;
        b.left = a;
        aNext.left = bPrev;
        bPrev.right = aNext;

        return a.key > b.key ? a : b; // Max-heap condition
    }

    private void removeNode(Node<T> node) {
        node.left.right = node.right;
        node.right.left = node.left;
    }

    public int getSize() {
        return size;
    }

    public boolean isEmpty() {
        return max == null;
    }
}