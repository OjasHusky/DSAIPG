package com.phasmidsoftware.dsaipg.projects.mcts.algorithm;

import org.junit.Test;
import static org.junit.Assert.*;

public class UCTTest {

    @Test
    public void testUctValue() {
        double uctValue = UCT.uctValue(100, 50, 10);
        double expectedValue = (50.0 / 10) + 1.41 * Math.sqrt(Math.log(100) / 10);
        assertEquals("UCT value calculation should match expected value", expectedValue, uctValue, 0.0001);
    }

    @Test
    public void testFindBestNodeWithUCTSingleChild() {
        NodeC4 parent = new NodeC4(null);
        parent.visits = 50;

        NodeC4 child1 = new NodeC4(null);
        child1.visits = 5;
        child1.score = 10;

        parent.addChild(child1);

        NodeC4 bestNode = UCT.findBestNodeWithUCT(parent);
        assertSame("findBestNodeWithUCT should return the only child when it's the only option", child1, bestNode);
    }
}
