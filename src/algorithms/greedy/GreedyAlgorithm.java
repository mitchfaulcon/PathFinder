package algorithms.greedy;

import algorithms.common.Algorithm;
import algorithms.common.Map.RET_CODE;
import controller.Tile;

public class GreedyAlgorithm extends Algorithm {

    public GreedyAlgorithm(int[][] map, int[] start, int[] end){
        super(map, start, end);
    }

    public RET_CODE startAlgorithm(Tile[][] tileMap) {
        System.out.println("Running Greedy algorithm");
        return RET_CODE.NO_PATH;
    }
}
