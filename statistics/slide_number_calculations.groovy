/**
 * Slide number calculations using coefficient of variation (CV)
 * @author Janan Arslan
 * 
 * Last edit: 9th August 2025

*/

import qupath.lib.objects.PathObject
import qupath.lib.measurements.MeasurementList
import java.util.stream.Collectors
import static java.lang.Math.sqrt

// Parameters
double zScore = 1.96 // For 95% confidence
double desiredME = 5 // Desired Margin of Error (percentage)

// Collect cell counts from all slides
def slides = getProject().getImageList()
def slideCellCounts = []

for (slide in slides) {
    setBatchProjectEntry(slide)
    def detections = getDetectionObjects()
    def cellCounts = detections.collect { it.getMeasurementList().getMeasurementValue("Cell Count") }
    slideCellCounts.add(cellCounts.sum()) // Total cell count for the slide
}

// Calculate mean (μ) and standard deviation (σ) of cell counts
def mean = slideCellCounts.sum() / slideCellCounts.size()
def stdDev = Math.sqrt(slideCellCounts.collect { Math.pow(it - mean, 2) }.sum() / slideCellCounts.size())

// Calculate CV
def cv = stdDev / mean
println "Coefficient of Variation (CV): ${cv}"

// Calculate Margin of Error (ME)
def me = zScore * (stdDev / sqrt(slideCellCounts.size()))
println "Margin of Error (ME): ${me}"

// Calculate sample size (n) needed
def requiredSampleSize = (Math.pow(zScore, 2) * Math.pow(cv, 2)) / Math.pow(desiredME / 100, 2)
println "Required Sample Size (n): ${Math.ceil(requiredSampleSize)}"

// Display summary results
println "\nSummary:"
println "Mean Cell Count: ${mean}"
println "Standard Deviation (σ): ${stdDev}"
println "Coefficient of Variation (CV): ${cv}"
println "Margin of Error (ME): ${me}"
println "Sample Size Required for Desired ME: ${Math.ceil(requiredSampleSize)}"

// Optionally, save results to the project
def root = getCurrentHierarchy().getRootObject()
root.getMeasurementList().addMeasurement("Mean Cell Count", mean)
root.getMeasurementList().addMeasurement("Standard Deviation", stdDev)
root.getMeasurementList().addMeasurement("Coefficient of Variation (CV)", cv)
root.getMeasurementList().addMeasurement("Margin of Error (ME)", me)
root.getMeasurementList().addMeasurement("Sample Size Needed", Math.ceil(requiredSampleSize))

fireHierarchyUpdate()
