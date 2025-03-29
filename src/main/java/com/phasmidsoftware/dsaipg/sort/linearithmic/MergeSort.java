/*
 * Copyright (c) 2024. Robin Hillyard
 */

package com.phasmidsoftware.dsaipg.sort.linearithmic;

import com.phasmidsoftware.dsaipg.sort.Helper;
import com.phasmidsoftware.dsaipg.sort.SortException;
import com.phasmidsoftware.dsaipg.sort.SortWithComparableHelper;
import com.phasmidsoftware.dsaipg.sort.elementary.InsertionSort;
import com.phasmidsoftware.dsaipg.util.Config;

import java.util.Arrays;

import static com.phasmidsoftware.dsaipg.util.Config_Benchmark.*;

public class MergeSort<X extends Comparable<X>> extends SortWithComparableHelper<X> {

    public static final String DESCRIPTION = "MergeSort";

    /**
     * Constructor for MergeSort
     * <p>
     * NOTE this is used only by unit tests, using its own instrumented helper.
     *
     * @param helper an explicit instance of Helper to be used.
     */
    public MergeSort(Helper<X> helper) {
        super(helper);
        insertionSort = setupInsertionSort(helper);
    }

    /**
     * Constructor for MergeSort
     *
     * @param N      the number elements we expect to sort.
     * @param nRuns  the expected number of runs.
     * @param config the configuration.
     */
    public MergeSort(int N, int nRuns, Config config) {
        super(DESCRIPTION + getConfigString(config), N, nRuns, config);
        insertionSort = setupInsertionSort(getHelper());
    }

    /**
     * Sorts the given array in-place or by creating a copy, depending on the parameter makeCopy.
     * This method initializes a sorting helper, allocates additional memory as necessary,
     * and performs the sort.
     *
     * @param xs       the array to be sorted
     * @param makeCopy if true, the array will be copied before sorting. Otherwise, sorting is done in-place.
     * @return the sorted array; either the modified original array (if makeCopy is false) or a new sorted array (if makeCopy is true)
     */
    public X[] sort(X[] xs, boolean makeCopy) {
        getHelper().init(xs.length);
        additionalMemory(xs.length);
        X[] result = makeCopy ? Arrays.copyOf(xs, xs.length) : xs;
        sort(result, 0, result.length);
        additionalMemory(-xs.length);
        return result;
    }

    /**
     * Sorts the specified portion of the array using the MergeSort algorithm.
     *
     * @param a    the array to be sorted
     * @param from the starting index of the range to sort, inclusive
     * @param to   the ending index of the range to sort, exclusive
     */
    public void sort(X[] a, int from, int to) {
        Config config = helper.getConfig();
        boolean noCopy = config.getBoolean(MERGESORT, NOCOPY);
        // CONSIDER don't copy but just allocate according to the xs/aux interchange optimization
        @SuppressWarnings("unchecked") X[] aux = noCopy ? helper.copyArray(a) : (X[]) new Comparable[a.length];
        sort(a, aux, from, to);
    }

    /**
     * Sets the memory for the array if it hasn't been set previously.
     * If arrayMemory has not been initialized (i.e., equals -1), it sets its value to n.
     * Additionally, allocates and updates additional memory using the additionalMemory method.
     *
     * @param n the amount of memory to be set for the array.
     */
    public void setArrayMemory(int n) {
        if (arrayMemory == -1) {
            arrayMemory = n;
            additionalMemory(n);
        }
    }

    /**
     * Updates the value of additionalMemory by adding the provided amount and adjusts maxMemory if necessary.
     *
     * @param n the amount of memory to be added to additionalMemory.
     */
    public void additionalMemory(int n) {
        additionalMemory += n;
        if (maxMemory < additionalMemory) maxMemory = additionalMemory;
    }

    /**
     * Computes the memory factor, which represents the ratio of the maximum memory available
     * to the array memory size. This calculation helps in determining the efficiency or feasibility
     * of operations concerning memory usage.
     *
     * @return the memory factor as a Double.
     * Throws a SortException if the array memory has not been set (i.e., arrayMemory == -1).
     */
    public Double getMemoryFactor() {
        if (arrayMemory == -1)
            throw new SortException("Array memory has not been set");
        return 1.0 * maxMemory / arrayMemory;
    }

