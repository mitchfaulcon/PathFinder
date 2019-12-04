package algorithms.common;

import algorithms.common.Map.RET_CODE;
import controller.Tile;
import javafx.application.Platform;

import java.util.concurrent.TimeUnit;

public abstract class Algorithm {

    protected int[][] map;
    protected int[] start;
    protected int[] end;
    protected static final int[][] NEIGHBOUR_POSITIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};    //Up, Down, Left, Right
    protected static final int UPDATE_DELAY_MS = 100;

    protected Algorithm(int[][] map, int[] start, int[] end) {
        this.map = map;
        this.start = start;
        this.end = end;
    }

    public abstract RET_CODE StartAlgorithm(Tile[][] tileMap);

    protected void WaitForDelay() {
//        try
//        {
//            Thread.sleep(UPDATE_DELAY_MS);
//        }
//        catch(InterruptedException ex)
//        {
//            Thread.currentThread().interrupt();
//        }
    }
}
