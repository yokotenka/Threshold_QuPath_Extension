package com.Kenta;
import javafx.scene.chart.XYChart.Series;

public class SPIATMarkerInformation {

    private String markerName;
    private String measurementName;
    private boolean isTumour;
    private double maxIntensity;
    private double maxDensity;
    private double threshold = -1;
    private double[] estimatedDensity;
    private double expressionProportion;
    private int count;

    private Series mainSeries;
    private Series thresholdLine;

    // Add in series as well
    /**
     * Stores all the information for each of the markers.
     */
    public SPIATMarkerInformation(String markerName, String measurementName, boolean isTumour){
        this.markerName = markerName;
        this.measurementName = measurementName;
        this.isTumour = isTumour;

        mainSeries = new Series();
        thresholdLine = new Series();

    }

    public SPIATMarkerInformation(String markerName){
        this.markerName = markerName;
    }

    // Setters

    public void setMarkerName(String markerName) {
        this.markerName = markerName;
    }
    public void setIsTumour(boolean tumour) {
        isTumour = tumour;
    }
    public void setMeasurementName(String measurementName) {
        this.measurementName = measurementName;
    }
    public void setMaxIntensity(double intensity){
        this.maxIntensity = intensity;
    }
    public void setMaxDensity(double density){
        this.maxDensity = density;
    }
    public void setThreshold(double threshold){
        this.threshold = threshold;
    }
    public void setEstimatedDensity(double[] estimatedDensity){
        this.estimatedDensity = estimatedDensity;
    }
    public void setExpressionProportion(double expressionProportion) { this.expressionProportion = expressionProportion; }
    public void setMainSeries(Series series){
        this.mainSeries = series;
    }
    public void setThresholdLine(Series line){
        this.thresholdLine = line;
    }
    public void setCount(int count){this.count = count; }

    // Getters
    public int getCount(){return count;}
    public double getExpressionProportion() {
        return expressionProportion;
    }
    public String getMarkerName() {
        return markerName;
    }
    public double getMaxIntensity(){
        return maxIntensity;
    }
    public double getMaxDensity() {
        return maxDensity;
    }
    public double getThreshold() {
        return threshold;
    }
    public String getMeasurementName() {
        return measurementName;
    }
    public double[] getEstimatedDensity() {
        return estimatedDensity;
    }
    public Series getMainSeries() {
        return mainSeries;
    }
    public Series getThresholdLine() {
        return thresholdLine;
    }
    public boolean isTumour() {
        return isTumour;
    }
}
