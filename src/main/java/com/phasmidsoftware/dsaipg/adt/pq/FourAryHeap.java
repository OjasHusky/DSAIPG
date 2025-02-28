package com.phasmidsoftware.dsaipg.adt.pq;

import java.util.Comparator;
import java.util.NoSuchElementException;

public class FourAryHeap<K> {
    private final Object[] heap;
    private final Comparator<K> comparator;
    private int size;
    private final int capacity;


    // Constructor that accepts a comparator and a capacity
    public FourAryHeap(int capacity, Comparator<K> comparator) {
        this.capacity = capacity; // Set capacity
        this.heap = new Object[capacity];
        this.comparator = comparator;
        this.size = 0;
    }

    public void insert(K value) {
        if (size == heap.length) throw new IllegalStateException("Heap is full");
        heap[size] = value;
        swim(size);
        size++;
    }

    @SuppressWarnings("unchecked")
    public K poll() {
        if (isEmpty()) throw new NoSuchElementException("Heap is empty");
        K root = (K) heap[0];
        heap[0] = heap[--size];
        heap[size] = null;
        sink(0);
        return root;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    /**
     * Floyd's Trick: Build heap using bottom-up sink approach
     */
    @SuppressWarnings("unchecked")
    public void buildHeap(K[] data) {
        size = Math.min(data.length, heap.length);
        System.arraycopy(data, 0, heap, 0, size);
        // Sink non-leaf nodes from bottom-up
        for (int i = (size - 1) / 4; i >= 0; i--) {
            sink(i);
        }
    }

    private void swim(int index) {
        while (index > 0) {
            int parent = (index - 1) / 4;
            if (comparator.compare((K) heap[index], (K) heap[parent]) >= 0) break;
            swap(index, parent);
            index = parent;
        }
    }

    private void sink(int index) {
        while (true) {
            int smallest = index;
            for (int i = 1; i <= 4; i++) {
                int child = 4 * index + i;
                if (child < size && comparator.compare((K) heap[child], (K) heap[smallest]) < 0) {
                    smallest = child;
                }
            }
            if (smallest == index) break;
            swap(index, smallest);
            index = smallest;
        }
    }

    private void swap(int i, int j) {
        Object temp = heap[i];
        heap[i] = heap[j];
        heap[j] = temp;
    }
}
