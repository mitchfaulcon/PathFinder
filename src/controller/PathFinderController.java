package controller;

import algorithms.common.Algorithm;
import algorithms.common.AlgorithmFactory.AlgorithmType;
import algorithms.common.AlgorithmListener;
import algorithms.common.Map;
import algorithms.common.Map.RET_CODE;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXToggleNode;
import imageParser.ImageParser;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.FileChooser;
import mapGeneration.MazeGenerator;
import mapGeneration.PerlinGenerator;
import mapGeneration.PureRandomGenerator;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PathFinderController implements Initializable, AlgorithmListener {

    @FXML Spinner<Integer> rowSpinner;
    @FXML Spinner<Integer> colSpinner;
    @FXML GridPane graphGrid;
    @FXML JFXToggleNode eraserToggle;
    @FXML JFXToggleNode wallToggle;
    @FXML JFXToggleNode startToggle;
    @FXML JFXToggleNode finishToggle;
    @FXML JFXToggleNode weightedToggle;
    @FXML Spinner<Integer> tileWeightSpinner;
    @FXML JFXButton undoButton;
    @FXML JFXButton redoButton;
    @FXML JFXButton saveButton;
    @FXML JFXButton loadButton;
    @FXML MenuButton mapSelectMenu;
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

    private ArrayList<MapSnapshot> mapHistory = new ArrayList<>();  //Stores entire history of map to be used in undo/redo
    private int currentState;   //The current index of mapHistory that the map is displaying

    private AlgorithmType currentAlgorithm;
    private Algorithm algorithm;            //Algorithm that will run

    private Tile dragStartTile;     //Tile that click & drag is started from

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
        algorithmComboBox.getItems().add("Bi-Directional");
        goButton.setDisable(true);      //Disable Go button until algorithm is selected
        stopButton.setDisable(true);    //Disable Stop button until algorithm is running
    }

    private void updateGrid() {

        //Make sure values do not get set too high
        if (rowSpinner.getValue() > MAXROWS) rowSpinner.getValueFactory().setValue(MAXROWS);
        if (colSpinner.getValue() > MAXCOLUMNS) colSpinner.getValueFactory().setValue(MAXCOLUMNS);

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

        //Clear the operation history as we now have a new map
        resetOperationHistory();
    }

    private void addCellListeners(){
        for (Node node : graphGrid.getChildren()){
            if (node instanceof Tile){      //Just to be safe

                //Start drawing
                node.setOnDragDetected(e -> {
                    node.startFullDrag();
                    dragStartTile = (Tile) node;    //Save tile drag was started from
                });

                //Detect when mouse is clicked and dragged over
                node.setOnMouseDragEntered(e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        updateTile((Tile) node, true);            //Drawing individual tile with left click so update straight away
                    } else if (e.getButton() == MouseButton.SECONDARY) {
                        fillRightClickDrag((Tile) node);    //Selecting a range to draw with right click
                    }
                });

                //Also need to detect single left clicks
                node.setOnMousePressed(e -> {
                    if (e.getButton() == MouseButton.PRIMARY)
                        updateTile((Tile) node, false);
                });

                //When mouse is released, add the current state to operation history for undo/redo
                node.setOnMouseReleased(e -> {
                    if (algorithm == null || !algorithm.isRunning()) {  //Only add if algorithm is not running
                        //Add current state to history
                        addToHistory();
                    }
                });
            }
        }
    }

    private void updateTile(Tile tile, boolean dragging){
        if (algorithm != null && algorithm.isRunning()) return;    //Don't update if algorithm is running

        //Prevent erasing map while dragging around start or finish tile
        if ((drawingMode == TileStyle.START || drawingMode == TileStyle.FINISH) && dragging) {
            mapHistory.get(currentState).setAsMap(tileGrid);
        }
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

    /**
     * Resets the tilegrid to be empty
     */
    private void clearGrid() {
        //Clear the operation history as we now have a new map
        resetOperationHistory();

        for (Tile[] row : tileGrid) {
            for (Tile tile : row) {
                tile.updateTileStyle(TileStyle.NONE);
            }
        }
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
        undoButton.setDisable(true);
        redoButton.setDisable(true);
        saveButton.setDisable(true);
        loadButton.setDisable(true);
        mapSelectMenu.setDisable(true);
        algorithmComboBox.setDisable(true);
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

        Map map = Map.getInstance();
        RET_CODE ret = map.buildAlgorithm(tileGrid, currentAlgorithm);
        //Only want to handle errors here, success/no path are handled in listener method
        switch (ret){
            case NO_START:
                showError("There is no start tile on the grid");
                algorithmCompleted();     //Need to re-enable buttons
                break;
            case NO_END:
                showError("There is no goal tile on the grid");
                algorithmCompleted();     //Need to re-enable buttons
                break;
            case BUILD_SUCCESS:
                algorithm = map.getAlgorithm();
                //Add this class as listener for algorithm
                algorithm.addListener(this);
                //Perform algorithm
                algorithm.startAlgorithm();
                break;
        }
    }

    public void algorithmCompleted(){
        //Re-enable buttons when algorithm completed
        rowSpinner.setDisable(false);
        colSpinner.setDisable(false);
        startToggle.setDisable(false);
        finishToggle.setDisable(false);
        wallToggle.setDisable(false);
        eraserToggle.setDisable(false);
        weightedToggle.setDisable(false);
        undoButton.setDisable(currentState == 0);                       //Disable undo button if we are back at the start
        redoButton.setDisable(currentState == mapHistory.size() - 1);   //Disable redo button if we are at the last element
        saveButton.setDisable(false);
        loadButton.setDisable(false);
        mapSelectMenu.setDisable(false);
        algorithmComboBox.setDisable(false);
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
        //Stop visualisation
        algorithm.stopRunning();
        algorithmCompleted();
    }

    private FileChooser createFileChooser(boolean load) {
        FileChooser fileChooser = new FileChooser();

        //Start the FileChooser in the current directory rather than the root
        String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
        fileChooser.setInitialDirectory(new File(currentPath));

        //Set extension filter for FileChooser to PathFinder Map files
        FileChooser.ExtensionFilter pfmExtensionFilter = new FileChooser.ExtensionFilter("PathFinder Map File (*.pfm)", "*.pfm");
        fileChooser.getExtensionFilters().add(pfmExtensionFilter);
        //Able to load images
        if (load) {
            FileChooser.ExtensionFilter imgExtensionFilter = new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg");
            fileChooser.getExtensionFilters().add(imgExtensionFilter);
        }

        return fileChooser;
    }

    @FXML
    private void onSave() {
        //Show save dialog
        File file = createFileChooser(false).showSaveDialog(saveButton.getScene().getWindow());

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
                        case WEIGHTED:
                            String weightString = "'" + tileGrid[row][col].getWeight() + "'";
                            sb.append(weightString);
                            break;
                    }
                    //Add space if not last column
                    if (col < tileGrid[0].length - 1) sb.append(" ");
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
        File file = createFileChooser(true).showOpenDialog(saveButton.getScene().getWindow());

        if (file != null) {
            clearGrid();

            //Get file extension
            String extension = "";
            int i = file.getName().lastIndexOf('.');
            if (i > 0) {
                extension = file.getName().substring(i+1);
            }

            if (extension.equals("pfm")) {
                loadPFM(file);
            } else if (extension.equals("png") || extension.equals("jpg") || extension.equals("jpeg")) {
                loadImage(file);
            }

            //Save state
            addToHistory();
        }
    }

    private void loadPFM(File file) {
        try {
            FileReader fileReader = new FileReader(file);

            //Get dimensions of loaded map
            int rows = 0;
            int cols = -1;
            boolean startFound = false;
            boolean endFound = false;
            BufferedReader reader = new BufferedReader(fileReader);
            String line = reader.readLine();
            while (line != null) {
                rows++;

                String[] rowEntries = line.split(" ");

                for (String entry : rowEntries) {
                    //Ensure line only contains legal entries
                    if (!entry.matches("[wsf0]|([']([2-9]|[1-9][0-9]|100)['])")){       //w s f 0 '<int from 2-100>'
                        showError("File contains illegal entry (line " + rows + ": \"" + entry + "\")\n");
                        return;
                    }
                    //Check for more than one start tile
                    if (entry.matches("s")) {
                        if (startFound){
                            showError("Cannot have more than one start tile");
                            return;
                        } else {
                            startFound = true;
                        }
                    }
                    //Check for more than one end tile
                    if (entry.matches("f")) {
                        if (endFound){
                            showError("Cannot have more than one end tile");
                            return;
                        } else {
                            endFound = true;
                        }
                    }
                }

                //Ensure number of columns is the same in every row
                if (cols != -1 && rowEntries.length != cols){
                    showError("Number of columns is not consistent throughout file");
                    return;
                } else {
                    cols = rowEntries.length;
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

                for (String entry : line.split(" ")) {
                    switch (entry) {
                        case "0":
                            tileGrid[row][col].updateTileStyle(TileStyle.NONE);
                            break;
                        case "w":
                            tileGrid[row][col].updateTileStyle(TileStyle.WALL);
                            break;
                        case "s":
                            tileGrid[row][col].updateTileStyle(TileStyle.START);
                            break;
                        case "f":
                            tileGrid[row][col].updateTileStyle(TileStyle.FINISH);
                            break;
                        default:    //Weighted tile gets here
                            //Remove single quotes from around number
                            String weight = entry.replace("'", "");
                            tileGrid[row][col].updateTileStyle(TileStyle.WEIGHTED, Integer.parseInt(weight));
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

    private void loadImage(File file) {
        ImageParser imageParser = new ImageParser(file, MINROWS, MINCOLUMNS, MAXROWS, MAXCOLUMNS);

        rowSpinner.getValueFactory().setValue(imageParser.getRows());        //Automatically updates tileGrid via spinner listener
        colSpinner.getValueFactory().setValue(imageParser.getCols());

        imageParser.imageToMap(tileGrid);

        saveButton.setDisable(isGridEmpty());
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
            case "Bi-Directional":
                currentAlgorithm = AlgorithmType.BIDIRECTIONAL;
        }
        goButton.setDisable(false);     //Algorithm has been selected so Go button can be clicked
    }

    @FXML
    private void onMapSelect(ActionEvent event) {
        updateGrid();       //Clear previous tiles from grid

        switch (((MenuItem) event.getSource()).getId()) {
            case "mazeMap":
                new MazeGenerator(tileGrid).generateMap();
                break;
            case "randomMap":
                new PureRandomGenerator(tileGrid).generateMap();
                break;
            case "perlinMap":
                new PerlinGenerator(tileGrid).generateMap();
                break;
            default:
                return;
        }
        mapHistory.set(0, new MapSnapshot(tileGrid));
    }

    private void resetOperationHistory() {
        undoButton.setDisable(true);
        redoButton.setDisable(true);
        mapHistory.clear();
        mapHistory.add(new MapSnapshot(tileGrid));
        currentState = 0;
    }

    @FXML
    private void onUndo() {
        if (currentState > 0) {
            //Set the previous map in the list to be displayed
            currentState--;
            mapHistory.get(currentState).setAsMap(tileGrid);

            //Enable the redo button
            redoButton.setDisable(false);

            //Disable undo button if we are back at the start
            undoButton.setDisable(currentState == 0);

            saveButton.setDisable(isGridEmpty());
        }
    }

    @FXML
    private void onRedo() {
        if (currentState < mapHistory.size() - 1) {
            //Set the next map in the list to be displayed
            currentState++;
            mapHistory.get(currentState).setAsMap(tileGrid);

            //Enable the undo button
            undoButton.setDisable(false);

            //Disable redo button if we are at the last element
            redoButton.setDisable(currentState == mapHistory.size() - 1);

            saveButton.setDisable(isGridEmpty());
        }
    }

    private void fillRightClickDrag(Tile dragEndTile) {
        //Only want to fill if erasing, drawing walls, or drawing weighed tiles
        if (drawingMode == TileStyle.START || drawingMode == TileStyle.FINISH) return;

        //Find index of drag start & end tiles
        Integer dragStartRow = null, dragStartCol = null, dragEndRow = null, dragEndCol = null;
        for (int row = 0; row < tileGrid.length; row++) {
            for (int col = 0; col < tileGrid[0].length; col++) {
                if (tileGrid[row][col].objectEquals(dragStartTile)) {
                    dragStartRow = row;
                    dragStartCol = col;
                }
                if (tileGrid[row][col].objectEquals(dragEndTile)) {
                    dragEndRow = row;
                    dragEndCol = col;
                }
            }
        }

        try {
            //Find min & max row,col coordinates so drawing can be done correctly
            int drawStartRow = Math.min(dragStartRow, dragEndRow);
            int drawEndRow = Math.max(dragStartRow, dragEndRow);
            int drawStartCol = Math.min(dragStartCol, dragEndCol);
            int drawEndCol = Math.max(dragStartCol, dragEndCol);

            //Set Map to latest state
            mapHistory.get(currentState).setAsMap(tileGrid);

            //Fill in tiles
            for (int row = drawStartRow; row <= drawEndRow; row++) {
                for (int col = drawStartCol; col <= drawEndCol; col++) {
                    if (drawingMode == TileStyle.WEIGHTED) {
                        tileGrid[row][col].updateTileStyle(TileStyle.WEIGHTED, weightedTileValue);
                    } else {
                        tileGrid[row][col].updateTileStyle(drawingMode);
                    }
                }
            }

            saveButton.setDisable(isGridEmpty());
        }
        catch (NullPointerException ignored) {

        }
    }

    private void addToHistory() {
        //Save current state only if new state is different to previous
        if (mapHistory.size() == 0 || !areMapsEqual(tileGrid, mapHistory.get(currentState).snapShot)) {
            //Remove anything past the current point in the array
            //(happens when redo is used, then a draw is done -> want to get rid of state that was undone)
            if (currentState < mapHistory.size() - 1)
                mapHistory.subList(currentState + 1, mapHistory.size()).clear();
            redoButton.setDisable(true);

            //Save state to history list
            mapHistory.add(new MapSnapshot(tileGrid));
            currentState++;
            undoButton.setDisable(false);
        }
    }

    private boolean areMapsEqual(Tile[][] map1, Tile[][] map2) {

        for (int row = 0; row < map1.length; row++) {
            for (int col = 0; col < map1[0].length; col++) {
                if (!map1[row][col].equals(map2[row][col])) return false;
            }
        }

        return true;
    }
}
