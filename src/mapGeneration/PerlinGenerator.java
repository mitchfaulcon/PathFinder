package mapGeneration;

import controller.PathFinderController;
import controller.Tile;
import org.j3d.texture.procedural.PerlinNoiseGenerator;

public class PerlinGenerator extends MapGenerator {

    public PerlinGenerator(Tile[][] tileMap) {
        super(tileMap);
    }

    /**
     * Generates a random map using a perlin noise generator
     */
    public void generateMap() {
        int seed = (int)(Math.random() * Integer.MAX_VALUE);        //Random seed to ensure a different map each time
        PerlinNoiseGenerator perlin = new PerlinNoiseGenerator(seed);

        for (int row = 0; row < tileMap.length; row++) {
            for (int col = 0; col < tileMap[0].length; col++) {
                float val = 0;
                for(int i = 2; i <= 32; i = i * i) {
                    val += perlin.noise2(i*row/(float)tileMap.length, i*col/(float)tileMap[0].length) / i;
                }
                if (Math.abs(val) > 0.1) tileMap[row][col].updateTileStyle(PathFinderController.TileStyle.WALL);
                else tileMap[row][col].updateTileStyle(PathFinderController.TileStyle.NONE);
            }
        }
    }
}
