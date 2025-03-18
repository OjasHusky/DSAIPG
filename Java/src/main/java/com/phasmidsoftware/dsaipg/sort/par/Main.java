package Java.src.main.java.com.phasmidsoftware.dsaipg.sort.par;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

/**
 * This code has been optimized and cleaned up.
 * It benchmarks ParSort with varying cutoff values and writes the results to a CSV file.
 */
public class Main {

    public static void main(String[] args) {
        processArgs(args);
        System.out.println("Degree of parallelism: " + ForkJoinPool.getCommonPoolParallelism());

        Random random = new Random();
        int[] array = new int[2000000];
        List<Long> timeList = new ArrayList<>();

        for (int j = 135; j <= 150; j += 1) {
            ParSort.cutoff = j * 1000; // Testing in finer increments
            long totalTime = 0;

            for (int t = 0; t < 20; t++) {
                for (int i = 0; i < array.length; i++) array[i] = random.nextInt(10000000);
                long startTime = System.currentTimeMillis();
                ParSort.sort(array, 0, array.length);
                totalTime += (System.currentTimeMillis() - startTime);
            }

            long avgTime = totalTime / 10;
            timeList.add(avgTime);

            System.out.println("Cutoff: " + ParSort.cutoff + "\tAvg Time: " + avgTime + "ms");
        }

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("C:/Users/misra/Downloads/newResults1.csv")))) {
            int j = 135;
            for (long time : timeList) {
                bw.write((j * 1000 / 2000000.0) + "," + time + "\n");
                j++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processArgs(String[] args) {
        List<String> xs = new ArrayList<>(Arrays.asList(args));
        while (!xs.isEmpty() && xs.get(0).startsWith("-")) {
            xs = Arrays.asList(processArg(xs.toArray(new String[0])));
        }
    }

    private static String[] processArg(String[] xs) {
        if (xs.length < 2) return new String[0]; // Avoid errors
        processCommand(xs[0], xs[1]);
        return Arrays.copyOfRange(xs, 2, xs.length);
    }

    private static void processCommand(String x, String y) {
        if (x.equalsIgnoreCase("N")) setConfig(x, Integer.parseInt(y));
        else if (x.equalsIgnoreCase("P")) System.out.println("Parallelism: " + ForkJoinPool.getCommonPoolParallelism());
    }

    private static void setConfig(String x, int i) {
        configuration.put(x, i);
    }

    private static final Map<String, Integer> configuration = new HashMap<>();
}
