package algorithms.dijkstra;

import algorithms.common.Algorithm;
import controller.Tile;

public class DijkstraAlgorithm extends Algorithm {

    public DijkstraAlgorithm(int[][] map, int[] start, int[] end, Tile[][] tileMap){
        super(map, start, end, tileMap);
    }

    public void startAlgorithm() {
        System.out.println("Running Dijkstra algorithm");
    }
}
