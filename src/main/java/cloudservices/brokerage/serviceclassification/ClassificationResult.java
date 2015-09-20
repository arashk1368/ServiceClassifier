/*
 * Copyright 2014 Arash khodadadi.
 * <http://www.arashkhodadadi.com/>
 */
package cloudservices.brokerage.serviceclassification;

import java.text.DecimalFormat;
import java.util.List;

/**
 *
 * @author Arash Khodadadi <http://www.arashkhodadadi.com/>
 */
public class ClassificationResult {

    private List<CategoryResult> categoryResults;
    private double macroPrecision;
    private double macroRecall;
    private double macroFMeasure;
    private double totalAccuracy;

    public ClassificationResult(List<CategoryResult> categoryResults) {
        this.categoryResults = categoryResults;

        double precisionSum = 0.0;
        double recallSum = 0.0;
        double sampleCount = 0.0;
        double fmeasureSum = 0.0;
        double tpSum = 0.0;

        for (CategoryResult categoryResult : categoryResults) {
            fmeasureSum += categoryResult.fMeasure();
            recallSum += categoryResult.recall();
            precisionSum += categoryResult.precision();
            sampleCount += categoryResult.sampleCount();
            tpSum += categoryResult.getTruePositives();
        }

        this.totalAccuracy = (tpSum / sampleCount) * 100;
        this.macroPrecision = precisionSum / categoryResults.size();
        this.macroRecall = recallSum / categoryResults.size();
        this.macroFMeasure = fmeasureSum / categoryResults.size();
    }

    public List<CategoryResult> getCategoryResults() {
        return categoryResults;
    }

    public void setCategoryResults(List<CategoryResult> categoryResults) {
        this.categoryResults = categoryResults;
    }

    public double getMacroPrecision() {
        return macroPrecision;
    }

    public void setMacroPrecision(double macroPrecision) {
        this.macroPrecision = macroPrecision;
    }

    public double getMacroRecall() {
        return macroRecall;
    }

    public void setMacroRecall(double macroRecall) {
        this.macroRecall = macroRecall;
    }

    public double getMacroFMeasure() {
        return macroFMeasure;
    }

    public void setMacroFMeasure(double macroFMeasure) {
        this.macroFMeasure = macroFMeasure;
    }

    public double getTotalAccuracy() {
        return totalAccuracy;
    }

    public void setTotalAccuracy(double totalAccuracy) {
        this.totalAccuracy = totalAccuracy;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append("Total Accuracy,Macro Precision,Macro Recall"
                + ",Macro F-Measure" + "\n");
        DecimalFormat formatter = new DecimalFormat("##.00");
        sb.append(formatter.format(this.totalAccuracy)).append(",");
        sb.append(formatter.format(this.macroPrecision)).append(",");
        sb.append(formatter.format(this.macroRecall)).append(",");
        sb.append(formatter.format(this.macroFMeasure));

        sb.append("\n");
        sb.append("\n");

        sb.append("Category ID,Category Name,True Positives,False Negatives,False Positives"
                + ",Precision,Recall,F-Measure" + "\n");
        for (CategoryResult categoryResult : this.categoryResults) {
            sb.append(categoryResult.toString()).append("\n");
        }

        return sb.toString();
    }
}
