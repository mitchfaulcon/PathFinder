package controller;

import javafx.scene.layout.Pane;
import controller.PathFinderController.TileStyle;

public class Tile extends Pane {
    private static final String TILE_BORDER_STYLE = " -fx-border-color: black; -fx-border-insets: -0.2; -fx-border-width: 0.4";
    private static final String DEFAULT_STYLE = "-fx-background-color: white;" + TILE_BORDER_STYLE;
    private static final String WALL_STYLE = "-fx-background-color: grey;" + TILE_BORDER_STYLE;
    private static final String START_STYLE = "-fx-background-color: dodgerblue;" + TILE_BORDER_STYLE;
    private static final String FINISH_STYLE = "-fx-background-color: green;" + TILE_BORDER_STYLE;

    private int x;
    private int y;
    private TileStyle tileStyle = TileStyle.NONE;

    public Tile (int x, int y) {
        this.x = x;
        this.y = y;
        setStyle(DEFAULT_STYLE);
    }

    /**
     * Sets the cell colour depending on the currently selected drawing mode
     */
    public void UpdateTileStyle(TileStyle drawingMode) {
        tileStyle = drawingMode;

        switch (tileStyle) {
            case NONE:
                setStyle(DEFAULT_STYLE);
                break;
            case START:
                setStyle(START_STYLE);
                break;
            case FINISH:
                setStyle(FINISH_STYLE);
                break;
            case WALL:
                setStyle(WALL_STYLE);
                break;
        }
    }
}
