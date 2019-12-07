package mapGeneration;

import controller.Tile;

public abstract class MapGenerator {

    protected Tile[][] tileMap;       //Tilemap to generate map on
    protected int[][] map;            //2D array to store status of currently generated map

    public MapGenerator(Tile[][] tileMap){
        this.tileMap = tileMap;
        map = new int[tileMap.length][tileMap[0].length];
    }

    public abstract void generateMap();
}
