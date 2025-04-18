package com.phasmidsoftware.dsaipg.projects.mcts.algorithm;

import com.phasmidsoftware.dsaipg.projects.mcts.connectfour.Main;
import com.phasmidsoftware.dsaipg.projects.mcts.utils.Board;

import java.time.Instant;

public class MctsC4 {

    private final Board board;
    private final int playerId;
    private final int opponentId;
    private final int computations;

    public MctsC4(Board board, int playerId, int computations) {
        this.board = board;
        this.playerId = playerId;
        this.opponentId = (playerId == 1) ? 2 : 1; // Ensure that player IDs are correct
        this.computations = computations;
    }

    public Board doMcts() {
        Instant start = Instant.now();
        long counter = 0;
        NodeC4 tree = new NodeC4(board);

        while (counter < computations) {
            counter++;

            // SELECT
            NodeC4 promisingNode = selectPromisingNode(tree);

            // EXPAND
            NodeC4 selected = promisingNode;
            if (selected.board.getStatus() == Board.GAME_IN_PROGRESS) {
                selected = expandNodeAndReturnRandom(promisingNode);
            }

            // SIMULATE
            int playoutResult = simulateLightPlayout(selected);

            // BACKPROPAGATION
            backPropagation(playoutResult, selected);
        }

        NodeC4 best = tree.getChildWithMaxScore();
        Instant end = Instant.now();
        long millis = end.toEpochMilli() - start.toEpochMilli();
        Main.benchmark.add(millis);

        System.out.println("Did " + counter + " expansions/simulations within " + millis + " millis");
        System.out.println("Best move scored " + best.score + " and was visited " + best.visits + " times");

        return best.board;
    }

    public NodeC4 expandNodeAndReturnRandom(NodeC4 node) {
        NodeC4 result = node;
        Board board = node.board;
        for (Board move : board.getAllLegalNextMoves()) {
            NodeC4 child = new NodeC4(move);
            child.parent = node;
            node.addChild(child);
            result = child;
        }
        return node.children.get(Board.RANDOM_GENERATOR.nextInt(node.children.size()));
    }

    public void backPropagation(int playerNumber, NodeC4 selected) {
        NodeC4 node = selected;
        while (node != null) {
            node.visits++;
            if (node.board.getLatestMovePlayer() == playerNumber) {
                node.score++; // Increment score if the simulation resulted in a win for the player
            }
            node = node.parent;
        }
    }

    public int simulateLightPlayout(NodeC4 promisingNode) {
        NodeC4 node = new NodeC4(promisingNode.board);
        int boardStatus = node.board.getStatus();
        if (boardStatus == opponentId) {
            return opponentId; // Immediate loss
        }
        while (node.board.getStatus() == Board.GAME_IN_PROGRESS) {
            Board nextMove = node.board.getRandomLegalNextMove();
            NodeC4 child = new NodeC4(nextMove);
            child.parent = node;
            node.addChild(child);
            node = child;
        }
        return node.board.getStatus(); // Return the status of the board at the end of the simulation
    }

    public NodeC4 selectPromisingNode(NodeC4 tree) {
        NodeC4 node = tree;
        while (!node.children.isEmpty()) {
            node = UCT.findBestNodeWithUCT(node);
        }
        return node;
    }
}
