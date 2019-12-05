package algorithms.dijkstra;

import algorithms.common.Algorithm;
import algorithms.common.Node;
import controller.Tile;

import java.util.ArrayList;

public class DijkstraAlgorithm extends Algorithm {

    public DijkstraAlgorithm(int[][] map, int[] start, int[] end, Tile[][] tileMap){
        super(map, start, end, tileMap);
    }

    public void startAlgorithm() {

        ArrayList<Node> closedList = new ArrayList<>();
        ArrayList<Node> openList = new ArrayList<>();

        Node startNode = new Node(start[0], start[1], null);
        startNode.weight = 0;
        openList.add(startNode);

        //Run while there are available nodes
        while (!openList.isEmpty()) {

            //Get node with the smallest cost
            Node currentNode = openList.get(0);
            for (Node node : openList) {
                if (node.weight < currentNode.weight) {
                    currentNode = node;
                }
            }

            //Remove current node from open list
            openList.remove(currentNode);

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
        }

        //If while loop ends, no path was found

        //Only display searched nodes
        visualise(closedList, null);
    }
}
