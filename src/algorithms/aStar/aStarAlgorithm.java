package algorithms.aStar;

import algorithms.common.Algorithm;
import algorithms.common.Map.RET_CODE;
import algorithms.common.Node;
import controller.PathFinderController;
import controller.Tile;

import java.util.ArrayList;

public class aStarAlgorithm extends Algorithm {

    public aStarAlgorithm(int[][] map, int[] start, int[] end){
        super(map, start, end);
    }

    public RET_CODE StartAlgorithm(Tile[][] tileMap){

        ArrayList<aStarNode> openList = new ArrayList<>();
        ArrayList<aStarNode> closedList = new ArrayList<>();

        openList.add(new aStarNode(start[0], start[1], null));

        while (!openList.isEmpty()) {

            //Get node with smallest cost
            aStarNode currentNode = openList.get(0);
            for (aStarNode node : openList) {
                if (node.f < currentNode.f) {
                    currentNode = node;
                }
            }

            //Remove current node from open list, add to closed list
            openList.remove(currentNode);
            //Update tile colour to searched, wait for delay to show next one
            tileMap[currentNode.GetRow()][currentNode.GetCol()].UpdateTileStyle(PathFinderController.TileStyle.SEARCHED);
            //TODO WaitForDelay();

            //End was found
            if (currentNode.GetRow() == end[0] && currentNode.GetCol() == end[1]) {
                Node pathNode = currentNode;
                ArrayList<Node> path = new ArrayList<>();
                while (pathNode != null) {
                    //Add path nodes in reverse order
                    path.add(0, pathNode);
                    pathNode = pathNode.GetParent();
                }
                //Display final path
                for (Node node : path) {
                    tileMap[node.GetRow()][node.GetCol()].UpdateTileStyle(PathFinderController.TileStyle.PATH);
                    //TODO WaitForDelay();
                }
                return RET_CODE.SUCCESS;
            }

            //Generate children nodes
            ArrayList<aStarNode> children = new ArrayList<>();
            for (int[] newPosition : NEIGHBOUR_POSITIONS){
                //Get node position
                int[] nodePosition = {currentNode.GetRow() + newPosition[0], currentNode.GetCol() + newPosition[1]};

                //Make sure node position is within bounds
                if (nodePosition[0] > map.length - 1 || nodePosition[0] < 0 || nodePosition[1] > map[0].length - 1 || nodePosition[1] < 0) {
                    continue;
                }

                //Make sure position is not a wall
                if (map[nodePosition[0]][nodePosition[1]] == -1) continue;

                //Add new node to children
                children.add(new aStarNode(nodePosition[0], nodePosition[1], currentNode));
            }

            //Loop through found children
            childLoop: for (aStarNode child : children) {

                //Check if child is in closed list
                for (aStarNode closedNode : closedList) {
                    if (child.GetRow() == closedNode.GetRow() && child.GetCol() == closedNode.GetCol()) continue childLoop;
                }

                //Create f,g,h values
                child.g = currentNode.g + map[child.GetRow()][child.GetCol()];
                child.h = (int) Math.sqrt((Math.pow(child.GetRow() - end[0], 2) + Math.pow(child.GetCol() - end[1], 2)));
                child.f = child.g + child.h;

                for (aStarNode openNode : openList) {
                    if (child.GetRow() == openNode.GetRow() && child.GetCol() == openNode.GetCol()) {
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
        return RET_CODE.NO_PATH;
    }
}
