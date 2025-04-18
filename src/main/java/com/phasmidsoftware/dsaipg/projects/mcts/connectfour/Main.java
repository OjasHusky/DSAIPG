package com.phasmidsoftware.dsaipg.projects.mcts.connectfour;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {
    public static List<Long> benchmark = new ArrayList<>();
    static String filePath = "benchmark_2.txt";
    public static void main(String[] args) {
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            long totalBenchmarkTime = 0l;
            int iters = 1;     // set number of iterations

            for(int i=0; i<iters; i++) {
                Game connectFour = new Game(new ConnectFourBoard(7),
                        new Computer(1, 4000),
                        new HumanPlayer(2)
                );
                connectFour.play();

                // Show benchmarks for the game
                Long totalBenchmark = 0l;
                for(Long b: benchmark) {
                    totalBenchmark += b;
                }
                System.out.println("All MCTS operations took: " + totalBenchmark + " milliseconds");
                totalBenchmarkTime += totalBenchmark;
                benchmark.clear();
            }
            System.out.println("Average time taken by MCTS to play game over all iterations: " + totalBenchmarkTime/iters + " milliseconds");
            writer.write(Long.toString(totalBenchmarkTime/iters));
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
        }
    }
}
