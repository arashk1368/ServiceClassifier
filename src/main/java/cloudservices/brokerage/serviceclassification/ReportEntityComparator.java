/*
 * Copyright 2014 Arash khodadadi.
 * <http://www.arashkhodadadi.com/>
 */
package cloudservices.brokerage.serviceclassification;

import java.util.Comparator;
import java.util.Map;

/**
 *
 * @author Arash Khodadadi <http://www.arashkhodadadi.com/>
 */
public class ReportEntityComparator implements Comparator<String> {

    private final Map<String, ReportEntity> base;

    public ReportEntityComparator(Map<String, ReportEntity> base) {
        this.base = base;
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    @Override
    public int compare(String key1, String key2) {
        ReportRow rr1 = base.get(key1).getConfigResult();
        ReportRow rr2 = base.get(key2).getConfigResult();
        if (rr1.getGuess().equals(rr2.getGuess())) {
            if (rr1.compareTo(rr2) > 0) {
                return -1;
            } else if (rr1.compareTo(rr2) < 0) {
                return 1;// returning 0 would merge keys
            } else { // Equal precision
                if (rr1.getMca() > rr2.getMca()) {
                    return -1;
                } else if (rr1.getMca() < rr2.getMca()) {
                    return 1;
                } else { // MCA and precision equal
                    if (rr1.getMs() < rr2.getMs()) {
                        return -1;
                    } else {
                        return 1;
                    }
                }
            }
        } else if (rr1.getGuess().equals("1st")) {
            return -1;
        } else {
            return 1;
        }
    }
}
