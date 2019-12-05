package algorithms.common;

import algorithms.BFS.BFSAlgorithm;
import algorithms.DFS.DFSAlgorithm;
import algorithms.aStar.AStarAlgorithm;
import algorithms.dijkstra.DijkstraAlgorithm;
import algorithms.greedy.GreedyAlgorithm;

/**
 * Factory class to return a different type of Algorithm depending on input
 */
public class AlgorithmFactory {

    public enum AlgorithmType {ASTAR, BFS, DFS, DIJKSTRA, GREEDY}

    public Algorithm generateAlgorithm(AlgorithmType algorithm, int[][] map, int[] start, int[] end) {
        switch (algorithm){
            case ASTAR:
                return new AStarAlgorithm(map, start, end);
            case BFS:
                return new BFSAlgorithm(map, start, end);
            case DFS:
                return new DFSAlgorithm(map, start, end);
            case DIJKSTRA:
                return new DijkstraAlgorithm(map, start, end);
            case GREEDY:
                return new GreedyAlgorithm(map, start, end);
        }
        return null;
    }
}
