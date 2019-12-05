package algorithms.aStar;

import algorithms.common.Node;

public class AStarNode extends Node {

    protected int f = 0;  //Total cost of node
    protected int g = 0;  //Distance between this node and start node
    protected int h = 0;  //Distance from current node to end

    protected AStarNode(int row, int col, AStarNode parent) {
        super(row, col, parent);
    }
}
