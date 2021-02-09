package com.Kenta;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

/*
 * ######################################### Kernel Density Estimation #################################################
 */
/** A class for Kernel Density Estimation. There are no KDE packages that come pre-installed with QuPath hence was
 * easier to write one which didn't require installing another package.
 *
 * Have not yet implemented solutions for the bounded bias.
 *
 * @author Kenta
 */
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
public class KernelDensityEstimation {
    /** The standard deviation which will be used for individual normal distributions */
    private double bandWidth;

    /** The kernel function name. Defaults to the Gaussian kernel. In reality whatever kernel works the same. */
    private String kernelName ="gaussian";

    /** Weighting for each data point. Not implemented yet. */
    private double[] weights = null;

    /** The range of the data points */
    private Range range = null;

    /** The estimated kernel density array */
    private double[] estimate;

    /** The number of data points to be included in the estimated array*/
    private int n = 2048;

    /** The data points which the estimation is performed on */
    private double[] x;


    /** Constructor
     * @param x The data points
     * @param min Minimum in the range.
     * @param max Maximum in the range.
     */
    KernelDensityEstimation(double[] x, double min, double max, int n){
        this.x = x;
        setBandWidthMethod("Default");
        this.range = new Range(min, max);
        this.n = n;
    }

    /** Estimation
     * @return kernelDensity The kernel density estimation for the given data, x.
     */
    public double[] estimate(){
        double[] kernelDensity = new double[n];
        double[] currentDensity = new double[n];
        boolean isFirstElement = true;


        // Initialise kernel distribution.
        NormalDistribution kernel = new NormalDistribution(bandWidth);

        // Iterate through every datapoint
        for (double point : x){

            // Set the mean value for the kernel distribution.
            kernel.setMean(point);

            // Get the distribution of the kernel for the given range
            currentDensity = kernel.getDensityOfRange(range, n);

            // Calculate the kernel density
            if (isFirstElement){
                System.arraycopy(currentDensity, 0, kernelDensity , 0, n);
                isFirstElement = false;
            } else{
                for (int i=0; i<n; i++){
                    kernelDensity[i] += currentDensity[i];
                }
            }
        }

        // Normalise
        for (int i=0; i<n; i++) {
            kernelDensity[i] = kernelDensity[i] / x.length;
        }

        return kernelDensity;
    }

    /** Private method which will calculate the bandwidth to be used. Probably should create another class for this.
     * */
    private void setBandWidthMethod(String bandWidthName) throws UnsupportedOperationException {

        if (bandWidthName.equals("Silverman")){
            throw new UnsupportedOperationException("Silverman not yet supported");
        } else if (bandWidthName.equals("SJ")){
            throw new UnsupportedOperationException("Sheather & Jones not yet supported");
        } else {
            // Can change this if need be.
            DescriptiveStatistics da = new DescriptiveStatistics(x);
            double xStandardDeviation = da.getStandardDeviation();
            double iqr = da.getPercentile(75) - da.getPercentile(25);
            this.bandWidth = 0.9 * Math.min(xStandardDeviation, iqr/1.34) * Math.pow(x.length, (double) -1/5.0);
        }
    }

    /** Getter for the bandwidth being used.
     * @return bandWidth
     */
    double getBandWidth(){
        return this.bandWidth;
    }
}