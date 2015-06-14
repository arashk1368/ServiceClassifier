/*
 * Copyright 2014 Arash khodadadi.
 * <http://www.arashkhodadadi.com/>
 */
package cloudservices.brokerage.serviceclassification;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Arash Khodadadi <http://www.arashkhodadadi.com/>
 */
public class ReportEntity {

    private Map<String, ReportRow> classResults;
    private final ReportRow configResult;

    public ReportEntity(ReportRow configResult) {
        this.configResult = configResult;
        this.classResults = new HashMap<>();
    }

    public void setClassResults(Map<String, ReportRow> classResults) {
        this.classResults = classResults;
    }

    public Map<String, ReportRow> getClassResults() {
        return classResults;
    }

    public ReportRow getConfigResult() {
        return configResult;
    }

    @Override
    public String toString() {
        return "ReportEntity{" + "classResults=" + classResults + ", configResult=" + configResult + '}';
    }
}
