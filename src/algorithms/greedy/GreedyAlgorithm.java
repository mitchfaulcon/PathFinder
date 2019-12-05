package algorithms.greedy;

import algorithms.common.Algorithm;
import controller.Tile;

public class GreedyAlgorithm extends Algorithm {

    public GreedyAlgorithm(int[][] map, int[] start, int[] end, Tile[][] tileMap){
        super(map, start, end, tileMap);
    }

    public void startAlgorithm() {
        System.out.println("Running Greedy algorithm");
    }
}
