package algorithms.dijkstra;

import algorithms.aStar.AStarNode;
import algorithms.common.Algorithm;
import algorithms.common.Node;
import controller.Tile;

import java.util.ArrayList;

public class DijkstraAlgorithm extends Algorithm {

    public DijkstraAlgorithm(int[][] map, int[] start, int[] end, Tile[][] tileMap){
        super(map, start, end, tileMap);
    }

    public void startAlgorithm() {

        ArrayList<DijkstraNode> closedList = new ArrayList<>();
        ArrayList<DijkstraNode> openList = new ArrayList<>();
//        ArrayList<DijkstraNode> unvisitedList = new ArrayList<>();

        //Add all nodes to unvisited list with infinite weight
//        for (int row = 0; row < map.length; row++) {
//            for (int col = 0; col < map[0].length; col++) {
//                DijkstraNode node = new DijkstraNode(row, col, null);
//                if (row == start[0] && col == start[1]) {
//                    //Set start node weight to 0
//                    node.weight = 0;
//                    openList.add(node);
//                } else {
//                    unvisitedList.add(node);
//                }
//            }
//        }

        DijkstraNode startNode = new DijkstraNode(start[0], start[1], null);
        startNode.weight = 0;
        openList.add(startNode);

        //Run while there are available nodes
        while (!openList.isEmpty()) {

            //Get node with the smallest cost
            DijkstraNode currentNode = openList.get(0);
            for (DijkstraNode node : openList) {
                if (node.weight < currentNode.weight) {
                    currentNode = node;
                }
            }

            //Remove current node from open list, add to closed list
            openList.remove(currentNode);

            //End was found
            if (currentNode.getRow() == end[0] && currentNode.getCol() == end[1]) {
                //Display searched/path tiles
                visualise(closedList, buildPath(currentNode));
                return;
            }

            //Generate children nodes
            ArrayList<DijkstraNode> children = new ArrayList<>();
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
                children.add(new DijkstraNode(nodePosition[0], nodePosition[1], currentNode));
            }

            //Loop through found children
            childLoop: for (DijkstraNode child : children) {

                //Check if child is in closed list
                for (DijkstraNode closedNode : closedList) {
                    if (child.getRow() == closedNode.getRow() && child.getCol() == closedNode.getCol()) continue childLoop;
                }

                //Calculate weight of child
                child.weight = currentNode.weight + map[child.getRow()][child.getCol()];

                for (DijkstraNode openNode : openList) {
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
