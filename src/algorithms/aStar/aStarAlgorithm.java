package algorithms.aStar;

import algorithms.common.Algorithm;
import algorithms.common.Map.RET_CODE;
import controller.Tile;

public class aStarAlgorithm extends Algorithm {

    public aStarAlgorithm(int[][] map, int[] start, int[] end){
        super(map, start, end);
    }

    public RET_CODE StartAlgorithm(Tile[][] tileMap){
        System.out.println("Running A* algorithm");
        return RET_CODE.NO_PATH;
    }
}
