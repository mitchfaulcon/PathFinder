package controller;

import javafx.scene.layout.Pane;
import controller.PathFinderController.TileStyle;

public class Tile extends Pane {
    private static final String TILE_BORDER_STYLE = " -fx-border-color: black; -fx-border-insets: -0.2; -fx-border-width: 0.4";
    private static final String DEFAULT_STYLE = "-fx-background-color: white;" + TILE_BORDER_STYLE;
    private static final String WALL_STYLE = "-fx-background-color: grey;" + TILE_BORDER_STYLE;
    private static final String START_STYLE = "-fx-background-color: dodgerblue;" + TILE_BORDER_STYLE;
    private static final String FINISH_STYLE = "-fx-background-color: rgb(255,36,67);" + TILE_BORDER_STYLE;
    private static final String SEARCHED_STYLE = "-fx-background-color: rgb(255,243,75);" + TILE_BORDER_STYLE;
    private static final String PATH_STYLE = "-fx-background-color: rgb(69,255,38);" + TILE_BORDER_STYLE;

    private TileStyle tileStyle = TileStyle.NONE;

    public Tile () {
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
            case SEARCHED:
                setStyle(SEARCHED_STYLE);
                break;
            case PATH:
                setStyle(PATH_STYLE);
                break;
        }
    }

    public TileStyle GetTileStyle() {
        return tileStyle;
    }
}
