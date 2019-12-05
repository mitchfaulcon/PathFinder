package algorithms.DFS;

import algorithms.common.Algorithm;
import controller.Tile;

public class DFSAlgorithm extends Algorithm {

    public DFSAlgorithm(int[][] map, int[] start, int[] end, Tile[][] tileMap){
        super(map, start, end, tileMap);
    }

    public void startAlgorithm() {
        System.out.println("Running DFS algorithm");
    }
}
