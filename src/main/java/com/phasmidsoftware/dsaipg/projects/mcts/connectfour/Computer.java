package com.phasmidsoftware.dsaipg.projects.mcts.connectfour;


import com.phasmidsoftware.dsaipg.projects.mcts.algorithm.MctsC4;
import com.phasmidsoftware.dsaipg.projects.mcts.utils.Board;
import com.phasmidsoftware.dsaipg.projects.mcts.utils.Player;

public class Computer implements Player {

    private final int computations;

    private int playerId; // should be 1 for player 1 or 2 for player 2

    public Computer(int id, int computations) {
        this.computations = computations;
        this.playerId = id;
    }

    public Board makeNextMove(Board currentBoard) {
        System.out.println("Doing MCTS.");
        MctsC4 mcts = new MctsC4(currentBoard, playerId, computations);
        Board winningMove = mcts.doMcts();
        System.out.println("MCTS done.");

        currentBoard.setSquareOnBoard(winningMove.getLatestMoveCoordinates(), playerId);
        return winningMove;
    }
}