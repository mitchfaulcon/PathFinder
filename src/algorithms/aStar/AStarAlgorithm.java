package algorithms.aStar;

import algorithms.common.Algorithm;
import algorithms.common.Node;
import controller.Tile;

import java.util.ArrayList;

public class AStarAlgorithm extends Algorithm {

    public AStarAlgorithm(int[][] map, int[] start, int[] end, Tile[][] tileMap){
        super(map, start, end, tileMap);
    }

    public void startAlgorithm(){

        ArrayList<AStarNode> openList = new ArrayList<>();
        ArrayList<AStarNode> closedList = new ArrayList<>();

        openList.add(new AStarNode(start[0], start[1], null));

        while (!openList.isEmpty()) {

            //Get node with smallest cost
            AStarNode currentNode = openList.get(0);
            for (AStarNode node : openList) {
                if (node.f < currentNode.f) {
                    currentNode = node;
                }
            }

            //Remove current node from open list, add to closed list
            openList.remove(currentNode);

            //End was found
            if (currentNode.getRow() == end[0] && currentNode.getCol() == end[1]) {
                Node pathNode = currentNode;
                ArrayList<Node> path = new ArrayList<>();
                while (pathNode != null) {
                    //Add path nodes in reverse order
                    path.add(0, pathNode);
                    pathNode = pathNode.getParent();
                }
                //Display searched/path tiles
                visualise(closedList, path);
                return;
            }

            //Generate children nodes
            ArrayList<AStarNode> children = new ArrayList<>();
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
                children.add(new AStarNode(nodePosition[0], nodePosition[1], currentNode));
            }

            //Loop through found children
            childLoop: for (AStarNode child : children) {

                //Check if child is in closed list
                for (AStarNode closedNode : closedList) {
                    if (child.getRow() == closedNode.getRow() && child.getCol() == closedNode.getCol()) continue childLoop;
                }

                //Create f,g,h values
                child.g = currentNode.g + map[child.getRow()][child.getCol()];
                child.h = (int) Math.sqrt((Math.pow(child.getRow() - end[0], 2) + Math.pow(child.getCol() - end[1], 2)));
                child.f = child.g + child.h;

                for (AStarNode openNode : openList) {
                    if (child.getRow() == openNode.getRow() && child.getCol() == openNode.getCol()) {
                        //Skip this child if it has already been added with a lower total cost
                        if (openNode.f < child.f) continue childLoop;
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

            //Add current node to closed list
            closedList.add(currentNode);
        }

        //If while loop ends, no path was found

        //Only display searched nodes
        visualise(closedList, null);
    }
}
