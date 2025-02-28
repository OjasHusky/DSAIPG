package com.phasmidsoftware.dsaipg.adt.pq;

import com.phasmidsoftware.dsaipg.util.Benchmark_Timer;

import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Random;
import java.util.function.Consumer;

public class PQImplementationsBenchmarking {

    private static final int M = 4095;  // Maximum elements in heap
    private static final int INSERTIONS = 16000;
    private static final int REMOVALS = 4000;

    public static void main(String[] args) {
        System.out.println("Priority Queue Implementations Benchmarking\n");

        // Run benchmarks for each implementation
        runBenchmark("Basic Binary Heap", PQImplementationsBenchmarking::basicBinaryHeap);
        runBenchmark("Binary Heap with Floyd's Trick", PQImplementationsBenchmarking::binaryHeapWithFloyd);
        runBenchmark("4-ary Heap", PQImplementationsBenchmarking::fourAryHeap);
        runBenchmark("4-ary Heap with Floyd's Trick", PQImplementationsBenchmarking::fourAryHeapWithFloyd);
        runBenchmark("Fibonacci Heap", PQImplementationsBenchmarking::fibonacciHeap);
    }

    private static void runBenchmark(String description, Consumer<int[]> pqImplementation) {
        Benchmark_Timer<int[]> benchmark = new Benchmark_Timer<>(description, pqImplementation);
        int[] randomData = generateRandomArray(INSERTIONS);
        double time = benchmark.runFromSupplier(() -> Arrays.copyOf(randomData, randomData.length), 10);
        System.out.printf("%s Total Time: %.3f ms (Insertion + Removal)\n", description, time);
    }

    private static int[] generateRandomArray(int size) {
        Random random = new Random();
        int[] array = new int[size];
        for (int i = 0; i < size; i++) {
            array[i] = random.nextInt(1000000);
        }
        return array;
    }

    private static void basicBinaryHeap(int[] data) {
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        int spilled = 0;
        Integer highestPriority = null;
        long startTime = System.nanoTime();

        for (int value : data) {
            if (pq.size() < M) {
                pq.add(value);
            } else if (value > pq.peek()) {
                pq.poll();
                pq.add(value);
                spilled++;
            }
        }

        for (int i = 0; i < REMOVALS && !pq.isEmpty(); i++) {
            highestPriority = pq.poll();
        }

        long endTime = System.nanoTime();
        double totalTime = (endTime - startTime) / 1e6;

        System.out.printf("Spilled Elements: %d, Highest Priority: %d, Total Time: %.3f ms\n", spilled, highestPriority, totalTime);
    }

    private static void binaryHeapWithFloyd(int[] data) {
        PriorityQueue<Integer> pq = new PriorityQueue<>();
        int spilled = 0;
        Integer highestPriority = null;
        long startTime = System.nanoTime();

        pq.addAll(Arrays.asList(Arrays.stream(data).boxed().toArray(Integer[]::new)));

        for (int i = 0; i < REMOVALS && !pq.isEmpty(); i++) {
            highestPriority = pq.poll();
        }

        long endTime = System.nanoTime();
        double totalTime = (endTime - startTime) / 1e6;

        System.out.printf("Spilled Elements: %d, Highest Priority: %d, Total Time: %.3f ms\n", spilled, highestPriority, totalTime);
    }

    private static void fourAryHeap(int[] data) {
        Comparator<Integer> comparator = Comparator.naturalOrder();
        FourAryHeap<Integer> pq = new FourAryHeap<>(M, comparator);
        int spilled = 0;
        Integer highestPriority = null;
        long startTime = System.nanoTime();

        for (int value : data) {
            if (pq.size() < M) {
                pq.insert(value);  // Insert using the "insert" method
            } else if (value > pq.poll()) {
                pq.poll();
                pq.insert(value);  // Re-insert if the current value is larger
                spilled++;
            }
        }

        for (int i = 0; i < REMOVALS && !pq.isEmpty(); i++) {
            highestPriority = pq.poll();  // Remove elements using the "poll" method
        }

        long endTime = System.nanoTime();
        double totalTime = (endTime - startTime) / 1e6;

        System.out.printf("Spilled Elements: %d, Highest Priority: %d, Total Time: %.3f ms\n", spilled, highestPriority, totalTime);
    }

    private static void fourAryHeapWithFloyd(int[] data) {
        Comparator<Integer> comparator = Comparator.naturalOrder();
        FourAryHeap<Integer> pq = new FourAryHeap<>(M, comparator);
        int spilled = 0;
        Integer highestPriority = null;
        long startTime = System.nanoTime();

        pq.buildHeap(Arrays.stream(data).boxed().toArray(Integer[]::new));  // Build the heap using Floyd's trick (bottom-up)

        for (int i = 0; i < REMOVALS && !pq.isEmpty(); i++) {
            highestPriority = pq.poll();  // Remove elements using the "poll" method
        }

        long endTime = System.nanoTime();
        double totalTime = (endTime - startTime) / 1e6;

        System.out.printf("Spilled Elements: %d, Highest Priority: %d, Total Time: %.3f ms\n", spilled, highestPriority, totalTime);
    }

    private static void fibonacciHeap(int[] data) {
        FibonacciHeap<Integer> fibonacciHeap = new FibonacciHeap<>();
        int spilled = 0;
        Integer highestPriority = null;
        long startTime = System.nanoTime();

        for (int value : data) {
            fibonacciHeap.insert(value, value);  // Assuming value as the key for simplicity
            if (fibonacciHeap.getSize() > M) {
                fibonacciHeap.extractMax();
                spilled++;
            }
        }

        for (int i = 0; i < REMOVALS && !fibonacciHeap.isEmpty(); i++) {
            highestPriority = fibonacciHeap.extractMax();
        }

        long endTime = System.nanoTime();
        double totalTime = (endTime - startTime) / 1e6;

        System.out.printf("Spilled Elements: %d, Highest Priority: %d, Total Time: %.3f ms\n", spilled, highestPriority, totalTime);
    }
}
