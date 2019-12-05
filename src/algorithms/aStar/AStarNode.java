package algorithms.aStar;

import algorithms.common.Node;

public class AStarNode extends Node {

    int f = 0;  //Total cost of node
    int g = 0;  //Distance between this node and start node
    int h = 0;  //Distance from current node to end
    AStarNode parent;

    public AStarNode(int row, int col, AStarNode parent) {
        super(row, col, parent);
    }
}
