package com.phasmidsoftware.dsaipg.projects.mcts.algorithm;

import com.phasmidsoftware.dsaipg.projects.mcts.utils.Board;
import org.junit.Test;

import java.awt.*;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class NodeC4Test {

    // We'll create a simple concrete implementation of the Board interface for testing
    static class MockBoard implements Board {
        @Override
        public int getStatus() {
            return 0; // Example status
        }

        @Override
        public Board getRandomLegalNextMove() {
            return this;
        }

        @Override
        public Point getLatestMoveCoordinates() {
            return null;
        }

        @Override
        public int[][] getState() {
            return new int[0][];
        }

        @Override
        public boolean setSquareOnBoard(Point coordinates, int player) {
            return false;
        }

        @Override
        public void printBoard() {

        }

        @Override
        public List<Board> getAllLegalNextMoves() {
            return Collections.singletonList(this);
        }

        @Override
        public int getLatestMovePlayer() {
            return 1; // Assume player 1 is the latest player
        }
    }

    @Test
    public void testAddChild() {
        NodeC4 parent = new NodeC4(new MockBoard());
        NodeC4 child = new NodeC4(new MockBoard());

        parent.addChild(child);

        assertTrue("Parent should contain the added child", parent.children.contains(child));
        assertEquals("Child's parent should be set to parent", parent, child.parent);
    }

    @Test
    public void testGetChildWithMaxScore() {
        NodeC4 parent = new NodeC4(new MockBoard());
        NodeC4 child1 = new NodeC4(new MockBoard());
        NodeC4 child2 = new NodeC4(new MockBoard());

        // Setting scores to test getChildWithMaxScore method
        child1.score = 10;
        child2.score = 20;

        parent.addChild(child1);
        parent.addChild(child2);

        NodeC4 maxScoreChild = parent.getChildWithMaxScore();

        assertEquals("Child with the highest score should be returned", child2, maxScoreChild);
    }

    @Test
    public void testGetChildWithMaxScoreSingleChild() {
        NodeC4 parent = new NodeC4(new MockBoard());
        NodeC4 child = new NodeC4(new MockBoard());

        child.score = 10;
        parent.addChild(child);

        NodeC4 maxScoreChild = parent.getChildWithMaxScore();

        assertSame("The only child should be returned as the child with max score", child, maxScoreChild);
    }

    @Test
    public void testGetChildWithMaxScoreEmpty() {
        NodeC4 parent = new NodeC4(new MockBoard());

        try {
            NodeC4 maxScoreChild = parent.getChildWithMaxScore();
            fail("Should have thrown an exception when no children are present");
        } catch (IndexOutOfBoundsException e) {
            // Expected exception - do nothing here
        }
    }
}
