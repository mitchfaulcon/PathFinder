package controller;

import javafx.scene.control.Label;
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
    private int weight = 1;     //Default weight
    private boolean isSearched = false;

    public Tile () {
        setStyle(DEFAULT_STYLE);
    }

    /**
     * Sets the cell colour depending on the currently selected drawing mode
     */
    public void UpdateTileStyle(TileStyle drawingMode, int weightedTileValue) {

        //Don't update tile to searched/path found if it is a wall, start, or finish tile
        if ((drawingMode == TileStyle.SEARCHED || drawingMode == TileStyle.PATH) &&
                (tileStyle == TileStyle.WALL || tileStyle == TileStyle.START || tileStyle == TileStyle.FINISH)) return;

        if (isSearched && drawingMode == TileStyle.NONE && tileStyle == TileStyle.WEIGHTED) {
            setStyle(DEFAULT_STYLE);
            isSearched = false;
            return;
        }

        //Remove labels
        if (drawingMode != TileStyle.SEARCHED && drawingMode != TileStyle.PATH) getChildren().clear();
        isSearched = false;

        switch (drawingMode) {
            case NONE:
                weight = 1;
                setStyle(DEFAULT_STYLE);
                break;
            case WEIGHTED:
                setStyle(DEFAULT_STYLE);
                if (weightedTileValue == 1) {
                    //Just want to erase background
                    tileStyle = TileStyle.NONE;
                    break;
                }
                weight = weightedTileValue;
                //Add label with weight to center of tile
                Label label = new Label(Integer.toString(weight));
                label.layoutXProperty().bind(this.widthProperty().subtract(label.widthProperty()).divide(2));
                label.layoutYProperty().bind(this.heightProperty().subtract(label.heightProperty()).divide(2));
                getChildren().add(label);
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
                isSearched = true;
                return;     //Don't update tileStyle field, only background colour
            case PATH:
                setStyle(PATH_STYLE);
                isSearched = true;
                return;     //Don't update tileStyle field, only background colour
        }
        tileStyle = drawingMode;
    }

    public void UpdateTileStyle(TileStyle drawingMode) {
        UpdateTileStyle(drawingMode, 1);
    }

    public TileStyle GetTileStyle() {
        return tileStyle;
    }

    public int GetWeight() {
        return weight;
    }

    public boolean HasBeenSearched() {
        return isSearched;
    }
}
