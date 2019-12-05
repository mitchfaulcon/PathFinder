package controller;

import algorithms.common.Map;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXToggleNode;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import algorithms.common.Map.RET_CODE;
import javafx.stage.FileChooser;
import javafx.scene.control.Alert.AlertType;
import algorithms.common.AlgorithmFactory.AlgorithmType;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PathFinderController implements Initializable {

    @FXML Spinner<Integer> rowSpinner;
    @FXML Spinner<Integer> colSpinner;
    @FXML GridPane graphGrid;
    @FXML JFXToggleNode eraserToggle;
    @FXML JFXToggleNode wallToggle;
    @FXML JFXToggleNode startToggle;
    @FXML JFXToggleNode finishToggle;
    @FXML JFXToggleNode weightedToggle;
    @FXML Spinner<Integer> tileWeightSpinner;
    @FXML JFXButton saveButton;
    @FXML JFXButton loadButton;
    @FXML JFXComboBox<String> algorithmComboBox;
    @FXML JFXButton goButton;
    @FXML JFXButton stopButton;

    private static int MINROWS = 5;
    private static int MINCOLUMNS = 5;
    private static int MAXROWS = 80;
    private static int MAXCOLUMNS = 80;

    public enum TileStyle {START, FINISH, WALL, NONE, WEIGHTED, SEARCHED, PATH}

    private TileStyle drawingMode;
    private int weightedTileValue;
    private Tile[][] tileGrid;               //Stores all tiles

    private AlgorithmType currentAlgorithm;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Setup grid size spinners
        rowSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MINROWS, MAXROWS));
        colSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MINCOLUMNS, MAXCOLUMNS));
        rowSpinner.valueProperty().addListener((observer, oldValue, newValue)-> updateGrid());
        colSpinner.valueProperty().addListener((observer, oldValue, newValue)-> updateGrid());
        rowSpinner.getValueFactory().setValue(10);
        colSpinner.getValueFactory().setValue(10);

        //Start mode is default
        drawingMode = TileStyle.START;
        startToggle.setSelected(true);
        tileWeightSpinner.setDisable(true);
        weightedTileValue = 2;

        //Setup wall weight spinner
        tileWeightSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(2,100));
        tileWeightSpinner.valueProperty().addListener((observer, oldValue, newValue) -> {
            weightedTileValue = newValue;
            drawingMode = TileStyle.WEIGHTED;
        });

        saveButton.setDisable(true);

        //Setup algorithm selection combo box
        algorithmComboBox.getItems().add("A*");
        algorithmComboBox.getItems().add("BFS");
        algorithmComboBox.getItems().add("DFS");
        algorithmComboBox.getItems().add("Dijkstra");
        algorithmComboBox.getItems().add("Greedy");
        goButton.setDisable(true);      //Disable Go button until algorithm is selected
        stopButton.setDisable(true);    //Disable Stop button until algorithm is running
    }

    private void updateGrid() {

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

        addCellListeners();

        saveButton.setDisable(isGridEmpty());
    }

    private void addCellListeners(){
        for (Node node : graphGrid.getChildren()){
            if (node instanceof Tile){      //Just to be safe

                //Start drawing
                node.setOnDragDetected(e -> node.startFullDrag());

                //Detect when mouse is clicked and dragged over
                node.setOnMouseDragEntered(e -> updateTile((Tile) node));

                //Also need to detect single clicks
                node.setOnMousePressed(e -> updateTile((Tile) node));
            }
        }
    }

    private void updateTile(Tile tile){
        for (Tile[] tiles : tileGrid) {
            for (Tile t : tiles) {
                //Remove previous searched/path tiles
                if (t.hasBeenSearched()) {
                    t.updateTileStyle(TileStyle.NONE);
                }

                //If drawing start or finish point, delete previous start or finish points
                if (t.getTileStyle() == TileStyle.START && drawingMode == TileStyle.START) {
                    t.updateTileStyle(TileStyle.NONE);
                }
                if (t.getTileStyle() == TileStyle.FINISH && drawingMode == TileStyle.FINISH) {
                    t.updateTileStyle(TileStyle.NONE);
                }
            }
        }

        if (drawingMode == TileStyle.WEIGHTED) {
            tile.updateTileStyle(TileStyle.WEIGHTED, weightedTileValue);
        } else {
            tile.updateTileStyle(drawingMode);
        }

        saveButton.setDisable(isGridEmpty());
    }

    /**
     * @return True if each tile on the grid is empty
     *         False if there is at least one of a wall, start, or finish tile
     */
    private boolean isGridEmpty(){
        for (Tile[] tiles : tileGrid) {
            for (Tile t : tiles) {
                TileStyle tileStyle = t.getTileStyle();
                if (tileStyle == TileStyle.WALL || tileStyle == TileStyle.START || tileStyle == TileStyle.FINISH || tileStyle == TileStyle.WEIGHTED) return false;
            }
        }
        return true;
    }

    @FXML
    private void onDrawToggle(ActionEvent actionEvent) {
        if (actionEvent.getSource() instanceof JFXToggleNode){
            tileWeightSpinner.setDisable(true);
            if (actionEvent.getSource().equals(eraserToggle)){
                if (drawingMode == TileStyle.NONE){
                    eraserToggle.setSelected(true);     //Don't let toggle get deselected when clicked again
                }
                drawingMode = TileStyle.NONE;
            } else if (actionEvent.getSource().equals(wallToggle)){
                if (drawingMode == TileStyle.WALL){
                    wallToggle.setSelected(true);     //Don't let toggle get deselected when clicked again
                }
                drawingMode = TileStyle.WALL;
            } else if (actionEvent.getSource().equals(startToggle)){
                if (drawingMode == TileStyle.START){
                    startToggle.setSelected(true);     //Don't let toggle get deselected when clicked again
                }
                drawingMode = TileStyle.START;
            } else if (actionEvent.getSource().equals(finishToggle)){
                if (drawingMode == TileStyle.FINISH){
                    finishToggle.setSelected(true);     //Don't let toggle get deselected when clicked again
                }
                drawingMode = TileStyle.FINISH;
            } else if (actionEvent.getSource().equals(weightedToggle)){
                if (drawingMode == TileStyle.WEIGHTED){
                    weightedToggle.setSelected(true);     //Don't let toggle get deselected when clicked again
                }
                tileWeightSpinner.setDisable(false);    //Only enable tile weight selector when drawing weighted tiles
                drawingMode = TileStyle.WEIGHTED;
            }
        }
    }

    @FXML
    private void onGoButton() {
        //Disable all buttons while algorithm is running
        rowSpinner.setDisable(true);
        colSpinner.setDisable(true);
        startToggle.setDisable(true);
        finishToggle.setDisable(true);
        wallToggle.setDisable(true);
        eraserToggle.setDisable(true);
        weightedToggle.setDisable(true);
        tileWeightSpinner.setDisable(true);
        saveButton.setDisable(true);
        loadButton.setDisable(true);
        goButton.setDisable(true);
        stopButton.setDisable(false);   //And enable stop button

        //Remove previous searched/path tiles
        for (Tile[] tiles : tileGrid) {
            for (Tile t : tiles) {
                if (t.hasBeenSearched()) {
                    t.updateTileStyle(TileStyle.NONE);
                }
            }
        }

        RET_CODE ret = Map.getInstance().runAlgorithm(tileGrid, currentAlgorithm);
        switch (ret){
            case NO_PATH:
                showError("No path could be found");
                break;
            case NO_START:
                showError("There is no start tile on the grid");
                break;
            case NO_END:
                showError("There is no goal tile on the grid");
                break;
            case SUCCESS:
                System.out.println("Path found");
                break;
        }

        //Re-enable buttons when algorithm completed
        rowSpinner.setDisable(false);
        colSpinner.setDisable(false);
        startToggle.setDisable(false);
        finishToggle.setDisable(false);
        wallToggle.setDisable(false);
        eraserToggle.setDisable(false);
        weightedToggle.setDisable(false);
        saveButton.setDisable(false);
        loadButton.setDisable(false);
        goButton.setDisable(false);
        stopButton.setDisable(true);   //And disable stop button

        //Set previously selected button to be selected again
        switch (drawingMode) {
            case NONE:
                eraserToggle.setSelected(true);
                break;
            case START:
                startToggle.setSelected(true);
                break;
            case FINISH:
                finishToggle.setSelected(true);
                break;
            case WALL:
                wallToggle.setSelected(true);
                break;
            case WEIGHTED:
                weightedToggle.setSelected(true);
                tileWeightSpinner.setDisable(false);
                break;
        }

    }

    @FXML
    private void onStopButton() {
        //TODO Stop visualisation
    }

    private FileChooser createFileChooser() {
        FileChooser fileChooser = new FileChooser();

        //Start the FileChooser in the current directory rather than the root
        String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
        fileChooser.setInitialDirectory(new File(currentPath));

        //Set extension filter for FileChooser to PathFinder Map files
        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("PathFinder Map File (*.pfm)", "*.pfm");
        fileChooser.getExtensionFilters().add(extensionFilter);

        return fileChooser;
    }

    @FXML
    private void onSave() {
        //Show save dialog
        File file = createFileChooser().showSaveDialog(saveButton.getScene().getWindow());

        if (file != null) {
            saveToFile(file);
        }
    }

    private void saveToFile(File file) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(file);

            for (int row = 0; row < tileGrid.length; row++) {
                StringBuilder sb = new StringBuilder();
                for (int col = 0; col < tileGrid[0].length; col++) {
                    TileStyle tileStyle = tileGrid[row][col].getTileStyle();
                    switch (tileStyle) {
                        case SEARCHED:  //Fall through
                        case PATH:      //Fall through
                        case NONE:
                            sb.append("0");
                            break;
                        case WALL:
                            sb.append("w");
                            break;
                        case START:
                            sb.append("s");
                            break;
                        case FINISH:
                            sb.append("f");
                            break;
                    }
                }
                writer.print(sb);
                if (row < tileGrid.length - 1) writer.print("\n");
            }

            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(PathFinderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @FXML
    private void onLoad() {
        //Show load dialog
        File file = createFileChooser().showOpenDialog(saveButton.getScene().getWindow());

        if (file != null) {
            loadFile(file);
        }
    }

    private void loadFile(File file) {
        try {
            FileReader fileReader = new FileReader(file);

            //Get dimensions of loaded map
            int rows = 0;
            int cols = -1;
            BufferedReader reader = new BufferedReader(fileReader);
            String line = reader.readLine();
            while (line != null) {
                rows++;

                //Ensure line only contains legal characters
                if (!line.matches("[0wsf]*")){
                    showError("File contains illegal character (line " + rows + ")");
                    return;
                }

                //Ensure number of columns is the same in every row
                if (cols != -1 && line.length() != cols){
                    showError("Number of columns is not consistent throughout file");
                    return;
                } else {
                    cols = line.length();
                }

                line = reader.readLine();
            }
            //Ensure number of rows are within allowed range
            if (rows > MAXROWS){
                showError("Number of rows is too large");
                return;
            }
            if (rows < MINROWS){
                showError("Number of rows is too small");
                return;
            }
            //Ensure number of columns are within allowed range
            if (cols > MAXCOLUMNS){
                showError("Number of columns is too large");
                return;
            }
            if (cols < MINCOLUMNS){
                showError("Number of columns is too small");
                return;
            }

            rowSpinner.getValueFactory().setValue(rows);        //Automatically updates tileGrid via spinner listener
            colSpinner.getValueFactory().setValue(cols);

            //Convert file to correct tileGrid
            fileReader = new FileReader(file);
            reader = new BufferedReader(fileReader);
            line = reader.readLine();
            int row = 0;
            while (line != null) {
                int col = 0;

                for (char c : line.toCharArray()) {
                    switch (c) {
                        case '0':
                            tileGrid[row][col].updateTileStyle(TileStyle.NONE);
                            break;
                        case 'w':
                            tileGrid[row][col].updateTileStyle(TileStyle.WALL);
                            break;
                        case 's':
                            tileGrid[row][col].updateTileStyle(TileStyle.START);
                            break;
                        case 'f':
                            tileGrid[row][col].updateTileStyle(TileStyle.FINISH);
                            break;
                    }
                    col++;
                }

                row++;
                line = reader.readLine();
            }

            saveButton.setDisable(isGridEmpty());

            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(PathFinderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(AlertType.ERROR, message);
        alert.showAndWait();
    }

    @FXML
    private void onAlgorithmSelect() {
        String selected = algorithmComboBox.getSelectionModel().getSelectedItem();
        switch (selected) {
            case "A*":
                currentAlgorithm = AlgorithmType.ASTAR;
                break;
            case "BFS":
                currentAlgorithm = AlgorithmType.BFS;
                break;
            case "DFS":
                currentAlgorithm = AlgorithmType.DFS;
                break;
            case "Dijkstra":
                currentAlgorithm = AlgorithmType.DIJKSTRA;
                break;
            case "Greedy":
                currentAlgorithm = AlgorithmType.GREEDY;
                break;
        }
        goButton.setDisable(false);     //Algorithm has been selected so Go button can be clicked
    }
}
