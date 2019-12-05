package algorithms.dijkstra;

import algorithms.common.Node;

public class DijkstraNode extends Node {

    protected int weight;
    protected boolean visited;

    protected DijkstraNode(int row, int col, DijkstraNode parent) {
        super(row, col, parent);
        visited = false;
        weight = Integer.MAX_VALUE;
    }
}
