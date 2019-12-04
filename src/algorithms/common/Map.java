package algorithms.common;

import controller.Tile;
import controller.PathFinderController.TileStyle;
import algorithms.common.AlgorithmFactory.AlgorithmType;

/**
 * Singleton class to store instance of map to perform path finding algorithm on
 */
public class Map {
    private static Map instance = new Map();    //Singleton

    public static Map GetInstance() {
        return instance;
    }

    private Map () {}

    public enum RET_CODE {SUCCESS, NO_PATH, NO_START, NO_END}

    //2D array to store data about map to perform path-finding algorithm on.
    // 0 - possible path
    // 1 - wall / not traversable
    private int[][] grid;

    private int[] start;
    private int[] end;

    public RET_CODE RunAlgorithm(Tile[][] tileMap, AlgorithmType algorithmToRun) {
        int rows = tileMap.length;
        int cols = tileMap[0].length;

        grid = new int[rows][cols];
        start = new int[2];
        end = new int[2];

        boolean startFound = false;
        boolean endFound = false;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                TileStyle tileStyle = tileMap[row][col].GetTileStyle();

                switch (tileStyle){
                    case SEARCHED:  //Fall through
                    case PATH:      //Fall through
                    case NONE:      //Fall through
                    case WEIGHTED:
                        grid[row][col] = tileMap[row][col].GetWeight();
                        break;
                    case WALL:
                        grid[row][col] = -1;
                        break;
                    case START:
                        start[0] = row;
                        start[1] = col;
                        startFound = true;
                        break;
                    case FINISH:
                        end[0] = row;
                        end[1] = col;
                        endFound = true;
                        break;
                }
            }
        }
        if (!startFound) return RET_CODE.NO_START;
        if (!endFound) return RET_CODE.NO_END;

        //DisplayMap();

        //Generate correct algorithm from factory
        Algorithm algorithm = new AlgorithmFactory().GenerateAlgorithm(algorithmToRun, grid, start, end);
        //Perform algorithm, return NO_PATH if path couldn't be found
        return algorithm.StartAlgorithm(tileMap);
    }

    /**
     * Helper function to display representation of map in terminal output
     */
    private void DisplayMap(){
        for (int[] row : grid) {
            for (int cell : row) {
                System.out.print(cell + "  ");
            }
            System.out.print("\n");
        }
        System.out.println("Start: " + start[0] + " " + start[1]);
        System.out.println("End: " + end[0] + " " + end[1]);
        System.out.print("\n");
    }
}
