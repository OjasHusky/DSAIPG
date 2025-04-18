package com.phasmidsoftware.dsaipg.projects.mcts.algorithm;

import com.phasmidsoftware.dsaipg.projects.mcts.utils.Board;
import org.junit.Test;
import static org.junit.Assert.*;

import java.awt.*;
import java.util.Arrays;
import java.util.Collections;

public class MctsC4Test {

    // Create a simple implementation of the Board interface for testing purposes
    class MockBoard implements Board {
        private int moveCount = 0; // Control the number of moves to avoid infinite loops

        @Override
        public int getStatus() {
            // Return GAME_OVER after a few moves to ensure finite simulations
            return (moveCount++ < 5) ? Board.GAME_IN_PROGRESS : Board.GAME_IN_PROGRESS-1;
        }

        @Override
        public Board getRandomLegalNextMove() {
            // Return null or this to simulate a single move path without creating new instances
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
        public java.util.List<Board> getAllLegalNextMoves() {
            // Return a list with only one move to limit memory usage
            return Collections.singletonList(this);
        }

        @Override
        public int getLatestMovePlayer() {
            return 1; // Assume the latest move was made by player 1
        }
    }

    @Test
    public void testDoMcts() {
        MockBoard board = new MockBoard();
        MctsC4 mcts = new MctsC4(board, 1, 1); // Use a small number of computations to limit processing
        Board resultBoard = mcts.doMcts();
        assertNotNull("doMcts should return a board", resultBoard);
    }

    @Test
    public void testExpandNodeAndReturnRandom() {
        MockBoard board = new MockBoard();
        NodeC4 node = new NodeC4(board);
        MctsC4 mcts = new MctsC4(board, 1, 100);

        NodeC4 result = mcts.expandNodeAndReturnRandom(node);
        assertTrue("The returned node should be one of the children", node.children.contains(result));
    }

    @Test
    public void testBackPropagation() {
        MockBoard board = new MockBoard();
        NodeC4 node = new NodeC4(board);
        NodeC4 childNode = new NodeC4(board);
        node.addChild(childNode);
        childNode.parent = node;

        MctsC4 mcts = new MctsC4(board, 1, 100);
        mcts.backPropagation(1, childNode);

        assertEquals("Parent node visits should be incremented", 1, node.visits);
        assertEquals("Child node visits should be incremented", 1, childNode.visits);
        assertEquals("Score should be incremented if the winning player matches", 1, childNode.score);
    }

    @Test
    public void testSimulateLightPlayout() {
        MockBoard board = new MockBoard();
        NodeC4 node = new NodeC4(board);
        MctsC4 mcts = new MctsC4(board, 1, 20);
        int result = mcts.simulateLightPlayout(node);
        assertTrue("SimulateLightPlayout should return a valid status",
                result == Board.GAME_IN_PROGRESS-1 || result == 1 || result == 2);
    }

    @Test
    public void testSelectPromisingNode() {
        MockBoard board = new MockBoard();
        NodeC4 node = new NodeC4(board);
        MctsC4 mcts = new MctsC4(board, 1, 1);

        NodeC4 result = mcts.selectPromisingNode(node);
        assertSame("SelectPromisingNode should return the node itself when there are no children", node, result);
    }
}



