package algorithms.common;

import algorithms.common.Map.RET_CODE;
import controller.Tile;

public abstract class Algorithm {

    protected int[][] map;
    protected int[] start;
    protected int[] end;

    protected Algorithm(int[][] map, int[] start, int[] end) {
        this.map = map;
        this.start = start;
        this.end = end;
    }

    public abstract RET_CODE RunAlgorithm(Tile[][] tileMap);
}
