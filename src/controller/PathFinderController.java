package controller;

import algorithms.common.Map;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import algorithms.common.Map.RET_CODE;

import java.net.URL;
import java.util.ResourceBundle;

public class PathFinderController implements Initializable {

    @FXML Spinner<Integer> rowSpinner;
    @FXML Spinner<Integer> colSpinner;
    @FXML GridPane graphGrid;
    @FXML JFXToggleNode EraserToggle;
    @FXML JFXToggleNode WallToggle;
    @FXML JFXToggleNode StartToggle;
    @FXML JFXToggleNode FinishToggle;
    @FXML JFXButton goButton;

    public enum TileStyle {START, FINISH, WALL, NONE, SEARCHED, PATH}
    private TileStyle drawingMode;

    private Tile[][] tileGrid;               //Stores all tiles

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

        tileGrid = new Tile[rowSpinner.getValue()][colSpinner.getValue()];
        //Add a tile object to each cell to allow for colouring & algorithms
        for (int row = 0; row < rowSpinner.getValue(); row++) {
            for (int col = 0; col < colSpinner.getValue(); col++) {
                Tile tile = new Tile();
                graphGrid.add(tile, col, row);
                tileGrid[row][col] = tile;
            }
        }

        AddCellListeners();
    }

    private void AddCellListeners(){
        for (Node node : graphGrid.getChildren()){
            if (node instanceof Tile){      //Just to be safe

                //Start drawing
                node.setOnDragDetected(e -> node.startFullDrag());

                //Detect when mouse is clicked and dragged over
                node.setOnMouseDragEntered(e -> UpdateTile((Tile) node));

                //Also need to detect single clicks
                node.setOnMousePressed(e -> UpdateTile((Tile) node));
            }
        }
    }

    private void UpdateTile(Tile tile){
        for (Tile[] tiles : tileGrid) {
            for (Tile t : tiles) {
                //Remove previous paths
                if (t.GetTileStyle() == TileStyle.SEARCHED || t.GetTileStyle() == TileStyle.PATH) {
                    t.UpdateTileStyle(TileStyle.NONE);
                }

                //If drawing start or finish point, delete previous start or finish points
                if (t.GetTileStyle() == TileStyle.START && drawingMode == TileStyle.START) {
                    t.UpdateTileStyle(TileStyle.NONE);
                }
                if (t.GetTileStyle() == TileStyle.FINISH && drawingMode == TileStyle.FINISH) {
                    t.UpdateTileStyle(TileStyle.NONE);
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

    public void OnGoButton() {
        RET_CODE ret = Map.GetInstance().RunAlgorithm(tileGrid);

        switch (ret){
            case NO_PATH:
                System.out.println("No path");
                break;
            case NO_START:
                System.out.println("No start");
                break;
            case NO_END:
                System.out.println("No end");
                break;
            case SUCCESS:
                System.out.println("Path found");
                break;
        }
    }
}
