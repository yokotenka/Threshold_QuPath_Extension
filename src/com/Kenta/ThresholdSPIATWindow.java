package com.Kenta;

import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.chart.LineChart;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import qupath.lib.classifiers.object.ObjectClassifier;
import qupath.lib.classifiers.object.ObjectClassifiers;
import qupath.lib.classifiers.object.ObjectClassifiers.ClassifyByMeasurementBuilder;
import qupath.lib.common.GeneralTools;
import qupath.lib.gui.QuPathGUI;
import qupath.lib.gui.dialogs.Dialogs;
import qupath.lib.gui.scripting.QPEx;
import qupath.lib.gui.viewer.QuPathViewer;
import qupath.lib.images.ImageData;
import qupath.lib.images.servers.ImageServer;
import qupath.lib.objects.PathObject;
import qupath.lib.objects.classes.PathClass;
import qupath.lib.objects.classes.PathClassFactory;
import qupath.lib.projects.Project;
import qupath.lib.projects.Projects;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import static qupath.lib.scripting.QP.*;

public class ThresholdSPIATWindow implements Runnable, ChangeListener<ImageData<BufferedImage>>, PropertyChangeListener {
    private Stage dialog;
    private final QuPathGUI qupath;
    private QuPathViewer viewer;
    private ImageServer<BufferedImage> server;
    private ImageData imageData;
    private Collection<PathObject> cells;


    private ObservableList<MarkerTableEntry> markers;
    private LineChart<Number, Number> lineChart;
    private StackPane pane;
    private Line prevLine;
    private TableCreator resultsTable;

    private GridPane gridPane;
//    private Button saveClassifier;
    private ComboBox<String> tumourBox;
    private ComboBox<String> comboBoxResults;
    private Button startButton;
    private TextField classifierName;
    private Button saveClassifierButton;




    private ArrayList<SPIATMarkerInformation> selectedMarkers;
    private ObservableList<MarkerTableEntry> selectedMarkersResults;
    private ArrayList<SPIATMarkerInformation> baselineMarkers;
    private SPIATMarkerInformation tumourMarker;

    private ThresholdSPIAT thresholdSPIAT;

    private Collection<PathObject> currentlySelected;

    private String title = "SPIAT Threshold";
    private TextField resultsTableName;
    private Button saveResultsTableButton;
    private Button chooseButton;

    public ThresholdSPIATWindow(QuPathGUI qupath) {
        this.qupath = qupath;
    }


