package controller;

import com.jfoenix.controls.JFXToggleNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class PathFinderController implements Initializable {

    public ToggleGroup DRAWMODE;
    @FXML Spinner<Integer> rowSpinner;
    @FXML Spinner<Integer> colSpinner;
    @FXML GridPane graphGrid;
    @FXML JFXToggleNode EraserToggle;
    @FXML JFXToggleNode WallToggle;
    @FXML JFXToggleNode StartToggle;
    @FXML JFXToggleNode FinishToggle;

    public enum TileStyle {START, FINISH, WALL, NONE}
    private TileStyle drawingMode;

    private ArrayList<ArrayList<Tile>> tileGrid = new ArrayList<>();        //Stores all tiles

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Setup spinners
        rowSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 80));
        colSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(5, 80));
        rowSpinner.valueProperty().addListener((observer, oldValue, newValue)->UpdateGrid());
        colSpinner.valueProperty().addListener((observer, oldValue, newValue)->UpdateGrid());
        rowSpinner.getValueFactory().setValue(10);
        colSpinner.getValueFactory().setValue(10);

        //Erase mode is default
        drawingMode = TileStyle.NONE;
        EraserToggle.setSelected(true);
    }

    private void UpdateGrid() {

        //Delete all old cells
        graphGrid.getChildren().clear();
        tileGrid.clear();

        //Remove all rows and columns
        while(graphGrid.getRowConstraints().size() > 0) {
            graphGrid.getRowConstraints().remove(0);
        }
        while(graphGrid.getColumnConstraints().size() > 0){
            graphGrid.getColumnConstraints().remove(0);
        }

        //Add entered number of rows and columns
        while (graphGrid.getRowConstraints().size() < rowSpinner.getValue()){
            RowConstraints rowConstraints = new RowConstraints();
            rowConstraints.setPercentHeight(100.0 / rowSpinner.getValue());         //Even row height
            graphGrid.getRowConstraints().add(rowConstraints);
        }
        while (graphGrid.getColumnConstraints().size() < colSpinner.getValue()){
            ColumnConstraints columnConstraints = new ColumnConstraints();
            columnConstraints.setPercentWidth(100.0 / colSpinner.getValue());       //Even column width
            graphGrid.getColumnConstraints().add(columnConstraints);
        }

        //Add a tile object to each cell to allow for colouring & algorithms
        for (int row = 0; row < rowSpinner.getValue(); row++) {
            ArrayList<Tile> rowTiles = new ArrayList<>();       //List for just this row
            for (int col = 0; col < colSpinner.getValue(); col++) {
                Tile tile = new Tile(row, col);
                graphGrid.add(tile, col, row);
                rowTiles.add(tile);
            }
            tileGrid.add(rowTiles);
        }

        AddCellListeners();
    }

    private void AddCellListeners(){
        for (Node node : graphGrid.getChildren()){
            if (node instanceof Tile){      //Just to be safe

                //Start drawing
                node.setOnDragDetected(e -> {
                    node.startFullDrag();
                });

                //Detect when mouse is clicked and dragged over
                node.setOnMouseDragEntered(e -> {
                    UpdateTile((Tile) node);
                });

                //Also need to detect single clicks
                node.setOnMousePressed(e -> {
                    UpdateTile((Tile) node);
                });
            }
        }
    }

    private void UpdateTile(Tile tile){
        //If drawing start or finish point, reset style for all other tiles in grid
        if (drawingMode == TileStyle.START || drawingMode == TileStyle.FINISH){
            for (ArrayList<Tile> tileRow: tileGrid) {
                for (Tile t : tileRow) {
                    if (t.GetTileStyle() == TileStyle.START && drawingMode == TileStyle.START){
                        t.UpdateTileStyle(TileStyle.NONE);
                    }
                    if (t.GetTileStyle() == TileStyle.FINISH && drawingMode == TileStyle.FINISH){
                        t.UpdateTileStyle(TileStyle.NONE);
                    }
                }
            }
        }
        tile.UpdateTileStyle(drawingMode);
    }

    public void OnDrawToggle(ActionEvent actionEvent) {
        if (actionEvent.getSource() instanceof JFXToggleNode){
            if (actionEvent.getSource().equals(EraserToggle)){
                if (drawingMode == TileStyle.NONE){
                    EraserToggle.setSelected(true);     //Don't let toggle get deselected when clicked again
                }
                drawingMode = TileStyle.NONE;
            } else if (actionEvent.getSource().equals(WallToggle)){
                if (drawingMode == TileStyle.WALL){
                    WallToggle.setSelected(true);     //Don't let toggle get deselected when clicked again
                }
                drawingMode = TileStyle.WALL;
            } else if (actionEvent.getSource().equals(StartToggle)){
                if (drawingMode == TileStyle.START){
                    StartToggle.setSelected(true);     //Don't let toggle get deselected when clicked again
                }
                drawingMode = TileStyle.START;
            } else if (actionEvent.getSource().equals(FinishToggle)){
                if (drawingMode == TileStyle.FINISH){
                    FinishToggle.setSelected(true);     //Don't let toggle get deselected when clicked again
                }
                drawingMode = TileStyle.FINISH;
            }
        }
    }
}
