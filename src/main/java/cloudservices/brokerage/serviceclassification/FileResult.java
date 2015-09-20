/*
 * Copyright 2014 Arash khodadadi.
 * <http://www.arashkhodadadi.com/>
 */
package cloudservices.brokerage.serviceclassification;

/**
 *
 * @author Arash Khodadadi <http://www.arashkhodadadi.com/>
 */
public class FileResult {

    private String fileName;
    private String identifiedObject;
    private String secondBest;
    private String expectedObject;
    private String expectedName;
    
    public FileResult() {
    }

    public FileResult(String fileName, String identifiedObject, String secondBest, String expectedObject) {
        this.fileName = fileName;
        this.identifiedObject = identifiedObject;
        this.secondBest = secondBest;
        this.expectedObject = expectedObject;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getIdentifiedObject() {
        return identifiedObject;
    }

    public void setIdentifiedObject(String identifiedObject) {
        this.identifiedObject = identifiedObject;
    }

    public String getSecondBest() {
        return secondBest;
    }

    public void setSecondBest(String secondBest) {
        this.secondBest = secondBest;
    }

    public String getExpectedObject() {
        return expectedObject;
    }

    public void setExpectedObject(String expectedObject) {
        this.expectedObject = expectedObject;
    }

    public String getExpectedName() {
        return expectedName;
    }

    public void setExpectedName(String expectedName) {
        this.expectedName = expectedName;
    }
    
    public boolean isCorrect() {
        return this.identifiedObject.compareTo(this.expectedObject) == 0;
    }

    @Override
    public String toString() {
        return "FileResult{" + "fileName=" + fileName + ", identifiedObject=" + identifiedObject + ", secondBest=" + secondBest + ", expectedObject=" + expectedObject + '}';
    }

    boolean validate() {
        return this.expectedObject != null && this.fileName != null && this.identifiedObject != null && this.secondBest != null;
    }
}
