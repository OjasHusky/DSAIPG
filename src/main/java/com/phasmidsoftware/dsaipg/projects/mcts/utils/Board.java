package com.phasmidsoftware.dsaipg.projects.mcts.utils;

import java.awt.*;
import java.util.List;
import java.util.Random;

public interface Board {

    Random RANDOM_GENERATOR = new Random();

    int GAME_IN_PROGRESS = 0;

    int PLAYER_1_WON = 1;

    int PLAYER_2_WON = 2;

    int DRAW = 3;

    int getStatus();

    List<Board> getAllLegalNextMoves();

    int getLatestMovePlayer();

    Board getRandomLegalNextMove();

    Point getLatestMoveCoordinates();

    int[][] getState();

    boolean setSquareOnBoard(Point coordinates, int player);

    void printBoard();


}