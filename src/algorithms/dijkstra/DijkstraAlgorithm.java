package algorithms.dijkstra;

import algorithms.common.Algorithm;
import algorithms.common.Map.RET_CODE;
import controller.Tile;

public class DijkstraAlgorithm extends Algorithm {

    public DijkstraAlgorithm(int[][] map, int[] start, int[] end){
        super(map, start, end);
    }

    public void startAlgorithm(Tile[][] tileMap) {
        System.out.println("Running Dijkstra algorithm");
    }
}
