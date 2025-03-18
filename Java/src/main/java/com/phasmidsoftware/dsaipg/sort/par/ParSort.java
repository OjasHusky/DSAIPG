package Java.src.main.java.com.phasmidsoftware.dsaipg.sort.par;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

/**
 * ParSort is a class implementing a parallel sorting algorithm.
 * The sorting is executed using a fork-and-join approach,
 * where large arrays are divided into smaller portions and sorted concurrently.
 * Designed to optimize performance for sorting large integer arrays.
 * @author Ziyao Qiao
 */
public final class ParSort {

    /**
     * Cutoff value for switching between parallel and sequential sorting.
     */
    public static int cutoff = 1000;

    /**
     * Sorts the given array in parallel using CompletableFuture.
     *
     * @param array the array to be sorted
     * @param from  the starting index (inclusive)
     * @param to    the ending index (exclusive)
     */
    public static void sort(int[] array, int from, int to) {
        if (to - from >= cutoff) {
            int mid = (from + to) / 2;

            CompletableFuture<int[]> leftFuture = asyncSort(array, from, mid);
            CompletableFuture<int[]> rightFuture = asyncSort(array, mid, to);

            CompletableFuture<int[]> mergedFuture = leftFuture.thenCombine(rightFuture, ParSort::doMerge);


            mergedFuture.whenComplete((result, throwable) -> {
                if (throwable == null) {
                    System.arraycopy(result, 0, array, from, result.length);
                } else {
                    throwable.printStackTrace();
                }
            });

            mergedFuture.join();
        } else {
            Arrays.sort(array, from, to);
        }
    }

    /**
     * Recursively sorts a portion of the array and returns a sorted sub-array.
     *
     * @param array the input array
     * @param from  the starting index (inclusive)
     * @param to    the ending index (exclusive)
     * @return a sorted sub-array
     */
    static int[] sortRecursive(int[] array, int from, int to) {
        if (to - from <= 1) {
            return Arrays.copyOfRange(array, from, to);
        }

        int mid = (from + to) / 2;
        int[] left = sortRecursive(array, from, mid);
        int[] right = sortRecursive(array, mid, to);

        return doMerge(left, right);
    }

    /**
     * Merges two sorted arrays into a single sorted array.
     *
     * @param left  the first sorted sub-array
     * @param right the second sorted sub-array
     * @return a merged sorted array
     */
    static int[] doMerge(int[] left, int[] right) {
        int[] result = new int[left.length + right.length];
        int i = 0, j = 0;

        for (int k = 0; k < result.length; k++) {
            if (i >= left.length) result[k] = right[j++];
            else if (j >= right.length) result[k] = left[i++];
            else if (right[j] < left[i]) result[k] = right[j++];
            else result[k] = left[i++];
        }
        return result;
    }

    /**
     * Asynchronously sorts a portion of the array using parallelism.
     *
     * @param array the input array
     * @param from  the starting index (inclusive)
     * @param to    the ending index (exclusive)
     * @return a CompletableFuture containing the sorted sub-array
     */
    static CompletableFuture<int[]> asyncSort(int[] array, int from, int to) {
        return CompletableFuture.supplyAsync(() -> sortRecursive(array, from, to), ForkJoinPool.commonPool());
    }
}
