package mapGeneration;

import controller.Tile;

public abstract class MapGenerator {

    protected Tile[][] tileMap;       //Tilemap to generate map on
    protected int[][] map;            //2D array to store status of currently generated map
    protected int rows, cols;

    public MapGenerator(Tile[][] tileMap){
        this.tileMap = tileMap;
        rows = tileMap.length;
        cols = tileMap[0].length;
        map = new int[rows][cols];
    }

    public abstract void generateMap();
}