    /**
     * Sets up an instance of InsertionSort using a cloned helper with a specific description.
     *
     * @param helper an instance of Helper to be cloned and used by the InsertionSort instance.
     * @return an instance of InsertionSort configured with the cloned helper.
     */
    private InsertionSort<X> setupInsertionSort(final Helper<X> helper) {
        return new InsertionSort<>(helper.clone("MergeSort: insertion sort"));
    }

    /**
     * Sorts the given range of the array using merge sort with optional optimizations for insurance and no-copy.
     *
     * @param a    the primary array used for sorting.
     * @param aux  the auxiliary array used for intermediate storage during sorting.
     * @param from the starting index (inclusive) of the range to be sorted.
     * @param to   the ending index (exclusive) of the range to be sorted.
     */
    private void sort(X[] a, X[] aux, int from, int to) {
        Config config = helper.getConfig();
        boolean insurance = config.getBoolean(MERGESORT, INSURANCE);
        boolean noCopy = config.getBoolean(MERGESORT, NOCOPY);

        // If subarray is at or below the insertion-sort cutoff, do insertion sort:
        if (to - from <= helper.cutoff()) {
            insertionSort.sort(a, from, to);
            return;
        }

        int mid = from + (to - from) / 2;
        sort(a, aux, from, mid);
        sort(a, aux, mid, to);

        // If insurance is enabled and a[mid-1] <= a[mid], skip the merge.
        if (insurance && helper.less(a, mid - 1, mid)) {
            return;
        }

        // Merge from a[] into aux[]:
        merge(a, aux, from, mid, to);

        // Copy merged result from aux[] back to a[]:
        System.arraycopy(aux, from, a, from, to - from);

        // ***** Add instrumentation for that final copy: *****
        helper.incrementCopies(to - from);
        helper.incrementHits(2L * (to - from));
    }

    /**
     * Merges two sorted subarrays into a combined sorted array.
     * The first subarray is defined as [from, mid) and the second subarray as [mid, to).
     * Elements from these subarrays are copied into the result array in sorted order.
     * CONSIDER combine with MergeSortBasic, perhaps.
     *
     * @param sorted The source array containing the two sorted subarrays to be merged.
     * @param result The destination array where the merged results will be stored.
     * @param from   The starting index (inclusive) of the first subarray.
     * @param mid    The ending index (exclusive) of the first subarray and the starting index of the second subarray.
     * @param to     The ending index (exclusive) of the second subarray.
     */
    private void merge(X[] sorted, X[] result, int from, int mid, int to) {
        int i = from;
        int j = mid;
        X v = helper.get(sorted, i);
        X w = helper.get(sorted, j);
        for (int k = from; k < to; k++) {
            if (i >= mid) {
                helper.copy(w, result, k);
                if (++j < to) w = helper.get(sorted, j);
            }
            else if (j >= to) {
                helper.copy(v, result, k);
                if (++i < mid) v = helper.get(sorted, i);
            }
            else if (helper.less(w, v)) {
                // For insurance, we do the fix count:
                helper.incrementFixes(mid - i);
                helper.copy(w, result, k);
                if (++j < to) w = helper.get(sorted, j);
            }
            else {
                helper.copy(v, result, k);
                if (++i < mid) v = helper.get(sorted, i);
            }
        }
    }

    public static final String MERGESORT = "mergesort";
    public static final String NOCOPY = "nocopy";
    public static final String INSURANCE = "insurance";

