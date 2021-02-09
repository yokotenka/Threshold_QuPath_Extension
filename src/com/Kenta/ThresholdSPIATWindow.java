package com.Kenta;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import com.Kenta.MarkerTableEntry;
import com.Kenta.TableCreator;
import com.Kenta.SPIATMarkerInformation;
import com.Kenta.ThresholdSPIAT;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.objects.PathDetectionObject;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.objects.classes.PathClassFactory;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import qupath.lib.gui.commands.Commands;

import static qupath.lib.scripting.QP.*;

public class ThresholdSPIATWindow implements Runnable{
    private Stage dialog;
    private final QuPathGUI qupath;
    private QuPathViewer viewer;
    private ImageServer<BufferedImage> server;
    private ImageData imageData;
    private Collection<PathObject> cells;


    private ObservableList<MarkerTableEntry> markers;
    private LineChart<Number, Number> lineChart;
    private TableView<MarkerTableEntry> resultsTable;

    private ArrayList<SPIATMarkerInformation> selectedMarkers;
    private ArrayList<SPIATMarkerInformation> baselineMarkers;
    private SPIATMarkerInformation tumourMarker;

    private ThresholdSPIAT thresholdSPIAT;

    private Collection<PathObject> currentlySelected;

    public ThresholdSPIATWindow(QuPathGUI qupath){
        this.qupath = qupath;
    }


    @Override
    public void run() {
        if (dialog == null){
            this.dialog = createDialog();
        }
        dialog.show();
    }


