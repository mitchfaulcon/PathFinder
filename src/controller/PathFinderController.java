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
    @FXML JFXToggleNode EraserToggle;
    @FXML JFXToggleNode WallToggle;
    @FXML JFXToggleNode StartToggle;
    @FXML JFXToggleNode FinishToggle;
    @FXML JFXButton goButton;
    @FXML JFXButton saveButton;
    @FXML JFXButton loadButton;
    @FXML JFXComboBox<String> algorithmComboBox;

    private static int MINROWS = 5;
    private static int MINCOLUMNS = 5;
    private static int MAXROWS = 80;
    private static int MAXCOLUMNS = 80;

    public enum TileStyle {START, FINISH, WALL, NONE, SEARCHED, PATH}

    private TileStyle drawingMode;
    private Tile[][] tileGrid;               //Stores all tiles

    private AlgorithmType currentAlgorithm;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Setup spinners
        rowSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MINROWS, MAXROWS));
        colSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(MINCOLUMNS, MAXCOLUMNS));
        rowSpinner.valueProperty().addListener((observer, oldValue, newValue)->UpdateGrid());
        colSpinner.valueProperty().addListener((observer, oldValue, newValue)->UpdateGrid());
        rowSpinner.getValueFactory().setValue(10);
        colSpinner.getValueFactory().setValue(10);

        //Erase mode is default
        drawingMode = TileStyle.NONE;
        EraserToggle.setSelected(true);

        saveButton.setDisable(true);

        //Setup algorithm selection combo box
        algorithmComboBox.getItems().add("A*");
        algorithmComboBox.getItems().add("BFS");
        algorithmComboBox.getItems().add("DFS");
        algorithmComboBox.getItems().add("Dijkstra");
        algorithmComboBox.getItems().add("Greedy");
        goButton.setDisable(true);      //Disable Go button until algorithm is selected
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

        saveButton.setDisable(IsGridEmpty());
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

        saveButton.setDisable(IsGridEmpty());
    }

    /**
     * @return True if each tile on the grid is empty
     *         False if there is at least one of a wall, start, or finish tile
     */
    private boolean IsGridEmpty(){
        for (Tile[] tiles : tileGrid) {
            for (Tile t : tiles) {
                TileStyle tileStyle = t.GetTileStyle();
                if (tileStyle == TileStyle.WALL || tileStyle == TileStyle.START || tileStyle == TileStyle.FINISH) return false;
            }
        }
        return true;
    }

    @FXML
    private void OnDrawToggle(ActionEvent actionEvent) {
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

    @FXML
    private void OnGoButton() {
        goButton.setDisable(true);      //Disable Go button while algorithm is running

        RET_CODE ret = Map.GetInstance().RunAlgorithm(tileGrid, currentAlgorithm);
        switch (ret){
            case NO_PATH:
                ShowError("No path could be found");
                break;
            case NO_START:
                ShowError("There is no start tile on the grid");
                break;
            case NO_END:
                ShowError("There is no goal tile on the grid");
                break;
            case SUCCESS:
                System.out.println("Path found");
                break;
        }

        goButton.setDisable(false);     //Re-enable Go button
    }

    private FileChooser CreateFileChooser() {
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
    private void OnSave() {
        //Show save dialog
        File file = CreateFileChooser().showSaveDialog(saveButton.getScene().getWindow());

        if (file != null) {
            SaveToFile(file);
        }
    }

    private void SaveToFile(File file) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(file);

            for (int row = 0; row < tileGrid.length; row++) {
                StringBuilder sb = new StringBuilder();
                for (int col = 0; col < tileGrid[0].length; col++) {
                    TileStyle tileStyle = tileGrid[row][col].GetTileStyle();
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
    private void OnLoad() {
        //Show load dialog
        File file = CreateFileChooser().showOpenDialog(saveButton.getScene().getWindow());

        if (file != null) {
            LoadFile(file);
        }
    }

    private void LoadFile(File file) {
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
                    ShowError("File contains illegal character (line " + rows + ")");
                    return;
                }

                //Ensure number of columns is the same in every row
                if (cols != -1 && line.length() != cols){
                    ShowError("Number of columns is not consistent throughout file");
                    return;
                } else {
                    cols = line.length();
                }

                line = reader.readLine();
            }
            //Ensure number of rows are within allowed range
            if (rows > MAXROWS){
                ShowError("Number of rows is too large");
                return;
            }
            if (rows < MINROWS){
                ShowError("Number of rows is too small");
                return;
            }
            //Ensure number of columns are within allowed range
            if (cols > MAXCOLUMNS){
                ShowError("Number of columns is too large");
                return;
            }
            if (cols < MINCOLUMNS){
                ShowError("Number of columns is too small");
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
                            tileGrid[row][col].UpdateTileStyle(TileStyle.NONE);
                            break;
                        case 'w':
                            tileGrid[row][col].UpdateTileStyle(TileStyle.WALL);
                            break;
                        case 's':
                            tileGrid[row][col].UpdateTileStyle(TileStyle.START);
                            break;
                        case 'f':
                            tileGrid[row][col].UpdateTileStyle(TileStyle.FINISH);
                            break;
                    }
                    col++;
                }

                row++;
                line = reader.readLine();
            }

            saveButton.setDisable(IsGridEmpty());

            reader.close();
        } catch (IOException ex) {
            Logger.getLogger(PathFinderController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void ShowError(String message) {
        Alert alert = new Alert(AlertType.ERROR, message);
        alert.showAndWait();
    }

    @FXML
    private void OnAlgorithmSelect() {
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