    @Override
    public void run() {
//        if (dialog == null){
        this.dialog = createDialog();
//        }
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
        if (markerMeasurements.contains("Cell: Mean")) {
            defaultValue = "Cell: Mean";
        } else {
            defaultValue = markerMeasurements.get(0);
        }


        // For collecting options
        markers = FXCollections.observableArrayList();
        ObservableList<String> markerNames = FXCollections.observableArrayList();
        ObservableList<String> columnNames = FXCollections.observableArrayList(markerMeasurements);


        // Create the entries for the table rows
        for (int i = 0; i < server.nChannels(); i++) {
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
        gridPane = new GridPane();
        gridPane.setVgap(5);
        gridPane.setPadding(new Insets(10, 10, 10, 10));


        // For selecting the markers which threshold will be applied
        gridPane.add(createLabel("Select the markers to apply threshold"), col, row_col0++, 2, 1);
        gridPane.add(
                TableCreator.createTable(
                        markers,
                        TableCreator.createColumn("Marker", "name"),
                        TableCreator.createColumn("Select", "isSelectedForThreshold"),
                        TableCreator.createColumn("Select\nBaseline Marker", "isBaselineMarker"),
                        TableCreator.createColumn("Measurement", "measurement")
                ),
                col,
                row_col0++,
                2,
                2
        );
        row_col0++;
        gridPane.add(createLabel("Select tumour marker "), 0, row_col0, 1, 1);
        tumourBox = new ComboBox<>(markerNames);
        gridPane.add(tumourBox, 1, row_col0++, 1, 1);

        startButton = new Button("Run Threshold");
        gridPane.add(startButton, col, row_col0++);






        // Separator
        gridPane.add(new Separator(Orientation.VERTICAL), 2, 0, 1, row_col0++);


        // Results Selection
        int row_col3 = 0;
        gridPane.add(createLabel("Select marker to display results "), 3, row_col3, 2, 1);
        comboBoxResults = new ComboBox<>();
        gridPane.add(comboBoxResults, 6, row_col3++, 1, 1);

        // Add the Results line chart to grid pane
        pane = new StackPane();
        pane.getChildren().add(initialiseLineChart());
        gridPane.add(pane, 3, row_col3++, 4, 1);


        // Results label
//        gridPane.add(createLabel("Threshold and Expression Proportions"), 4, 0);

        // Results table
        resultsTable = new TableCreator();
        resultsTable.addColumn("Marker", "name");
        resultsTable.addColumn("Measurement", "comboBoxValue");
        resultsTable.addColumn("Threshold", "threshold");
        resultsTable.addColumn("Proportion", "proportion");
        resultsTable.addColumn("Count", "count");
        resultsTable.getTable().setPrefSize(400, 100);
        gridPane.add(resultsTable.getTable(), 3, row_col3++, 4, 1);


        gridPane.add(createLabel("Classifier Name "), 3, row_col3, 1, 1);
        classifierName = new TextField("SPIAT_Classifier");
        gridPane.add(classifierName, 4, row_col3, 1, 1);
        saveClassifierButton = new Button("Save & Apply");
        gridPane.add(saveClassifierButton, 5, row_col3++, 2, 1);


        gridPane.add(createLabel("Table Name "), 3, row_col3, 1, 1);
        resultsTableName = new TextField("SPIAT_Table_Results");
        gridPane.add(resultsTableName, 4, row_col3, 1, 1);
        chooseButton = new Button("Choose"); 
        gridPane.add(chooseButton, 5, row_col3, 1, 1);
        saveResultsTableButton = new Button("Save");
        gridPane.add(saveResultsTableButton, 6, row_col3++, 1, 1);


//        saveClassifier = new Button("Save");
//        gridPane.add(saveClassifier, 3, 4);

        startButton.setOnAction((event) -> {
            // This may not work
            QPEx.resetDetectionClassifications();

            imageData.getHierarchy().getDetectionObjects().forEach(it -> it.setPathClass(null));


            String tumourMarkerName = tumourBox.getValue();
            StringBuilder invalidInputs = new StringBuilder();
            StringBuilder measurementNotSelected = new StringBuilder();


            selectedMarkers = new ArrayList<>();
            selectedMarkersResults = FXCollections.observableArrayList();
            baselineMarkers = new ArrayList<>();
            lineChart.getData().clear();
            /*
             Get the selected markers, baseline markers and the tumour marker.
             Note: only markers which are selected for thresholding will be included
             as a baseline marker if selected.
            */
            boolean isBaselineAndTumour = false;
            boolean isInvalid = false;
            for (MarkerTableEntry marker : markers) {
                if (marker.isSelected()) {
                    selectedMarkers.add(marker.getMarkerInfo());
                    selectedMarkersResults.add(marker);
                }
                if (marker.isBaselineMarker()) {
                    baselineMarkers.add(marker.getMarkerInfo());
                }
                if (marker.isBaselineMarker() && !marker.isSelected()) {
                    invalidInputs.append(marker.getName()).append(", ");
                    isInvalid = true;
                }

                if (marker.getName().equals(tumourMarkerName)) {
                    tumourMarker = marker.getMarkerInfo();
                    if (marker.isBaselineMarker()) {
                        isBaselineAndTumour = true;
                        isInvalid = true;
                    }
                }
                if (!marker.setMeasurementName()) {
                    measurementNotSelected.append(marker.getName()).append(", ");
                    isInvalid = true;
                }
                marker.setThreshold(-5);
            }


            // Checks

            String errorMsg = "";
            if (!invalidInputs.toString().equals("")) {
                Dialogs.showErrorMessage(title, "The following markers were selected as baseline marker but not selected for thresholding:\n"
                        + invalidInputs.subSequence(0, invalidInputs.length() - 2) + "\n");
            }

            if (!measurementNotSelected.toString().equals("")) {
                Dialogs.showErrorMessage(title, "The following markers did not have a measurement selected: \n"
                        + measurementNotSelected.subSequence(0, measurementNotSelected.length() - 2) + "\n");
            }
//                if (tumourMarkerName == null) {
//                    errorMsg += "- " + "Tumour not selected";
//                }
            if (isBaselineAndTumour) {
                Dialogs.showErrorMessage(title, "Tumour is also selected as baseline marker. Cannot be both!\n");
            }
//                Dialogs.showErrorMessage(title, errorMsg);
//                return;
            if (isInvalid) {
                return; 
            }

            // Initialise the threshold algorithm
            thresholdSPIAT = new ThresholdSPIAT(cells, selectedMarkers, baselineMarkers, tumourMarker);

            // Calculate the thresholds
            double[] thresholds = thresholdSPIAT.calculateThresholds();

            // populate the graph
            populateGraph(thresholdSPIAT);

            // Set the path classes
//            setCellPathClass(thresholdSPIAT);

            comboBoxResults.setItems(
                    FXCollections.observableList(
                            selectedMarkers
                                    .stream()
                                    .map(i -> i.getMarkerName())
                                    .collect(Collectors.toCollection(ArrayList::new))
                    )
            );

//            fireHierarchyUpdate();
            resultsTable.getTable().getItems().clear();
            resultsTable.addItems(selectedMarkersResults);


        });


        currentlySelected = getSelectedObjects();
        comboBoxResults.setOnAction((event) -> {
            String selectedForResults = comboBoxResults.getValue();
            NumberAxis yAxis = (NumberAxis) lineChart.getYAxis();
            NumberAxis xAxis = (NumberAxis) lineChart.getXAxis();
            int i = 0;
            for (SPIATMarkerInformation marker : thresholdSPIAT.getMarkerInformationMap().values()) {
                if (marker.getMainSeries().getNode().isVisible()) {
                    lineChart.getData().get(i).getNode().setVisible(false);
                }

                if (marker.getMarkerName().equals(selectedForResults)) {
                    lineChart.getData().get(i).getNode().setVisible(true);
                    yAxis.setUpperBound(marker.getMaxDensity() * 1.25);
                    yAxis.setLowerBound(0);

                    // Change the upper bound of the intensity to suit the marker
                    xAxis.setUpperBound(marker.getThreshold() * 1.5);
                    xAxis.setLowerBound(0);

                    if (currentlySelected != null) {
                        imageData.getHierarchy().getSelectionModel().deselectObjects(currentlySelected);
                    }
                    List<PathObject> positive = cells.stream().parallel().filter(
                            it -> Double.compare(it.getMeasurementList().getMeasurementValue(marker.getMeasurementName()),marker.getThreshold())>0
                    ).collect(Collectors.toList());
                    imageData.getHierarchy().getSelectionModel().selectObjects(positive);
                    currentlySelected = positive;
                }
                i++;
            }

        });

        // Definitely exists a better way
        saveClassifierButton.setOnAction((event) -> {
            saveClassifiers();
            setCellPathClass();
        });

        chooseButton.setOnAction((event) -> {
                String ext = ".csv";
                String extDesc = "CSV (Comma delimited)";
                File pathOut = Dialogs.promptToSaveFile("Output file", Projects.getBaseDirectory(qupath.getProject()), "SPIAT_Table_Results" + ext, extDesc, ext);
                if (pathOut != null) {
                    if (pathOut.isDirectory())
                        pathOut = new File(pathOut.getAbsolutePath() + File.separator + "SPIAT_Table_Results" + ext);
                    resultsTableName.setText(pathOut.getAbsolutePath());
                }
        });

        saveResultsTableButton.setOnAction((event) -> {
                String fullFileName = resultsTableName.getText();
                try {
                    writeExcel(fullFileName, selectedMarkersResults);
                } catch (Exception e) {
                    e.printStackTrace();
                }
        });


        // Initialise stage
        Stage stage = new Stage();
        stage.initOwner(QuPathGUI.getInstance().getStage());
        stage.setScene(new Scene(gridPane));
        stage.setTitle(title);
        stage.setHeight(500);
        stage.setWidth(740);

        return stage;
    }


    private Label createLabel(String msg) {
        Label label = new Label(msg);
        label.setFont(javafx.scene.text.Font.font(14));
        label.setAlignment(Pos.CENTER);
        return label;
    }


    /**
     * Initialises the line chart
     */
    private LineChart<Number, Number> initialiseLineChart() {

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
        lineChart.setPrefSize(400,100 );

        return lineChart;
    }

    /**
     * Populates the line chart with the calculations
     *
     * @param thresholdSPIAT line chart
     */
    private void populateGraph(ThresholdSPIAT thresholdSPIAT) {
        lineChart.getData().clear();
        int i = 0;
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


    private List<String> getMarkerMeasurementNames() {

        // Do something for when no cell detected
        if (cells == null) {
            return null;
        }

        PathObject cell = (PathObject) cells.toArray()[0];

        String markerName = server.getChannel(0).getName();
        List<String> measurementList = cell.getMeasurementList().getMeasurementNames();

        // Potentially could be a source of error #################################################
        return measurementList.stream()
                .parallel()
                .filter(it -> it.contains(markerName+":"))
                .map(it -> it.substring(markerName.length() + 2))
                .collect(Collectors.toList());
    }


    private void setCellPathClass() {
        for (SPIATMarkerInformation marker : thresholdSPIAT.getMarkerInformationMap().values()) {
            List<PathObject> positive = cells.stream().parallel().filter(it ->
                    it.getMeasurementList().getMeasurementValue(marker.getMeasurementName())
                            > marker.getThreshold()).collect(Collectors.toList());

            positive.forEach(it -> {
                        PathClass currentClass = it.getPathClass();
                        PathClass pathClass;

                        if (currentClass == null) {
                            pathClass = PathClassFactory.getPathClass(marker.getMarkerName());
                        } else {
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

    /**
     * Checks if all the classification names in the array are in the pathclass
     *
     * @param pathClass
     * @param classificationNames
     * @return
     */
    private boolean checkForClassifications(PathClass pathClass, String... classificationNames) {
        if (classificationNames.length == 0)
            return false;
        for (String name : classificationNames) {
            if (!checkForSingleClassification(pathClass, name))
                return false;
        }
        return true;
    }


    public void writeExcel(String fileName, ObservableList<MarkerTableEntry> markers) throws Exception {
        try {
            File file = new File(fileName);
            Writer writer = new BufferedWriter(new FileWriter(file));
            writer.write("Marker,Measurement,Threshold,Proportion,Count\n");
            for (MarkerTableEntry marker : markers) {

                String text = marker.getName() + ","
                        + marker.getComboBoxValue() + ","
                        + marker.getThreshold() + ","
                        + marker.getProportion() + ","
                        + marker.getCount() + "\n";

                writer.write(text);
            }
            Dialogs.showInfoNotification(title, "Results table saved");
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }





    /**
     * Try to save the classifier & return the name of the saved classifier if successful
     * @return
     */



    private void saveClassifiers(){
        var project = qupath.getProject();

        if (project == null) {
            Dialogs.showErrorMessage(title, "You need a project to save this classifier!");
            return;
        }

        String name = GeneralTools.stripInvalidFilenameChars(classifierName.getText());
        if (name.isBlank()) {
            Dialogs.showErrorMessage(title, "Please enter a name for the classifier!");
            return;
        }

        try {
            var classifierNames = project.getObjectClassifiers().getNames();

            if (classifierNames.stream().anyMatch(it -> it.contains("SPIAT") )) {
                if (!Dialogs.showConfirmDialog(title, "There are already classifiers with the prefix '" + classifierName.getText() + "'. Do you want to overwrite these?"))
                    return;
            }
            ObservableList<ClassifierWrapper<BufferedImage>> array = FXCollections.observableArrayList();
            for (MarkerTableEntry marker : selectedMarkersResults) {
                String markerClassifierName = makeClassifierName(marker.getName());



                var classifier = updateClassifier(marker);
                project.getObjectClassifiers().put(markerClassifierName, classifier);

                var wrap = wrapClassifier(markerClassifierName);
                if (!array.contains(wrap)){
                    array.add(wrap);
                }
            }

            tryToSave(project, array, name);

//            Dialogs.showInfoNotification(title, "Saved individual classifiers with prefix '" + classifierName.getText() + "'");
        } catch (Exception e) {
            Dialogs.showErrorNotification(title, e);
            return;
        }

    }


    private String makeClassifierName(String markerName){
        return classifierName.getText() + "_" + markerName;
    }


    private ObjectClassifier<BufferedImage> updateClassifier(MarkerTableEntry marker) {
        String measurement = marker.getMarkerInfo().getMeasurementName();
        double threshold = marker.getThreshold();
        PathClass classAbove = PathClassFactory.getPathClass(marker.getName());
        var classEquals = classAbove; // We use >= and if this changes the tooltip must change too!

        if (measurement == null || Double.isNaN(threshold))
            return null;

        return new ClassifyByMeasurementBuilder<BufferedImage>(measurement)
                .threshold(threshold)
                .above(classAbove)
                .equalTo(classEquals)
                .build();
    }


    private ProjectClassifierWrapper<BufferedImage> wrapClassifier(String classifierName){
        return new ProjectClassifierWrapper<>(qupath.getProject(), classifierName);
    }


    /*
    * Code taken from Pete Bankhead's "CreateCompositeClassifier.java"
    * */
    private ObjectClassifier<BufferedImage> tryToBuild(Collection<ClassifierWrapper<BufferedImage>> wrappers) throws IOException {
        var classifiers = new LinkedHashSet<ObjectClassifier<BufferedImage>>();
        for (var wrapper : wrappers) {
            classifiers.add(wrapper.getClassifier());
        }
        if (classifiers.size() < 2) {
            Dialogs.showErrorMessage(title, "At least two different classifiers must be selected to create a composite!");
            return null;
        }
        return ObjectClassifiers.createCompositeClassifier(classifiers);
    }


    private ObjectClassifier<BufferedImage> tryToSave(Project<BufferedImage> project, Collection<ClassifierWrapper<BufferedImage>> wrappers, String name) {
        try {
            var composite = tryToBuild(wrappers);
            if (composite == null)
                return null;

            name = name == null ? null : GeneralTools.stripInvalidFilenameChars(name);
            if (project != null && name != null && !name.isBlank()) {
                if (project.getObjectClassifiers().contains(name)) {
                    if (!Dialogs.showConfirmDialog(title, "Overwrite existing classifier called '" + name + "'?"))
                        return null;
                }
                project.getObjectClassifiers().put(name, composite);
                Dialogs.showInfoNotification(title, "Classifier written to project as " + name);
            } else {
                var file = Dialogs.promptToSaveFile(title, null, name, "JSON", ".json");
                if (file != null) {
                    ObjectClassifiers.writeClassifier(composite, file.toPath());
                    Dialogs.showInfoNotification(title, "Classifier written to " + file.getAbsolutePath());
                }
            }
            return composite;
        } catch (Exception e) {
            Dialogs.showErrorMessage(title, e);
            return null;
        }

    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }

    @Override
    public void changed(ObservableValue<? extends ImageData<BufferedImage>> observableValue, ImageData<BufferedImage> bufferedImageImageData, ImageData<BufferedImage> t1) {

    }









}
