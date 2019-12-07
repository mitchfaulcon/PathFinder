package mapGeneration;

import controller.PathFinderController;
import controller.Tile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import static algorithms.common.Algorithm.NEIGHBOUR_POSITIONS;

public class MazeGenerator extends MapGenerator {

    private final int SPACE = 0;
    private final int WALL = 1;
    private final int INTERSECTION_WALL = -1;
    private final int SEARCHED_SPACE = -2;
    private final int SEARCHED_WALL = -3;

    /**
     * Class to hold a group of empty spaces in the map.
     * The regions in the map act as a tree, with each having another as its parent.
     * When two regions get connected, the root of one is set to the other.
     * This results in all connected regions having the same root, allowing an easy check to see if they are connected
     */
    static class EmptyRegion {
        List<int[]> spaces = new ArrayList<>();

        EmptyRegion parent = null;

        EmptyRegion root() {
            return parent == null ? this : parent.root();
        }

        void mergeRegion(EmptyRegion region) {
            region.root().parent = this;
        }

        boolean isConnected(EmptyRegion region) {
            return root() == region.root();
        }
    }
    private List<EmptyRegion> regions = new ArrayList<>();      //List of all empty regions in the map

    /**
     * Class to hold a group of connected walls that can later be removed from the map to produce a maze
     */
    static class Edge {
        List<int[]> walls = new ArrayList<>();
        List<EmptyRegion> connectedRegions = new ArrayList<>();
    }

    public MazeGenerator(Tile[][] tileMap) {
        super(tileMap);
    }

