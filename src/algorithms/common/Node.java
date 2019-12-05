package algorithms.common;

/**
 * A class to represent a node in the graph
 */
public class Node {

    private int row, col;
    public Node parent;
    public int weight;      //Cost to get to this node

    public Node (int row, int col, Node parent) {
        this.row = row;
        this.col = col;
        this.parent = parent;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}
