package com.phasmidsoftware.dsaipg.projects.mcts.tictactoe;

import com.phasmidsoftware.dsaipg.projects.mcts.core.Move;
import com.phasmidsoftware.dsaipg.projects.mcts.core.Node;
import com.phasmidsoftware.dsaipg.projects.mcts.core.State;

import java.util.*;

public class MCTS {

    public static TicTacToeNode root;

    // Constructor to initialize the root node
    public MCTS(TicTacToeNode root) {
        MCTS.root = root;
    }

    // Run MCTS iterations
    public void run(int iterations) {
        for (int i = 0; i < iterations; i++) {
            Node<TicTacToe> node = select(root);
            int result = simulate(node);
            backPropagate(node, result);
        }
    }

    // Selection step: Select the best child node using UCB1
    Node<TicTacToe> select(Node<TicTacToe> node) {
        while (!node.isLeaf()) {
            if (!node.children().isEmpty()) {
                node = bestChild(node);
            } else {
                node.explore();  // Explore and expand if no child nodes
                return node;
            }
        }
        return node;
    }

    // Choose the best child node based on UCB1 (Upper Confidence Bound for Trees)
    Node<TicTacToe> bestChild(Node<TicTacToe> node) {
        if (node.children().isEmpty()) {
            return null; // No children, return null (you may handle this differently)
        }
        return node.children().stream()
                .max(Comparator.comparingDouble(this::ucb1))
                .orElseThrow(() -> new IllegalStateException("No best child found, but children list is not empty"));
    }

    // Compute the UCB1 value for a given node
    private double ucb1(Node<TicTacToe> node) {
        double c = Math.sqrt(2); // Exploration factor
        Node<TicTacToe> parent = node.getParent();
        if (parent == null) {
            return Double.MAX_VALUE;  // If no parent, prioritize this node (first visit)
        }
        return node.wins() / (double) node.playouts() +
                c * Math.sqrt(Math.log(parent.playouts()) / (double) node.playouts());
    }

    // Simulate a random game to a terminal state and return the result (1 for X wins, -1 for O wins, 0 for draw)
    int simulate(Node<TicTacToe> node) {
        State<TicTacToe> state = node.state();
        Random random = new Random();

        // Ensure that there are valid moves before selecting
        while (!state.isTerminal()) {
            List<Move<TicTacToe>> moves = new ArrayList<>(state.moves(state.player()));
            if (moves.isEmpty()) {
                break; // No valid moves, end the simulation
            }
            Move<TicTacToe> move = moves.get(random.nextInt(moves.size()));
            state = state.next(move);
        }
        return state.winner().orElse(-1); // Return winner (-1 = O wins, 1 = X wins, 0 = draw)


    }

    // Backpropagate the result of a simulation up the tree
    void backPropagate(Node<TicTacToe> node, int result) {
        while (node != null) {
            // Increment playouts for the current node
            node.setPlayouts(node.playouts() + 1);

            // Update wins based on the result of the simulation
            if ((node.state().player() == 0 && result == 1) ||  // X wins and it's X's move
                   (node.state().player() == 1 && result == 0)) { // O wins and it's O's move
                node.setWins(node.wins() + 1);
            } else if (result == -1) { // Draw
               node.setWins(node.wins() + 0); // Optionally update for a draw
           }

          //  if (result == node.state().player()) {
           //     node.setWins(node.wins() + 1); // 如果当前节点玩家就是赢家，加分
            //} else if (result == 0) {
             //   node.setWins(node.wins() + 1); // 平局也可给1分（可调整）
            //}
            node = node.getParent();  // Move up to the parent node for backpropagation
        }
    }
}
