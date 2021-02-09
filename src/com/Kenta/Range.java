package com.Kenta;


public class Range {
    private Double min;
    private Double max;

    public Range(Double min, Double max){
        if (min.compareTo(max) >= 0 ){
            // Do error handling for when min is greater than equal to max
        }

        this.min = min;
        this.max= max;
    }

    public double getMax() {
        return max;
    }

    public double getMin() {
        return min;
    }

    public double getDifference(){
        return max - min;
    }

}