    protected Stage createDialog() {



        // Get all the QuPath data
        viewer = qupath.getViewer();
        server = viewer.getServer();
        imageData = viewer.getImageData();
        cells = imageData.getHierarchy().getCellObjects();


        // Collect the measurement names
        List<String> markerMeasurements = getMarkerMeasurementNames();
        String defaultValue;


        // Set the default Measurement to Cell mean if it exists
        assert markerMeasurements != null;
        if (markerMeasurements.contains("Cell: Mean")){
            defaultValue = "Cell: Mean";
        } else{
            defaultValue = markerMeasurements.get(0);
        }


        // For collecting options
        markers = FXCollections.observableArrayList();
        ObservableList<String> markerNames = FXCollections.observableArrayList();
        ObservableList<String> columnNames = FXCollections.observableArrayList(markerMeasurements);


        // Create the entries for the table rows
        for (int i=0; i<server.nChannels(); i++){
            String markerName = server.getChannel(i).getName();
            markerNames.add(markerName);

            markers.add(
                new MarkerTableEntry(
                    server.getChannel(i).getName(),
                    true,
                    false,
                    columnNames,
                    defaultValue
                    )
                );
        }


        // Create the grid
        //Settings to control the dialog boxes for the GUI
        int col = 0;

        // Row number for the current column
        int row_col0 = 0;
//        int row_col1 = 0;

        // Initialise the grid
        GridPane gridPane = new GridPane();
        gridPane.setVgap(5);
        gridPane.setPadding(new Insets(10, 10, 10, 10));


        // For selecting the markers which threshold will be applied
        gridPane.add(createLabel("Select the markers to apply threshold:"),col, row_col0++, 1, 1);

        // Create and add table to grid pane

        gridPane.add(
                TableCreator.createTable(
                        markers,
                        TableCreator.createColumn("Marker", "name"),
                        TableCreator.createColumn("Select", "isSelectedForThreshold"),
                        TableCreator.createColumn("Select\n Baseline Marker", "isBaselineMarker"),
                        TableCreator.createColumn("Measurement", "measurement")
                ),
                col,
                row_col0++
        );

        // ComboBox for selecting tumour marker
        gridPane.add(createLabel("Select tumour marker:"), 0, row_col0++, 3, 1);
        ComboBox<String> tumourBox = new ComboBox<>(markerNames);
        gridPane.add(tumourBox,0, row_col0++) ;


        // Start button
        Button startButton = new Button("Run Threshold");
        gridPane.add(startButton, col, row_col0++);


        // Separator
        gridPane.add(new Separator(Orientation.VERTICAL), 1, 0, 1, row_col0++);


        // Results Selection
        int row_col2 = 0;
        gridPane.add(createLabel("Select marker to display results:"), 2, row_col2++);
        ComboBox<String> comboBoxResults = new ComboBox<>(markerNames);
        gridPane.add(comboBoxResults, 3, 0);

        // Add the Results line chart to grid pane
        gridPane.add(initialiseLineChart(), 2, row_col2++, 2, 1);


        // Results Stats
        gridPane.add(createLabel("Threshold and Expression Proportions"), 4, 0);



        resultsTable = new TableView<>();
        gridPane.add(resultsTable, 4, 1);

        startButton.setOnAction((event) -> {
            // This may not work
            resetDetectionClassifications();

            String tumourMarkerName = tumourBox.getValue();
            StringBuilder invalidInputs = new StringBuilder();
            StringBuilder measurementNotSelected = new StringBuilder();


            ///////// Add in a way to save options and load options. also save results
            selectedMarkers = new ArrayList<>();
            baselineMarkers = new ArrayList<>();
            lineChart.getData().clear();
            /*
             Get the selected markers, baseline markers and the tumour marker.
             Note: only markers which are selected for thresholding will be included
             as a baseline marker if selected.
            */
            for (MarkerTableEntry marker : markers) {
                if (marker.isSelected()){
                    selectedMarkers.add(marker.getMarkerInfo());
                }
                if (marker.isBaselineMarker()){
                    baselineMarkers.add(marker.getMarkerInfo());
                }
                if (marker.isBaselineMarker() && !marker.isSelected()){
                    invalidInputs.append(marker.getName()).append(", ");

                }

                if (marker.getName().equals(tumourMarkerName)){
                    tumourMarker = marker.getMarkerInfo();
                }
                if (!marker.setMeasurementName()){
                    measurementNotSelected.append(marker.getName()).append(", ");
                }
            }


            // Checks
            if (tumourMarkerName == null || !invalidInputs.toString().equals("")){
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setHeaderText("Input not valid");
                String errorMsg = "";
                if (invalidInputs.toString().equals("")){
                    errorMsg = "The following markers were selected as baseline marker but not selected for thresholding:\n"
                            + invalidInputs.subSequence(0, invalidInputs.length()-2) + "\n";
                }

                if (measurementNotSelected.toString().equals("")){
                    errorMsg = "The following markers did not have a measurement selected: \n"
                            + measurementNotSelected.subSequence(0, measurementNotSelected.length()-2)+"\n";
                }
                if (tumourMarkerName == null){
                    errorMsg += "- "+"Tumour not selected";
                }
                errorAlert.setContentText(errorMsg);
                errorAlert.showAndWait();
            }

            // Initialise the threshold algorithm
            thresholdSPIAT = new ThresholdSPIAT(cells, selectedMarkers, baselineMarkers, tumourMarker);

            // Calculate the thresholds
            double[] thresholds = thresholdSPIAT.calculateThresholds();

            // populate the graph
            populateGraph(thresholdSPIAT);

            // Set the path classes
            setCellPathClass(thresholdSPIAT);

            fireHierarchyUpdate();


//            resultsTable = TableCreator.createTable(
//                    FXCollections.observableArrayList(selectedMarkers),
//                    TableCreator.createColumn("Marker", "name"),
//                    TableCreator.createColumn("Threshold", "threshold"),
//                    TableCreator.createColumn("Proportion", "proportion")
//            );
//            gridPane.add(resultsTable, 4, 1);
        });


        currentlySelected = getSelectedObjects();
        comboBoxResults.setOnAction((event) -> {
            String selectedForResults = comboBoxResults.getValue();
            NumberAxis lineChartYAxis = (NumberAxis) lineChart.getYAxis();
            NumberAxis lineChartXAxis = (NumberAxis) lineChart.getXAxis();
            int i=0;
            for (SPIATMarkerInformation marker: thresholdSPIAT.getMarkerInformationMap().values()){
                if (marker.getMainSeries().getNode().isVisible()){
                    lineChart.getData().get(i).getNode().setVisible(false);
                }

                if (marker.getMarkerName().equals(selectedForResults)){
                    lineChart.getData().get(i).getNode().setVisible(true);
                    lineChartYAxis.setUpperBound(marker.getMaxDensity() * 1.25);
                    lineChartYAxis.setLowerBound(0);

                    // Change the upper bound of the intensity to suit the marker
                    lineChartXAxis.setUpperBound(marker.getMaxIntensity());
                    lineChartXAxis.setLowerBound(0);

                    if (currentlySelected != null) {
                        imageData.getHierarchy().getSelectionModel().deselectObjects(currentlySelected);
                    }
                    List<PathObject> positive = cells.stream().filter(
                            it -> checkForClassifications(it.getPathClass(), selectedForResults)
                            ).collect(Collectors.toList());
                    imageData.getHierarchy().getSelectionModel().selectObjects(positive);
                    currentlySelected = positive;
                }
                i++;
            }

        });


        // Initialise stage
        Stage stage = new Stage();
        stage.initOwner(QuPathGUI.getInstance().getStage());
        stage.setScene(new Scene( gridPane));
        stage.setTitle("SPIAT Thresholding");
        return stage; 
    }


