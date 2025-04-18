package com.phasmidsoftware.dsaipg.projects.mcts.algorithm;

import com.phasmidsoftware.dsaipg.projects.mcts.utils.Board;

import java.util.ArrayList;
import java.util.List;

public class NodeC4 {

    public Board board;

    int visits;

    int score;

    List<NodeC4> children = new ArrayList<>();

    NodeC4 parent = null;

    public NodeC4(Board initBoard) {
        this.board = initBoard;
    }

    NodeC4 getChildWithMaxScore() {
        NodeC4 result = children.get(0);
        for (int i = 1; i < children.size(); i++) {
            if (children.get(i).score > result.score) {
                result = children.get(i);
            }
        }
        return result;
    }

    void addChild(NodeC4 node) {
        children.add(node);
        node.parent = this;
    }
}