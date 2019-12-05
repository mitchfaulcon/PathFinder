package algorithms.common;

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

    protected Algorithm(int[][] map, int[] start, int[] end, Tile[][] tileMap) {
        this.map = map;
        this.start = start;
        this.end = end;
        this.tileMap = tileMap;
        isRunning = true;
    }

    public abstract void startAlgorithm();

    public void addListener(AlgorithmListener listener) {
        listeners.add(listener);
    }

    private void algorithmCompleted() {
        for (AlgorithmListener listener : listeners) {
            listener.algorithmCompleted();
        }
    }

    /**
     * Method to build the complete path in order starting from the first node to the end node
     */
    protected ArrayList<Node> buildPath(Node endNode) {
        Node pathNode = endNode;
        ArrayList<Node> path = new ArrayList<>();
        while (pathNode != null) {
            //Need to add path nodes in reverse order
            path.add(0, pathNode);
            pathNode = pathNode.parent;
        }
        return path;
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
