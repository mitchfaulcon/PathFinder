package algorithms.biDirectional;

import algorithms.common.Algorithm;
import algorithms.common.Node;
import controller.Tile;

import java.util.ArrayList;
import java.util.Collections;

public class BiDirectionalAlgorithm extends Algorithm {

    public BiDirectionalAlgorithm(int[][] map, int[] start, int[] end, Tile[][] tileMap) {
        super(map, start, end, tileMap);
    }

    /**
     * This algorithm runs from the end point and start point at the same time, with each branching out towards the other.
     * Runs like the Dijkstra algorithm but from both directions.
     */
    public void startAlgorithm() {

        ArrayList<Node> closedList = new ArrayList<>();
        ArrayList<Node> startOpenList = new ArrayList<>();
        ArrayList<Node> endOpenList = new ArrayList<>();

        Node startNode = new Node(start[0], start[1], null);
        startNode.weight = 0;
        startOpenList.add(startNode);

        Node endNode = new Node(end[0], end[1], null);
        endNode.weight = 0;
        endOpenList.add(endNode);

        while (!startOpenList.isEmpty() || !endOpenList.isEmpty()) {        //This could be && or ||    || will stop once both lists are empty
                                                                            //                          && will stop once either list is empty
            if (!startOpenList.isEmpty()) {
                if (iterateList(startOpenList, closedList, endNode)) return;
            }

            if (!endOpenList.isEmpty()) {
                if (iterateList(endOpenList, closedList, startNode)) return;
            }
        }

        //If while loop ends, no path was found

        //Only display searched nodes
        visualise(closedList, null);
    }

    /**
     * Method to iterate through the next node in an open list
     * @return True if a path between the two nodes gets found in this iteration
     *         False if the iteration completes with no path getting found
     */
    private boolean iterateList(ArrayList<Node> openList, ArrayList<Node> closedList, Node goalNode) {
        //Get node with the smallest cost
        Node currentNode = openList.get(0);
        for (Node node : openList) {
            if (node.weight < currentNode.weight) {
                currentNode = node;
            }
        }

        //Remove current node from open list
        openList.remove(currentNode);

        //Check if there is any matching nodes in closed list
        for (Node closedNode : closedList) {
            if (closedNode.getRow() == currentNode.getRow() && closedNode.getCol() == currentNode.getCol()) {
                //The two searches have reached each other
                pathFound(closedList, currentNode, closedNode);
                return true;
            }
        }

        //Generate children nodes
        ArrayList<Node> children = new ArrayList<>();
        for (int[] newPosition : NEIGHBOUR_POSITIONS){
            //Get node position
            int[] nodePosition = {currentNode.getRow() + newPosition[0], currentNode.getCol() + newPosition[1]};

            //Make sure node position is within bounds
            if (nodePosition[0] > map.length - 1 || nodePosition[0] < 0 || nodePosition[1] > map[0].length - 1 || nodePosition[1] < 0) {
                continue;
            }

            //Make sure position is not a wall
            if (map[nodePosition[0]][nodePosition[1]] == -1) continue;

            //Add new node to children
            children.add(new Node(nodePosition[0], nodePosition[1], currentNode));
        }

        //Loop through found children
        childLoop: for (Node child : children) {

            //End was found
            if (child.getRow() == goalNode.getRow() && child.getCol() == goalNode.getCol()) {
                //Display searched/path tiles
                closedList.add(currentNode);
                ArrayList<Node> path = buildPath(child);      //Want to use normal build path if algorithms didn't meet in the middle
                if (goalNode.getRow() == start[0] && goalNode.getCol() == start[1]) Collections.reverse(path);      //Need to reverse if we reached the start from the end
                visualise(closedList, path);
                return true;
            }

            //Check if child is in closed list
            for (Node closedNode : closedList) {
                if (child.getRow() == closedNode.getRow() && child.getCol() == closedNode.getCol()) continue childLoop;
            }

            //Calculate weight of child
            child.weight = currentNode.weight + map[child.getRow()][child.getCol()];

            for (Node openNode : openList) {
                if (child.getRow() == openNode.getRow() && child.getCol() == openNode.getCol()) {
                    //Skip this child if it has already been added with a lower total cost
                    if (openNode.weight < child.weight) continue childLoop;
                    else {
                        //Child has a lower cost than the one already in the list
                        openList.remove(openNode);
                        break;
                    }
                }
            }

            //Add child to open list
            openList.add(child);
        }

        closedList.add(currentNode);

        return false;
    }

    private void pathFound(ArrayList<Node> closedList, Node lastNode1, Node lastNode2) {

        //Build paths in both directions
        ArrayList<Node> path1 = buildPath(lastNode1);
        ArrayList<Node> path2 = buildPath(lastNode2);

        //Reverse one of the paths so that they can be connected correctly
        Collections.reverse(path2);
        path2.remove(0);    //Remove the duplicate node

        path1.addAll(path2);

        //Check if first node in path is the end node, if so, the path needs to be reversed
        if (path1.get(0).getRow() == end[0] && path1.get(0).getCol() == end[1]) Collections.reverse(path1);
        visualise(closedList, path1);
    }
}
