package com.phasmidsoftware.dsaipg.projects.mcts.utils;


public class AbstractBoard {

    public int latestMoveByPlayer = 2; // the number of the player who set the last piece

    public int getLatestMovePlayer() {
        return latestMoveByPlayer;
    }
}