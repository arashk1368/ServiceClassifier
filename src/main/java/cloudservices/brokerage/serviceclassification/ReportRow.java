/*
 * Copyright 2014 Arash khodadadi.
 * <http://www.arashkhodadadi.com/>
 */
package cloudservices.brokerage.serviceclassification;

/**
 *
 * @author Arash Khodadadi <http://www.arashkhodadadi.com/>
 */
public class ReportRow implements Comparable<ReportRow> {

    private String guess;
    private String run;
    private String config;
    private double good;
    private double bad;
    private double precision;
    private double recall;
    private double mca;
    private double ms;
    private boolean isClassResult;
    private String className;

    public ReportRow() {
    }

    public String getGuess() {
        return guess;
    }

    public void setGuess(String guess) {
        this.guess = guess;
    }

    public String getRun() {
        return run;
    }

    public void setRun(String run) {
        this.run = run;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public double getGood() {
        return good;
    }

    public void setGood(double good) {
        this.good = good;
    }

    public double getBad() {
        return bad;
    }

    public void setBad(double bad) {
        this.bad = bad;
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getRecall() {
        return recall;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }

    public double getMca() {
        return mca;
    }

    public void setMca(double mca) {
        this.mca = mca;
    }

    public double getMs() {
        return ms;
    }

    public void setMs(double ms) {
        this.ms = ms;
    }

    public boolean isIsClassResult() {
        return isClassResult;
    }

    public void setIsClassResult(boolean isClassResult) {
        this.isClassResult = isClassResult;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getKey() {
        return this.guess.concat(this.config);
    }

    @Override
    public String toString() {
        return "ReportRow{" + "guess=" + guess + "; run=" + run + "; config=" + config + "; good=" + good + "; bad=" + bad + "; precision=" + precision + "; recall=" + recall + "; mca=" + mca + "; ms=" + ms + "; isClassResult=" + isClassResult + "; className=" + className + '}';
    }

    public String toReportString() {
        return guess + "," + run + "," + config + "," + good + "," + bad + "," + precision + "," + recall + "," + mca + "," + ms;
    }

    @Override
    public int compareTo(ReportRow other) {
        return (int) Math.ceil(this.precision - other.precision);
    }
}
