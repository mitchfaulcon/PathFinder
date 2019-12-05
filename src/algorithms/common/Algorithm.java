package algorithms.common;

import algorithms.common.Map.RET_CODE;
import controller.PathFinderController;
import controller.Tile;
import javafx.application.Platform;

import java.util.ArrayList;

public abstract class Algorithm {

    protected ArrayList<AlgorithmListener> listeners = new ArrayList<>();

    protected Tile[][] tileMap;
    protected int[][] map;
    protected int[] start;
    protected int[] end;
    private boolean isRunning;
    protected static final int[][] NEIGHBOUR_POSITIONS = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};    //Up, Down, Left, Right

    protected Algorithm(int[][] map, int[] start, int[] end) {
        this.map = map;
        this.start = start;
        this.end = end;
        isRunning = true;
    }

    public abstract void startAlgorithm(Tile[][] tileMap);

    public void addListener(AlgorithmListener listener) {
        listeners.add(listener);
    }

    private void algorithmCompleted() {
        for (AlgorithmListener listener : listeners) {
            listener.algorithmCompleted();
        }
    }

    /**
     * Returns a good delay based on the number of input nodes
     */
    private int getDelay(int numNodes) {
        return (int)(635 * Math.pow(numNodes, -0.61));
    }

    protected void visualise(ArrayList<? extends Node> searchedNodes, ArrayList<Node> path) {

        Runnable task = () -> {
            try {
                //Update searched tile colours, wait for delay to show next one
                int searchDelay = getDelay(searchedNodes.size());
                for (Node node : searchedNodes) {
                    if (!isRunning) return;     //Stop visualisation
                    Platform.runLater(() -> {
                        tileMap[node.getRow()][node.getCol()].updateTileStyle(PathFinderController.TileStyle.SEARCHED);
                    });
                    Thread.sleep(searchDelay);
                }

                //Update path tile colours, wait for delay to show next one
                if (path != null) {
                    int pathDelay = getDelay(path.size());
                    for (Node node : path) {
                        if (!isRunning) return;     //Stop visualisation
                        Platform.runLater(() -> {
                            tileMap[node.getRow()][node.getCol()].updateTileStyle(PathFinderController.TileStyle.PATH);
                        });
                        Thread.sleep(pathDelay);
                    }
                }
                //Notify listeners of completion
                algorithmCompleted();
            }
            catch (InterruptedException ignored) {

            }
        };
        new Thread(task).start();
    }

    public void stopRunning() {
        isRunning = false;
    }
}
