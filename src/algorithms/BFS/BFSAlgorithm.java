package algorithms.BFS;

import algorithms.common.Algorithm;
import algorithms.common.Node;
import controller.Tile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

public class BFSAlgorithm extends Algorithm {

    public BFSAlgorithm(int[][] map, int[] start, int[] end, Tile[][] tileMap){
        super(map, start, end, tileMap);
    }

    public void startAlgorithm() {

        ArrayList<Node> closedList = new ArrayList<>();
        Queue<Node> openQueue = new LinkedList<>();

        Node startNode = new Node(start[0], start[1], null);
        startNode.weight = 0;
        openQueue.add(startNode);

        //Run while there are available nodes
        while (!openQueue.isEmpty()) {
            //Get node to process from queue
            Node currentNode = openQueue.remove();

            //End was found
            if (currentNode.getRow() == end[0] && currentNode.getCol() == end[1]) {
                //Display searched/path tiles
                visualise(closedList, buildPath(currentNode));
                return;
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

                //Check if child is in closed list
                for (Node closedNode : closedList) {
                    if (child.getRow() == closedNode.getRow() && child.getCol() == closedNode.getCol()) continue childLoop;
                }

                //Calculate weight of child
                child.weight = currentNode.weight + map[child.getRow()][child.getCol()];

                for (Node openNode : openQueue) {
                    if (child.getRow() == openNode.getRow() && child.getCol() == openNode.getCol()) {
                        //Skip this child if it has already been added
                        continue childLoop;
                    }
                }

                //Add child to queue
                openQueue.add(child);
            }

            closedList.add(currentNode);
        }

        //If while loop ends, no path was found

        //Only display searched nodes
        visualise(closedList, null);
    }
}
