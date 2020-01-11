package controller;

/**
 * Class to store a snapshot of the tile map to be used in the undo/redo functionality
 */
public class MapSnapshot {
    Tile[][] snapShot;

    /**
     * Constructor that makes a copy of the input tilemap.
     * Cannot directly assign the tileMap input to the snapShot field as it will be updated from
     * the controller class when another draw is made.
     */
    public MapSnapshot(Tile[][] tileMap) {
        int rows = tileMap.length;
        int cols = tileMap[0].length;
        snapShot = new Tile[rows][cols];

        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                snapShot[row][col] = new Tile(tileMap[row][col].getTileStyle(), tileMap[row][col].getWeight());
            }
        }
    }

    /**
     * Sets the input tilemap to be the same as this snapshot
     */
    public void setAsMap(Tile[][] tileMap) {
        for (int row = 0; row < tileMap.length; row++) {
            for (int col = 0; col < tileMap[0].length; col++) {
                tileMap[row][col].updateTileStyle(snapShot[row][col].getTileStyle(), snapShot[row][col].getWeight());
            }
        }
    }
}
