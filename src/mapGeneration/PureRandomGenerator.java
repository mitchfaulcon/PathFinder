package mapGeneration;

import controller.PathFinderController;
import controller.Tile;

import java.util.Random;

public class PureRandomGenerator extends MapGenerator {

    public PureRandomGenerator(Tile[][] tileMap) {
        super(tileMap);
    }

    /**
     * Method to generate a purely random map.
     * Randomly picks a wall coverage from 20-40% then places the walls
     */
    public void generateMap() {
        double coverage = Math.random() * (0.4 - 0.2) + 0.2;
        int numWallsToPlace = (int)(tileMap.length * tileMap[0].length * coverage);

        while (numWallsToPlace > 0) {
            int row, col;
            //Want to place a wall in a new location
            do {
                row = (int) (Math.random() * tileMap.length);
                col = (int) (Math.random() * tileMap[0].length);
            } while (tileMap[row][col].getTileStyle() == PathFinderController.TileStyle.WALL);

            tileMap[row][col].updateTileStyle(PathFinderController.TileStyle.WALL);
            numWallsToPlace--;
        }
    }
}
