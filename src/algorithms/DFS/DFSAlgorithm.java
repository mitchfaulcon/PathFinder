package algorithms.DFS;

import algorithms.common.Algorithm;
import algorithms.common.Map.RET_CODE;
import controller.Tile;

public class DFSAlgorithm extends Algorithm {

    public DFSAlgorithm(int[][] map, int[] start, int[] end){
        super(map, start, end);
    }

    public void startAlgorithm(Tile[][] tileMap) {
        System.out.println("Running DFS algorithm");
    }
}
