/*
 * Copyright 2014 Arash khodadadi.
 * <http://www.arashkhodadadi.com/>
 */
package cloudservices.brokerage.serviceclassification;

import cloudservices.brokerage.commons.utils.file_utils.DirectoryUtil;
import cloudservices.brokerage.commons.utils.file_utils.FileReader;
import cloudservices.brokerage.commons.utils.file_utils.FileWriter;
import cloudservices.brokerage.commons.utils.logging.LoggerSetup;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.util.Pair;

/**
 *
 * @author Arash Khodadadi <http://www.arashkhodadadi.com/>
 */
public class ReportsAnalayzer {

    private final static Logger LOGGER = Logger.getLogger(ReportsAnalayzer.class.getName());
    private final List<File> reportFiles;
    private final String extension;

    public ReportsAnalayzer(String reportsFolderAddress, String extension) {
        this.extension = extension;
        this.reportFiles = DirectoryUtil.getAllFiles(reportsFolderAddress, extension);

    }

    public static void main(String[] argv) {
        try {
            createLogFile();

            LOGGER.log(Level.SEVERE, "Analyzing Report Files Start...");

            ReportsAnalayzer analyzer = new ReportsAnalayzer("reports/test/", "txt");
            analyzer.analyzeAll2FoldFiles();

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void analyzeAll2FoldFiles() throws Exception {
        Pair<File, File> pair = getNextFilesPair();
        while (pair != null) {
            File file1 = pair.getKey();
            File file2 = pair.getValue();
            LOGGER.log(Level.INFO, "Analyzing {0} and {1}", new Object[]{file1.getPath(), file2.getPath()});
            File average = this.createResultFile(file1);
            LOGGER.log(Level.INFO, "Created Result File in {0}", average.getPath());
            pair = getNextFilesPair();
        }
    }

    private Pair<File, File> getNextFilesPair() {
        for (File reportFile : reportFiles) {

            String path1 = reportFile.getPath();
            LOGGER.log(Level.FINE, "Analyzing {0}", path1);
            if (path1.contains("2Fold")) {
                File file2 = new File(path1.replace("-2Fold", ""));
                if (!reportFiles.contains(file2)) {
                    LOGGER.log(Level.INFO, "There is no pair for: {0}", path1);
                } else {
                    LOGGER.log(Level.FINE, "Found Pair: {0}", file2);
                    reportFiles.remove(reportFile);
                    reportFiles.remove(file2);
                    return new Pair(reportFile, file2);
                }
            }
        }
        return null;
    }

    private static boolean createLogFile() {
        try {
            StringBuilder sb = new StringBuilder();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm");
            Calendar cal = Calendar.getInstance();
            sb.append(dateFormat.format(cal.getTime()));
            String filename = sb.toString();
            DirectoryUtil.createDir("logs");
            LoggerSetup.setup("logs/" + filename + ".txt", "logs/" + filename + ".html", Level.FINE);
            return true;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            return false;
        }
    }

    private Pair<File, File> createResultFiles(File file1) throws Exception {
        String address = file1.getPath().replace(file1.getName(), "");
        address += "results";
        DirectoryUtil.createDir(address);
        address += "/";
        String fileName = file1.getName().replace("." + this.extension, "");
        if (fileName.contains("-2Fold")) {
            fileName = fileName.replace("-2Fold", "");
        }

        String averageName = fileName + "-Average";
        File average = new File(address + averageName + "." + this.extension);
        if (average.exists()) {
            LOGGER.log(Level.FINE, "Average file already exists : {0}", average.getPath());
            average.delete();
        }
        average.createNewFile();

        String configOnlyName = fileName + "-ConfigOnlyAverage";
        File configOnly = new File(address + configOnlyName + "." + this.extension);
        if (configOnly.exists()) {
            LOGGER.log(Level.FINE, "Config only file already exists : {0}", configOnly.getPath());
            configOnly.delete();
        }
        configOnly.createNewFile();

        return new Pair<>(average, configOnly);
    }

    private Map<String, ReportEntity> createReportEntities(File file) throws IOException {
        Map<String, ReportEntity> reportEntities = new HashMap<>();
        List<String> file1Contents = FileReader.ReadAllLines(file);
        LOGGER.log(Level.INFO, "Creating Report Entities for {0}", file.getName());

        ReportEntity re = null;
        ReportRow rr;

        for (String line : file1Contents) {
            if (!line.startsWith("1st") && !line.startsWith("2nd")) {
                LOGGER.log(Level.FINE, "Ignoring {0}", line);
            }

            // guess,run,config,good,bad,%,recall,mca,ms
            rr = new ReportRow();
            String[] contents = line.split(",");
            rr.setGuess(contents[0]);
            rr.setRun(Integer.parseInt(contents[1]));
            rr.setConfig(contents[2]);
            rr.setGood(Integer.parseInt(contents[3]));
            rr.setBad(Integer.parseInt(contents[4]));
            rr.setPrecision(Double.parseDouble(contents[5]));
            rr.setRecall(Double.parseDouble(contents[6]));
            rr.setMca(Double.parseDouble(contents[7]));
            rr.setMs(Double.parseDouble(contents[8]));
            LOGGER.log(Level.FINE, "Report Row : {0}", rr);

            if (rr.getConfig().contains("(")) {
                // It is class result
                rr.setIsClassResult(true);
                rr.setClassName(rr.getConfig().split(" ")[0]);
                re.getClassResults().put(rr.getKey(), rr);
            } else {
                rr.setIsClassResult(false);
                re = new ReportEntity(rr);
                LOGGER.log(Level.FINE, "New Report Entity : {0}", re);
                reportEntities.put(rr.getKey(), re);
            }
        }
        return reportEntities;
    }

    private ReportEntity getAverageReport(ReportEntity firstRE, ReportEntity secondRE) {
        ReportRow firstRR = firstRE.getConfigResult();
        ReportRow secondRR = secondRE.getConfigResult();
        ReportRow average = getAverageRow(firstRR, secondRR);
        average.setIsClassResult(false);
        ReportEntity re = new ReportEntity(average);

        for (Map.Entry<String, ReportRow> entrySet : firstRE.getClassResults().entrySet()) {
            String key = entrySet.getKey();
            ReportRow classAverage = this.getAverageRow(entrySet.getValue(), secondRE.getClassResults().get(key));
            classAverage.setIsClassResult(true);
            classAverage.setClassName(classAverage.getConfig().split(" ")[0]);
            re.getClassResults().put(key, classAverage);
        }

        Map<String, ReportRow> sortedClasses = this.sortClassesReport(re.getClassResults());
        re.setClassResults(sortedClasses);
        return re;
    }

    private ReportRow getAverageRow(ReportRow first, ReportRow second) {
        // guess,run,config,good,bad,%,recall,mca,ms
        ReportRow averageRR = new ReportRow();
        averageRR.setGuess(first.getGuess());
        averageRR.setRun(first.getRun() + "-" + second.getRun());
        averageRR.setConfig(first.getConfig());
        averageRR.setGood((first.getGood() + second.getGood()) / 2.0);
        averageRR.setBad((first.getBad() + second.getBad()) / 2.0);
        averageRR.setPrecision((first.getPrecision() + second.getPrecision()) / 2.0);
        averageRR.setRecall((first.getRecall() + second.getRecall()) / 2.0);
        averageRR.setMca((first.getMca() + second.getMca()) / 2.0);
        averageRR.setMs((first.getMs() + second.getMs()) / 2.0);
        return averageRR;
    }
    }
}
