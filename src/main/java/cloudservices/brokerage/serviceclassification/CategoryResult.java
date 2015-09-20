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
    private int falseNegatives;
    private int truePositives;
    private int falsePositives;

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

    public int getFalseNegatives() {
        return falseNegatives;
    }

    public void setFalseNegatives(int falseNegatives) {
        this.falseNegatives = falseNegatives;
    }

    public int getTruePositives() {
        return truePositives;
    }

    public void setTruePositives(int truePositives) {
        this.truePositives = truePositives;
    }

    public int getFalsePositives() {
        return falsePositives;
    }

    public void setFalsePositives(int falsePositives) {
        this.falsePositives = falsePositives;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public double precision() {
        return ((double) this.truePositives / (double) (this.falsePositives + this.truePositives)) * 100;
    }

    public double recall() {
        return ((double) this.truePositives / (double) (this.truePositives + this.falseNegatives)) * 100;
    }

    public double fMeasure() {
        return (2.0 * this.precision() * this.recall()) / (double) (this.precision() + this.recall());
    }

    public int sampleCount() {
        return this.truePositives + this.falseNegatives;
    }

    @Override
    public String toString() {
        DecimalFormat formatter = new DecimalFormat("##.00");
        return category + "," + categoryName + "," + truePositives + "," + +falseNegatives + "," + falsePositives
                + "," + formatter.format(precision()) + "," + formatter.format(recall()) + "," + formatter.format(fMeasure());
    }
}
