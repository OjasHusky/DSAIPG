package com.phasmidsoftware.dsaipg.projects.mcts.utils;

public class Round {

    boolean turn;

    public Round() {
        this.turn = false;
    }

    public int nextRound() {
        this.turn = !this.turn;
        return turn ? 1 : 2;
    }
}