package algorithms.BFS;

import algorithms.common.Algorithm;
import algorithms.common.Map.RET_CODE;
import controller.Tile;

public class BFSAlgorithm extends Algorithm {

    public BFSAlgorithm(int[][] map, int[] start, int[] end){
        super(map, start, end);
    }

    public RET_CODE RunAlgorithm(Tile[][] tileMap) {
        return RET_CODE.SUCCESS;
    }
}
