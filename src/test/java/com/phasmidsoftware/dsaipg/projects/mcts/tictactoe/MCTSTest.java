package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;


public class MCTSTest {

    @Test
    public void testInitializationAndGameStart() {
        TicTacToe game = new TicTacToe();
        TicTacToeNode initialNode = new TicTacToeNode(game.start());
        MCTS mcts = new MCTS(initialNode);
        assertNotNull(mcts);
        assertEquals("TicTacToe\n. . .\n. . .\n. . .\n", initialNode.state().toString());
    }

    @Test
    public void testSelectMethod() {
        // Initialize the game and root node properly
        TicTacToe game = new TicTacToe();
        TicTacToeNode rootNode = new TicTacToeNode(game.start());
        rootNode.explore();  // Ensure the root node has at least one child to select from, if necessary

        MCTS mcts = new MCTS(rootNode);  // Ensure MCTS is initialized with a non-null root
        Node<TicTacToe> selectedNode = mcts.select(rootNode);

        assertNotNull(selectedNode);  // Check that select does not return null
    }

    @Test
    public void testBestChildSelection() {
        TicTacToeNode rootNode = new TicTacToeNode(new TicTacToe().start());
        rootNode.explore();  // To ensure there are children to select from
        Node<TicTacToe> bestChild = new MCTS(rootNode).bestChild(rootNode);
        assertNotNull(bestChild);
        assertTrue(rootNode.children().contains(bestChild));
    }

    @Test
    public void testSimulationAndBackPropagation() {
        TicTacToeNode node = new TicTacToeNode(new TicTacToe().start());
        MCTS mcts = new MCTS(node);
        int initialPlayouts = node.playouts();
        int result = mcts.simulate(node);
        mcts.backPropagate(node, result);
        assertEquals(initialPlayouts + 1, node.playouts());
    }

}