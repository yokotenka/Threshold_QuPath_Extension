package com.Kenta;

import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import com.Kenta.SPIATMarkerInformation;

public class MarkerTableEntry {

    private CheckBox isSelectedForThreshold;
    private CheckBox isBaselineMarker;
    private ComboBox<String> measurement;


    private SPIATMarkerInformation markerInfo;


    /** Constructor
     * @param name
     * @param isSelected
     */
    public MarkerTableEntry(String name, boolean isSelected){
        this.markerInfo = new SPIATMarkerInformation(name);

        this.isSelectedForThreshold = new CheckBox();
        this.isSelectedForThreshold.setSelected(isSelected);
    }

    public MarkerTableEntry(String name, boolean isSelected, boolean isBaselineMarker, ObservableList<String> list, String defaultValue){
        this.markerInfo = new SPIATMarkerInformation(name);

        this.isSelectedForThreshold = new CheckBox();
        this.isSelectedForThreshold.setSelected(isSelected);
        this.isBaselineMarker = new CheckBox();
        this.isBaselineMarker.setSelected(isBaselineMarker);

        // Change so that the marker name is appended in fron of lsit elements.
        this.measurement = new ComboBox<String>(list);
        measurement.setValue(defaultValue);
    }


    // Getters needed for PropertyValueFactory<>() during table creation

    public String getName() {
        return markerInfo.getMarkerName();
    }

    public CheckBox getIsSelectedForThreshold() {
        return isSelectedForThreshold;
    }

    public ComboBox getMeasurement() {
        return measurement;
    }

    public CheckBox getIsBaselineMarker() {
        return isBaselineMarker;
    }

    public double getThreshold() {
        return markerInfo.getThreshold();
    }

    public double getProportion() {
        return markerInfo.getExpressionProportion();
    }

    public SPIATMarkerInformation getMarkerInfo() {
        return markerInfo;
    }

    // Setters

    public void setName(String name) {
        this.markerInfo.setMarkerName(name);
    }

    public void setIsSelectedForThreshold(CheckBox isSelectedForThreshold) {
        this.isSelectedForThreshold = isSelectedForThreshold;
    }

    public void setMeasurement(ComboBox measurement) {
        this.measurement = measurement;
    }

    public boolean setMeasurementName(){

        this.markerInfo.setMeasurementName(this.getName()+": "+measurement.getValue());
        if (measurement.getValue() != null)
            return true;
        return false;
    }

    public void setThreshold(double threshold) {
        this.markerInfo.setThreshold(threshold);
    }

    public void setProportion(double proportion) {
        this.markerInfo.setExpressionProportion(proportion);
    }

    // For my use
    public boolean isSelected() {
        return isSelectedForThreshold.isSelected();
    }

    public boolean isBaselineMarker(){
        return isBaselineMarker.isSelected();
    }

    public String getComboBoxValue(){
        return measurement.getValue();
    }
}