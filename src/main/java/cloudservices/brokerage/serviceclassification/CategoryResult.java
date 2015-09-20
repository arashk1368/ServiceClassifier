/*
 * Copyright 2014 Arash khodadadi.
 * <http://www.arashkhodadadi.com/>
 */
package cloudservices.brokerage.serviceclassification;

import java.text.DecimalFormat;

/**
 *
 * @author Arash Khodadadi <http://www.arashkhodadadi.com/>
 */
public class CategoryResult {

    private String category;
    private String categoryName;
    private double falseNegatives;
    private double truePositives;
    private double falsePositives;
    public double precision;
    public double recall;
    public double fMeasure;

    public CategoryResult() {
    }

    public CategoryResult(String category) {
        this.category = category;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getFalseNegatives() {
        return falseNegatives;
    }

    public void setFalseNegatives(double falseNegatives) {
        this.falseNegatives = falseNegatives;
    }

    public double getTruePositives() {
        return truePositives;
    }

    public void setTruePositives(double truePositives) {
        this.truePositives = truePositives;
    }

    public double getFalsePositives() {
        return falsePositives;
    }

    public void setFalsePositives(double falsePositives) {
        this.falsePositives = falsePositives;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double getPrecision() {
        if (precision == 0) {
            return ((double) this.truePositives / (double) (this.falsePositives + this.truePositives)) * 100;
        }

        return this.precision;
    }

    public double getRecall() {
        if (this.recall == 0) {
            return ((double) this.truePositives / (double) (this.truePositives + this.falseNegatives)) * 100;
        }
        return this.recall;
    }

    public double getFmeasure() {
        if (this.fMeasure == 0) {
            if (this.getPrecision() == 0 && this.getRecall() == 0) {
                return 0;
            }
            return (2.0 * this.getPrecision() * this.getRecall()) / (double) (this.getPrecision() + this.getRecall());
        }
        return this.fMeasure;
    }

    public double sampleCount() {
        return this.truePositives + this.falseNegatives;
    }

    @Override
    public String toString() {
        DecimalFormat formatter = new DecimalFormat("##.00");
        DecimalFormat formatter2 = new DecimalFormat("##.##");
        return category + "," + categoryName + "," + formatter2.format(truePositives) + "," + formatter2.format(falseNegatives) + ","
                + formatter2.format(falsePositives) + "," + formatter.format(getPrecision()) + "," + formatter.format(getRecall()) + ","
                + formatter.format(getFmeasure());
    }
}