    private Label createLabel(String msg){
        Label label = new Label(msg);
        label.setFont(javafx.scene.text.Font.font(15));
        label.setAlignment(Pos.CENTER);
        return label;
    }




    /**
     * Initialises the line chart
     */
    private LineChart<Number, Number> initialiseLineChart(){

        final NumberAxis xAxis = new NumberAxis();
        final NumberAxis yAxis = new NumberAxis();
        xAxis.setAutoRanging(false);
        xAxis.setUpperBound(255);
        xAxis.setLabel("Intensity");
        yAxis.setAutoRanging(false);

        //creating the chart
        lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Density Estimation");
        lineChart.setCreateSymbols(false);
        lineChart.setLegendVisible(false);

        return lineChart;
    }

    /**
     * Populates the line chart with the calculations
     * @param thresholdSPIAT line chart
     */
    private void populateGraph(ThresholdSPIAT thresholdSPIAT){

        int i=0;
        for (SPIATMarkerInformation marker : thresholdSPIAT.getMarkerInformationMap().values()) {
            // Marker name
            String markerName = marker.getMarkerName();

            // For the actual density
            Series<Number, Number> series = new Series<>();
            for (int j = 0; j < thresholdSPIAT.getX().length; j++) {
                series.getData().add(new Data<>(thresholdSPIAT.getValueAtX(j), marker.getEstimatedDensity()[j]));
            }
//            series.getNode().setVisible(false);
            marker.setMainSeries(series);
            lineChart.getData().add(series);
            lineChart.getData().get(i++).getNode().setVisible(false);

        }
    }


    private List<String> getMarkerMeasurementNames(){

        // Do something for when no cell detected
        if (cells == null){
            return null;
        }

        PathObject cell = (PathObject) cells.toArray()[0];

        String markerName = server.getChannel(0).getName();
        List<String> measurementList = cell.getMeasurementList().getMeasurementNames();

        // Potentially could be a source of error #################################################
        return measurementList.stream()
                .filter(it -> it.contains(markerName))
                .map(it -> it.substring(markerName.length()+2))
                .collect(Collectors.toList());
    }


    private void setCellPathClass(ThresholdSPIAT thresholdSPIAT){
        for (SPIATMarkerInformation marker : thresholdSPIAT.getMarkerInformationMap().values()){
            List<PathObject> positive = cells.stream().filter(it ->
                    it.getMeasurementList().getMeasurementValue(marker.getMeasurementName())
                            > marker.getThreshold()).collect(Collectors.toList());

            positive.forEach(it-> {
                        PathClass currentClass = it.getPathClass();
                        PathClass pathClass;

                        if (currentClass == null){
                            pathClass = PathClassFactory.getPathClass(marker.getMarkerName());
                        } else{
                            pathClass = PathClassFactory.getDerivedPathClass(
                                    currentClass,
                                    marker.getMarkerName(),
                                    null);
                        }
                        it.setPathClass(pathClass);
                    }
                );
        }
    }

    private boolean checkForSingleClassification(PathClass pathClass, String classificationName) {
        if (pathClass == null)
            return false;
        if (pathClass.getName().equals(classificationName))
            return true;
        return checkForSingleClassification(pathClass.getParentClass(), classificationName);
    }

    /** Checks if all the classification names in the array are in the pathclass
     * @param pathClass
     * @param classificationNames
     * @return
     */
    private boolean checkForClassifications(PathClass pathClass, String...classificationNames) {
        if (classificationNames.length == 0)
            return false;
        for (String name : classificationNames) {
            if (!checkForSingleClassification(pathClass, name))
                return false;
        }
        return true;
    }

}