    /**
     * Method to generate a maze using a customised version of Kruskal's maze generation algorithm
     */
    public void generateMap() {
        int rows = tileMap.length;
        int cols = tileMap[0].length;

        //Calculate how much space there should be in between the walls of the maze
        int horizontalSpaces = (cols - 1) / 20 + 1;       //5-20 -> 1, 21-40 -> 2, 41-60 -> 3, 61-80 -> 4
        int verticalSpaces = (rows - 1) / 20 + 1;

        //Setup initial criss-cross pattern of walls for maze
        for (int row = 0; row < rows; row++) {
            boolean wallRow = row % (verticalSpaces + 1) == verticalSpaces;
            for (int col = 0; col < cols; col++) {
                tileMap[row][col].updateTileStyle(PathFinderController.TileStyle.WALL);     //Start everything as a wall
                boolean wallColumn = col % (horizontalSpaces + 1) == horizontalSpaces;

                if (((row == rows - 1 && !wallColumn) ||           //Only place wall on last row if this column should be a wall
                        (col == cols - 1) && !wallRow) ||          //Only place wall on last column if this row should be a wall
                        (row == rows - 1 && col == cols - 1) ||    //Never want a wall in bottom-right corner
                        !(wallRow || wallColumn)) {                //Ensures there is enough space between walls
                    map[row][col] = SPACE;
                    tileMap[row][col].updateTileStyle(PathFinderController.TileStyle.NONE);

                } else if (wallColumn && wallRow && !(row == rows - 1 || col == cols - 1)) {    //Want to ignore walls at intersections
                    map[row][col] = INTERSECTION_WALL;

                } else {
                    map[row][col] = WALL;
                }

            }
        }

        int[][] neighbouringPositions = NEIGHBOUR_POSITIONS;
        //Group empty regions in map together
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (map[row][col] == SPACE) {
                    EmptyRegion currentRegion = new EmptyRegion();

                    //Add all neighbouring spaces to same region
                    Stack<int[]> connectedSpaces = new Stack<>();
                    connectedSpaces.push(new int[]{row, col});
                    map[row][col] = SEARCHED_SPACE;     //Don't want to check this space again
                    while (!connectedSpaces.isEmpty()) {
                        int[] currentSpace = connectedSpaces.pop();

                        //Find neighbouring spaces
                        for (int[] newPosition : neighbouringPositions){
                            //Get space position
                            int[] spacePosition = {currentSpace[0] + newPosition[0], currentSpace[1] + newPosition[1]};

                            //Make sure space position is within bounds
                            if (spacePosition[0] >= rows || spacePosition[0] < 0 || spacePosition[1] >= cols || spacePosition[1] < 0) {
                                continue;
                            }

                            //Make sure position is not a wall or has been searched before
                            if (map[spacePosition[0]][spacePosition[1]] == WALL || map[spacePosition[0]][spacePosition[1]] == SEARCHED_SPACE || map[spacePosition[0]][spacePosition[1]] == INTERSECTION_WALL) continue;

                            //Add new space to stack
                            connectedSpaces.push(new int[]{spacePosition[0], spacePosition[1]});
                            map[spacePosition[0]][spacePosition[1]] = SEARCHED_SPACE;   //Don't want to check this space again
                        }

                        //Add space to current region after neighbours have been found
                        currentRegion.spaces.add(currentSpace);
                    }

                    regions.add(currentRegion);
                }
            }
        }

        //Group removable wall sections (edges)
        Stack<Edge> edgeStack = new Stack<>();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                if (map[row][col] == WALL) {
                    //Find all connected walls
                    Edge currentEdge = new Edge();

                    Stack<int[]> connectedWalls = new Stack<>();
                    connectedWalls.push(new int[]{row, col});
                    map[row][col] = SEARCHED_WALL;     //Don't want to check this space again
                    while (!connectedWalls.isEmpty()) {
                        int[] currentWall = connectedWalls.pop();

                        //Find neighbouring walls
                        for (int[] newPosition : neighbouringPositions){
                            //Get wall position
                            int[] wallPosition = {currentWall[0] + newPosition[0], currentWall[1] + newPosition[1]};

                            //Make sure wall position is within bounds
                            if (wallPosition[0] >= rows || wallPosition[0] < 0 || wallPosition[1] >= cols || wallPosition[1] < 0) {
                                continue;
                            }

                            //Make sure position is not an intersection wall and has not been checked before
                            if (map[wallPosition[0]][wallPosition[1]] == INTERSECTION_WALL || map[wallPosition[0]][wallPosition[1]] == SEARCHED_WALL) continue;

                            if (map[wallPosition[0]][wallPosition[1]] == SEARCHED_SPACE) {
                                //Add region to this edge's list of connected regions
                                EmptyRegion connectedRegion = getRegionContainingSpace(wallPosition);
                                if (!currentEdge.connectedRegions.contains(connectedRegion) && connectedRegion != null) currentEdge.connectedRegions.add(connectedRegion);
                                continue;
                            }
                            //Add new wall to stack
                            connectedWalls.push(new int[]{wallPosition[0], wallPosition[1]});
                            map[wallPosition[0]][wallPosition[1]] = SEARCHED_WALL;   //Don't want to check this wall again
                        }

                        //Add wall to current edge after neighbours have been found
                        currentEdge.walls.add(currentWall);
                    }

                    edgeStack.push(currentEdge);
                }
            }
        }

        //Randomise order of edges to look at
        Collections.shuffle(edgeStack);

        while (!edgeStack.isEmpty()) {
            Edge currentEdge = edgeStack.pop();
            EmptyRegion connectedRegion1 = currentEdge.connectedRegions.get(0);      //connectedRegions will always have 2 elements
            EmptyRegion connectedRegion2 = currentEdge.connectedRegions.get(1);

            //Only want to remove the edge if its neighbouring regions are not already connected, ie. a cycle will not be formed
            if (!connectedRegion1.isConnected(connectedRegion2)) {
                for (int[] wall : currentEdge.walls) {
                    tileMap[wall[0]][wall[1]].updateTileStyle(PathFinderController.TileStyle.NONE);
                }
                //Connect the regions together
                connectedRegion1.mergeRegion(connectedRegion2);
            }
        }

    }

    private EmptyRegion getRegionContainingSpace(int[] space) {
        //Check through all regions to find one containing the input empty space
        for (EmptyRegion region : regions) {
            for (int[] emptySpace : region.spaces) {
                if (emptySpace[0] == space[0] && emptySpace[1] == space[1]) return region;
            }
        }
        return null;
    }
}