    /**
     * Builds a configuration string based on the provided configuration settings.
     * The configuration string describes certain properties such as whether
     * insurance comparison, no-copy, or specific cutoff values are enabled.
     *
     * @param config the configuration object used to determine the settings for the string.
     * @return a string representing the configuration settings.
     */
    private static String getConfigString(Config config) {
        StringBuilder stringBuilder = new StringBuilder();
        if (config.getBoolean(MERGESORT, INSURANCE)) stringBuilder.append(" with insurance comparison");
        if (config.getBoolean(MERGESORT, NOCOPY)) stringBuilder.append(" with no copy");
        int cutoff = config.getInt(HELPER, CUTOFF, CUTOFF_DEFAULT);
        if (cutoff != CUTOFF_DEFAULT) {
            if (cutoff == 1) stringBuilder.append(" with no cutoff");
            else stringBuilder.append(" with cutoff ").append(cutoff);
        }
        return stringBuilder.toString();
    }

    private final InsertionSort<X> insertionSort;
    private int arrayMemory = -1;
    private int additionalMemory;
    private int maxMemory;
}


///**
// * Class MergeSort.
// *
// * @param <X> the underlying comparable type.
// */
//public class MergeSort<X extends Comparable<X>> extends SortWithComparableHelper<X> {
//
//    public static final String DESCRIPTION = "MergeSort";
//
//    /**
//     * Constructor for MergeSort
//     * <p>
//     * NOTE this is used only by unit tests, using its own instrumented helper.
//     *
//     * @param helper an explicit instance of Helper to be used.
//     */
//    public MergeSort(Helper<X> helper) {
//        super(helper);
//        insertionSort = setupInsertionSort(helper);
//    }
//
//    /**
//     * Constructor for MergeSort
//     *
//     * @param N      the number elements we expect to sort.
//     * @param nRuns  the expected number of runs.
//     * @param config the configuration.
//     */
//    public MergeSort(int N, int nRuns, Config config) {
//        super(DESCRIPTION + getConfigString(config), N, nRuns, config);
//        insertionSort = setupInsertionSort(getHelper());
//    }
//
//    private InsertionSort<X> setupInsertionSort(final Helper<X> helper) {
//        return new InsertionSort<>(helper.clone("MergeSort: insertion sort"));
//    }
//
//    public X[] sort(X[] xs, boolean makeCopy) {
//        getHelper().init(xs.length);
//        additionalMemory(xs.length);
//        X[] result = makeCopy ? Arrays.copyOf(xs, xs.length) : xs;
//        sort(result, 0, result.length);
//        additionalMemory(-xs.length);
//        return result;
//    }
//
//    public void sort(X[] a, int from, int to) {
//        Config config = helper.getConfig();
//        boolean noCopy = config.getBoolean(MERGESORT, NOCOPY);
//        // CONSIDER don't copy but just allocate according to the xs/aux interchange optimization
//        @SuppressWarnings("unchecked") X[] aux = noCopy ? helper.copyArray(a) : (X[]) new Comparable[a.length];
//        sort(a, aux, from, to);
//    }
//
////    private void sort(X[] a, X[] aux, int from, int to) {
////        Config config = helper.getConfig();
////        boolean insurance = config.getBoolean(MERGESORT, INSURANCE);
////        boolean noCopy = config.getBoolean(MERGESORT, NOCOPY);
////        if (to <= from + helper.cutoff()) { // XXX check that a cutoff value of 1 effectively stops the cutoff mechanism.
////            insertionSort.sort(a, from, to);
////            return;
////        }
////
////        // TO BE IMPLEMENTED  : implement merge sort with insurance and no-copy optimizations
////
////        int mid = (from + to) / 2;
////        sort(a, aux, from, mid);  // Sort the first half
////        sort(a, aux, mid, to);    // Sort the second half
////
////        // After sorting, merge the two halves
////        merge(a, aux, from, mid, to);
////
////
////}
////
////    // CONSIDER combine with MergeSortBasic, perhaps.
////    private void merge(X[] sorted, X[] result, int from, int mid, int to) {
////        int i = from;
////        int j = mid;
////        X v = helper.get(sorted, i);
////        X w = helper.get(sorted, j);
////        for (int k = from; k < to; k++) {
////            if (i >= mid) {
////                helper.copy(w, result, k);
////                if (++j < to) w = helper.get(sorted, j);
////            } else if (j >= to) {
////                helper.copy(v, result, k);
////                if (++i < mid) v = helper.get(sorted, i);
////            } else if (helper.less(w, v)) {
////                helper.incrementFixes(mid - i);
////                helper.copy(w, result, k);
////                if (++j < to) w = helper.get(sorted, j);
////            } else {
////                helper.copy(v, result, k);
////                if (++i < mid) v = helper.get(sorted, i);
////            }
////        }
////    }
////private void sort(X[] a, X[] aux, int from, int to) {
////    Config config = helper.getConfig();
////    boolean insurance = config.getBoolean(MERGESORT, INSURANCE);
////    boolean noCopy = config.getBoolean(MERGESORT, NOCOPY);
////
////    if (to <= from + helper.cutoff()) {
////        insertionSort.sort(a, from, to);
////        return;
////    }
////
////    int mid = (from + to) / 2;
////
////    sort(a, aux, from, mid);
////    sort(a, aux, mid, to);
////
////    if (noCopy) {
////        mergeNoCopy(a, from, mid, to);
////    } else {
////        merge(a, aux, from, mid, to);
////    }
////}
////
////    private void mergeNoCopy(X[] a, int from, int mid, int to) {
////        int i = from;
////        int j = mid;
////        X v = helper.get(a, i);
////        X w = helper.get(a, j);
////        for (int k = from; k < to; k++) {
////            if (i >= mid) {
////                helper.copy(w, a, k);  // Copy from the right side to the original array
////                if (++j < to) w = helper.get(a, j);
////            } else if (j >= to) {
////                helper.copy(v, a, k);  // Copy from the left side to the original array
////                if (++i < mid) v = helper.get(a, i);
////            } else if (helper.less(w, v)) {
////                helper.incrementFixes(mid - i);
////                helper.copy(w, a, k); // Copy the smaller element from the right side
////                if (++j < to) w = helper.get(a, j);
////            } else {
////                helper.copy(v, a, k); // Copy the smaller element from the left side
////                if (++i < mid) v = helper.get(a, i);
////            }
////        }
////    }
//
//
//    /**
//     * Sorts the given range of the array using merge sort with optional
//     * optimizations for insurance and no-copy.
//     *
//     * @param a    the primary array used for sorting.
//     * @param aux  the auxiliary array used for intermediate storage during sorting.
//     * @param from the starting index (inclusive) of the range to be sorted.
//     * @param to   the ending index (exclusive) of the range to be sorted.
//     */
//    private void sort(X[] a, X[] aux, int from, int to) {
//        // Grab our two config flags:
//        Config config = helper.getConfig();
//        boolean insurance = config.getBoolean(MERGESORT, INSURANCE);
//        boolean noCopy = config.getBoolean(MERGESORT, NOCOPY);
//
//        // Dispatch to the appropriate sort routine:
//        if (noCopy) {
//            // "Role-switching" mergesort that avoids extra copying.
//            sortNoCopy(a, aux, from, to, insurance);
//        }
//        else {
//            // Standard mergesort that copies from 'a' to 'aux' before merging back to 'a'.
//            sortWithCopy(a, aux, from, to, insurance);
//        }
//    }
//
//    /**
//     * Standard top-down mergesort that copies from the primary array (a)
//     * into the auxiliary array (aux) for each merge step. The final merge
//     * writes sorted output back into 'a'.
//     */
//    private void sortWithCopy(X[] a, X[] aux, int from, int to, boolean insurance) {
//        // Base case: use insertion sort on small ranges.
//        if (to - from <= helper.cutoff()) {
//            insertionSort.sort(a, from, to);
//            return;
//        }
//        int mid = from + (to - from) / 2;
//
//        // Sort the left and right halves (in 'a' itself).
//        sortWithCopy(a, aux, from, mid, insurance);
//        sortWithCopy(a, aux, mid, to, insurance);
//
//        // If "insurance" is on, skip merge if already in order:
//        // i.e. a[mid-1] <= a[mid].
//        if (insurance && !helper.less(a[mid], a[mid - 1])) {
//            return;
//        }
//
//        // Copy subrange [from..to) from 'a' into 'aux'.
//        for (int i = from; i < to; i++) {
//            aux[i] = a[i];
//        }
//
//        // Merge from aux -> a.
//        merge(aux, a, from, mid, to);
//    }
//
//    /**
//     * "No-copy" merge sort. We do not re-copy before every merge step; instead
//     * we switch the roles of the primary and auxiliary arrays at each level of
//     * recursion. The final merge writes sorted output into 'resultArray'.
//     */
//    private void sortNoCopy(X[] a, X[] aux, int from, int to, boolean insurance) {
//        if (to - from <= helper.cutoff()) {
//            insertionSort.sort(a, from, to);
//            return;
//        }
//        int mid = from + (to - from) / 2;
//
//        sortNoCopy(aux, a, from, mid, insurance);
//        sortNoCopy(aux, a, mid, to, insurance);
//
//        if (insurance && !helper.less(aux[mid], aux[mid - 1])) {
//            System.arraycopy(aux, from, a, from, to - from);
//            return;
//        }
//
//        merge(aux, a, from, mid, to);
//    }
//
//    private void merge(X[] a, X[] aux, int from, int mid, int to) {
//        int i = from;
//        int j = mid;
//        X v = helper.get(a, i);
//        X w = helper.get(a, j);
//
//        for (int k = from; k < to; k++) {
//            if (i >= mid) {
//                helper.copy(w, aux, k);
//                if (++j < to) w = helper.get(a, j);
//            } else if (j >= to) {
//                helper.copy(v, aux, k);
//                if (++i < mid) v = helper.get(a, i);
//            } else if (helper.less(w, v)) {
//                helper.incrementFixes(mid - i);
//                helper.copy(w, aux, k);
//                if (++j < to) w = helper.get(a, j);
//            } else {
//                helper.copy(v, aux, k);
//                if (++i < mid) v = helper.get(a, i);
//            }
//        }
//
//        for (int k = from; k < to; k++) {
//            helper.copy(aux[k], a, k);
//        }
//    }
//
//
//    public static final String MERGESORT = "mergesort";
//    public static final String NOCOPY = "nocopy";
//    public static final String INSURANCE = "insurance";
//
//    private static String getConfigString(Config config) {
//        StringBuilder stringBuilder = new StringBuilder();
//        if (config.getBoolean(MERGESORT, INSURANCE)) stringBuilder.append(" with insurance comparison");
//        if (config.getBoolean(MERGESORT, NOCOPY)) stringBuilder.append(" with no copy");
//        int cutoff = config.getInt(HELPER, CUTOFF, CUTOFF_DEFAULT);
//        if (cutoff != CUTOFF_DEFAULT) {
//            if (cutoff == 1) stringBuilder.append(" with no cutoff");
//            else stringBuilder.append(" with cutoff ").append(cutoff);
//        }
//        return stringBuilder.toString();
//    }
//
//    private final InsertionSort<X> insertionSort;
//
//
//    private int arrayMemory = -1;
//    private int additionalMemory;
//    private int maxMemory;
//
//    public void setArrayMemory(int n) {
//        if (arrayMemory == -1) {
//            arrayMemory = n;
//            additionalMemory(n);
//        }
//    }
//
//    public void additionalMemory(int n) {
//        additionalMemory += n;
//        if (maxMemory < additionalMemory) maxMemory = additionalMemory;
//    }
//
//    public Double getMemoryFactor() {
//        if (arrayMemory == -1)
//            throw new SortException("Array memory has not been set");
//        return 1.0 * maxMemory / arrayMemory;
//    }
//
//}
/**
 * A generic implementation of the MergeSort algorithm for sorting elements of type X,
 * where X extends Comparable<X>. This class provides optimized sorting techniques such as
 * insurance and no-copy optimizations, offering scalable sorting solutions. It makes use of
 * a helper class for monitoring and performing additional utilities during the sorting process.
 *
 * @param <X> The type of elements to be sorted, which must implement the Comparable interface.
 */
