package algorithms.aStar;

import algorithms.common.Node;

public class aStarNode extends Node {

    int f = 0;  //Total cost of node
    int g = 0;  //Distance between this node and start node
    int h = 0;  //Distance from current node to end
    aStarNode parent;

    public aStarNode (int row, int col, aStarNode parent) {
        super(row, col, parent);
    }
}
