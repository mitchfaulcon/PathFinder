package algorithms.common;

/**
 * A class to represent a node in the graph
 */
public class Node {

    private int row, col;
    protected Node parent;

    public Node (int row, int col, Node parent) {
        this.row = row;
        this.col = col;
        this.parent = parent;
    }

    public int GetRow() {
        return row;
    }

    public int GetCol() {
        return col;
    }

    public Node GetParent() {
        return parent;
    }
}
