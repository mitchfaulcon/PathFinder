# PathFinder
PathFinder is a shortest-path algorithm visualisation tool that allows you to see how different pathfinding algorithms run by colouring in each tile as it gets searched, and then colouring in the shortest path once it gets found.

## Features
- Change the size of the grid that the algorithm will be run on - can be anywhere from a 5x5 grid to an 80x80 grid
- Draw any map that you want to see a shortest path for - can place start & finish points, draw walls, or place weighted tiles that cost more to travel through than the default tiles
- Saving & loading of maps - can save a map to load it back in later, and can also load in an image (png or jpeg) to use as a map to perform pathfinding on

Different algorithms, both for pathfinding and for random map generation have also been implemented:
### Pathfinding Algorithms
- A*: Nodes closer to the end point are given a higher weighting so tend to get searched first 
<p align="center">
  <img src="https://github.com/mitchfaulcon/PathFinder/blob/master/Wiki/gifs/aStar.gif" width="400">
</p>

- Dijkstra: Nodes with the current lowest weight are searched first
<p align="center">
  <img src="https://github.com/mitchfaulcon/PathFinder/blob/master/Wiki/gifs/Dijkstra.gif" width="400">
</p>

- Bi-Directional: An alternative version of Dijstra's algorithm where both the start and finish points search for each other at the same time
<p align="center">
  <img src="https://github.com/mitchfaulcon/PathFinder/blob/master/Wiki/gifs/BiDirectional.gif" width="400">
</p>

- BFS (Breadth First Search): All neighbour nodes are searched before moving to the next layer of nodes - does not take into account node weight
<p align="center">
  <img src="https://github.com/mitchfaulcon/PathFinder/blob/master/Wiki/gifs/BFS.gif" width="400">
</p>

- DFS (Depth First Search): Paths are explored as far as possible before backtracking
<p align="center">
  <img src="https://github.com/mitchfaulcon/PathFinder/blob/master/Wiki/gifs/DFS.gif" width="400">
</p>

### Random Map Generation Algorithms
- Maze generation: A modified version of [Kruskal's algorithm](https://en.wikipedia.org/wiki/Kruskal's_algorithm) has been used to generate completely random mazes with only one possible solution (perfect maze)
<p align="center">
  <img src="https://github.com/mitchfaulcon/PathFinder/blob/master/Wiki/screenshots/map_maze.png" width="400">
</p>

- Purely random map: Randomly selects tiles to be set as walls or not
<p align="center">
  <img src="https://github.com/mitchfaulcon/PathFinder/blob/master/Wiki/screenshots/map_random.png" width="400">
</p>

- Perlin Noise generation: Uses the Java3D library to generate a Perlin Noise map
<p align="center">
  <img src="https://github.com/mitchfaulcon/PathFinder/blob/master/Wiki/screenshots/map_perlin.png" width="400">
</p>

## Running PathFinder
To download PathFinder, head to the [Releases](https://github.com/mitchfaulcon/PathFinder/releases) page and download the latest .zip file. After the download is complete, extract the .zip file to the desired loaction and run the PathFinder.bat file.

Note: Java 11 or higher is required to run PathFinder (Java 13 can be downloaded from [here](https://www.oracle.com/technetwork/java/javase/downloads/jdk13-downloads-5672538.html))
