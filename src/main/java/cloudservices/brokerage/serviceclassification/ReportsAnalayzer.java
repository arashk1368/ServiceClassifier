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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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

            ReportsAnalayzer analyzer = new ReportsAnalayzer("reports/", "txt");
            analyzer.analyzeAll2FoldFiles();

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public void analyzeAll2FoldFiles() throws Exception {
        Pair<File, File> pair = getNextFilesPair();
        int fileCounter = 0;
        while (pair != null) {
            File file1 = pair.getKey();
            File file2 = pair.getValue();
            LOGGER.log(Level.INFO, "Analyzing {0} and {1}", new Object[]{file1.getPath(), file2.getPath()});
            Map<String, ReportEntity> report1 = this.createReportEntities(file1);
            Map<String, ReportEntity> report2 = this.createReportEntities(file2);
            Map<String, ReportEntity> resultAverage = new HashMap<>();
            int counter = 0;

            LOGGER.log(Level.INFO, "Starting Average Process...");

            for (Map.Entry<String, ReportEntity> entrySet : report1.entrySet()) {
                String key = entrySet.getKey();
                ReportEntity first = entrySet.getValue();
                LOGGER.log(Level.FINE, "First Report Entity {0}", first);
                ReportEntity second = report2.get(key);
                if (second == null) {
                    LOGGER.log(Level.INFO, "Second Report Entity does not have {0}", key);
                    continue;
                }

                report2.remove(key);
                LOGGER.log(Level.FINE, "Second Report Entity {0}", second);
                ReportEntity average = this.getAverageReport(first, second);
                LOGGER.log(Level.FINE, "Average Report Entity {0}", average);
                resultAverage.put(key, average);

                counter++;
            }

            LOGGER.log(Level.INFO, "{0} Report Entities Averaged", counter);
            for (Map.Entry<String, ReportEntity> entrySet2 : report2.entrySet()) {
                LOGGER.log(Level.INFO, "First Report Entity does not have {0}", entrySet2.getKey());
            }

            this.writeResult(resultAverage, file1);

            pair = getNextFilesPair();
            fileCounter++;
        }

        LOGGER.log(Level.INFO, "{0} Report Pair Files Analayzed", fileCounter);
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
            LoggerSetup.setup("logs/" + filename + ".txt", "logs/" + filename + ".html", Level.INFO);
            return true;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            return false;
        }
    }

    private Pair<File, File> createResultFiles(File file1) throws Exception {
        String address = file1.getPath().replace(file1.getName(), "");
        address += "Two-Fold-Average";
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
                continue;
            }

            // guess,run,config,good,bad,%,recall,mca,ms
            rr = new ReportRow();
            String[] contents = line.split(",");
            rr.setGuess(contents[0]);
            rr.setRun(contents[1]);
            rr.setConfig(contents[2]);
            rr.setGood(Double.parseDouble(contents[3]));
            rr.setBad(Double.parseDouble(contents[4]));
            rr.setPrecision(Double.parseDouble(contents[5]));
            rr.setRecall(Double.parseDouble(contents[6]));
            rr.setMca(Double.parseDouble(contents[7]));
            rr.setMs(Double.parseDouble(contents[8]));

            if (rr.getConfig().contains("(")) {
                // It is class result
                rr.setIsClassResult(true);
                rr.setClassName(rr.getConfig().split(" ")[0]);
                re.getClassResults().put(rr.getKey(), rr);
                LOGGER.log(Level.FINE, "Report Row : {0}", rr);
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

    private Map<String, ReportEntity> sortReportEntities(Map<String, ReportEntity> original) {
        ReportEntityComparator bvc = new ReportEntityComparator(original);
        TreeMap<String, ReportEntity> sortedMap = new TreeMap<>(bvc);
        sortedMap.putAll(original);
        return sortedMap;
    }

    private Map<String, ReportRow> sortClassesReport(Map<String, ReportRow> original) {
        TreeMap<String, ReportRow> sortedMap = new TreeMap<>(original);
        return sortedMap;
    }

    private void writeResult(Map<String, ReportEntity> result, File input) throws Exception {
        LOGGER.log(Level.INFO, "Starting to Write Result Files...");
        Pair<File, File> resultFiles = this.createResultFiles(input);
        String averageFilePath = resultFiles.getKey().getPath();
        String configOnlyFilePath = resultFiles.getValue().getPath();
        LOGGER.log(Level.INFO, "Average file created : {0}", averageFilePath);
        LOGGER.log(Level.INFO, "Config Only Average file created : {0}", configOnlyFilePath);
        FileWriter.appendString("guess,run,config,good,bad,%,recall,mca,ms" + "\n", averageFilePath);
        FileWriter.appendString("guess,run,config,good,bad,%,recall,mca,ms" + "\n", configOnlyFilePath);

        Map<String, ReportEntity> sorted = this.sortReportEntities(result);

        int counter = 0;
        for (Map.Entry<String, ReportEntity> entrySet : sorted.entrySet()) {
            ReportEntity resultRE = entrySet.getValue();

            FileWriter.appendString(resultRE.getConfigResult().toReportString() + "\n", averageFilePath);
            for (Map.Entry<String, ReportRow> classResultEntry : resultRE.getClassResults().entrySet()) {
                FileWriter.appendString(classResultEntry.getValue().toReportString() + "\n", averageFilePath);
            }

            FileWriter.appendString(resultRE.getConfigResult().toReportString() + "\n", configOnlyFilePath);
            counter++;
        }

        LOGGER.log(Level.INFO, "{0} Report Entities Wrote to Files", counter);
    }
}
