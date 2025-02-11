package com.phasmidsoftware.dsaipg.sort.elementary;

import java.util.Arrays;
import java.util.Random;

public class BenchmarkInsertionSort {
    private static final int NUM_ITERATIONS = 100; // Run multiple iterations for accuracy
    private static final int WARMUP_ITERATIONS = 10; // Warm-up runs to stabilize JVM optimizations

    public static void main(String[] args) {
        int[] sizes = {250, 500, 800, 1000, 1500, 2000};
        System.out.println("Insertion Sort Benchmark (Time in Microseconds)");

        for (int size : sizes) {
            System.out.println("\nBenchmarking for n = " + size);

            benchmark(size, "Random Order", BenchmarkInsertionSort::generateRandomArray);
            benchmark(size, "Ordered", BenchmarkInsertionSort::generateSortedArray);
            benchmark(size, "Partially Ordered", BenchmarkInsertionSort::generatePartiallySortedArray);
            benchmark(size, "Reverse Ordered", BenchmarkInsertionSort::generateReverseSortedArray);
        }
    }

    private static void benchmark(int size, String description, ArrayGenerator generator) {
        // Warm-up phase
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            insertionSort(generator.generate(size));
        }

        long totalTime = 0;

        for (int i = 0; i < NUM_ITERATIONS; i++) {
            int[] array = generator.generate(size);
            long startTime = System.nanoTime();
            insertionSort(array);
            long endTime = System.nanoTime();
            totalTime += (endTime - startTime);
        }

        double avgTime = (totalTime / NUM_ITERATIONS) / 1_000.0; // Convert ns to µs
        System.out.printf("%-20s: %.3f µs\n", description, avgTime);
    }

    private static void insertionSort(int[] array) {
        for (int i = 1; i < array.length; i++) {
            int key = array[i];
            int j = i - 1;
            while (j >= 0 && array[j] > key) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = key;
        }
    }

    @FunctionalInterface
    interface ArrayGenerator {
        int[] generate(int size);
    }

    private static int[] generateRandomArray(int size) {
        Random rand = new Random();
        return rand.ints(size, 1, 10000).toArray();
    }

    private static int[] generateSortedArray(int size) {
        int[] arr = generateRandomArray(size);
        Arrays.sort(arr);
        return arr;
    }

    private static int[] generatePartiallySortedArray(int size) {
        int[] arr = generateSortedArray(size);
        Random rand = new Random();
        for (int i = 0; i < size / 10; i++) {
            int idx1 = rand.nextInt(size);
            int idx2 = rand.nextInt(size);
            int temp = arr[idx1];
            arr[idx1] = arr[idx2];
            arr[idx2] = temp;
        }
        return arr;
    }

    private static int[] generateReverseSortedArray(int size) {
        int[] arr = generateSortedArray(size);
        for (int i = 0, j = size - 1; i < j; i++, j--) {
            int temp = arr[i];
            arr[i] = arr[j];
            arr[j] = temp;
        }
        return arr;
    }
}
