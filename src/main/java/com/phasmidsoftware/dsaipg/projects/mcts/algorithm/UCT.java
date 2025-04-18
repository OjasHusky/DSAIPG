package com.phasmidsoftware.dsaipg.projects.mcts.algorithm;

import java.util.Collections;
import java.util.Comparator;

public class UCT {
    public static double uctValue(
            int totalVisit, double nodeWinScore, int nodeVisit) {
        if (nodeVisit == 0) {
            return Integer.MAX_VALUE;
        }
        return ((double) nodeWinScore / (double) nodeVisit)
                + 1.41 * Math.sqrt(Math.log(totalVisit) / (double) nodeVisit);
    }

    public static NodeC4 findBestNodeWithUCT(NodeC4 node) {
        int parentVisit = node.visits;
        return Collections.max(
                node.children,
                Comparator.comparing(c -> uctValue(parentVisit,
                        c.score, c.visits)));
    }
}