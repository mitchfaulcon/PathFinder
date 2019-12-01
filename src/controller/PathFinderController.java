package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;

import java.net.URL;
import java.util.ResourceBundle;

public class PathFinderController implements Initializable {

    @FXML Spinner<Integer> rowSpinner;
    @FXML Spinner<Integer> colSpinner;
    @FXML GridPane graphGrid;

    public enum TileStyle {START, FINISH, WALL, NONE}
    private TileStyle drawingMode = TileStyle.NONE;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Setup spinners
        rowSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(20, 80));
        colSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(20, 80));
        rowSpinner.valueProperty().addListener((observer, oldValue, newValue)->UpdateGrid());
        colSpinner.valueProperty().addListener((observer, oldValue, newValue)->UpdateGrid());
        rowSpinner.getValueFactory().setValue(30);
        colSpinner.getValueFactory().setValue(30);
    }

    private void UpdateGrid() {

        //Delete all old cells
        graphGrid.getChildren().clear();

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
            for (int col = 0; col < colSpinner.getValue(); col++) {
                Tile tile = new Tile(row, col);
                //tile.setPrefSize(graphGrid.getPrefWidth() / colSpinner.getValue(), graphGrid.getPrefHeight() / rowSpinner.getValue());
                graphGrid.add(tile, col, row);
            }
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
                    ((Tile) node).UpdateTileStyle(drawingMode);
                });
            }
        }
    }
}
