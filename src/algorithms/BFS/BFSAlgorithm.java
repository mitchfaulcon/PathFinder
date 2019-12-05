package algorithms.BFS;

import algorithms.common.Algorithm;
import controller.Tile;

public class BFSAlgorithm extends Algorithm {

    public BFSAlgorithm(int[][] map, int[] start, int[] end, Tile[][] tileMap){
        super(map, start, end, tileMap);
    }

    public void startAlgorithm() {
        System.out.println("Running BFS algorithm");
    }
}
