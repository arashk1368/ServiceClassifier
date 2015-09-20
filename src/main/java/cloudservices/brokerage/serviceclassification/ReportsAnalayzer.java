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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

    public ReportsAnalayzer(String reportsFolderAddress, String extension, String fileName) {
        this.extension = extension;
        List<File> allFiles = DirectoryUtil.getAllFiles(reportsFolderAddress, extension);
        this.reportFiles = new ArrayList<>();
        for (File file : allFiles) {
            if (file.getName().compareTo(fileName) == 0) {
                this.reportFiles.add(file);
            }
        }
    }

    public static void main(String[] argv) {
        try {
            createLogFile();

            LOGGER.log(Level.SEVERE, "Analyzing Report Files Start...");

//            String reportsFolderAddress = "C:\\Users\\Administrator\\Documents\\Education\\M.Sc\\Thesis\\Implementation\\Classification\\Service-Classification-Bitbucket\\SnapshotRepository\\";
//            reportsFolderAddress += "RESTS10Fold\\results\\";
            String reportsFolderAddress = "test/";
            ReportsAnalayzer analyzer = new ReportsAnalayzer(reportsFolderAddress, "log");
            analyzer.computeResults();

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
                ReportEntity[] res = new ReportEntity[2];
                res[0] = first;
                res[1] = second;
                ReportEntity average = this.getAverageReport(res);
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

    public void analyze10FoldFiles() throws Exception {
        if (reportFiles.size() != 10) {
            throw new Exception("The files are not correct for 10-fold.");
        }

        Map[] reports = new Map[10];

        for (int i = 0; i < 10; i++) {
            File file = reportFiles.get(i);
            LOGGER.log(Level.INFO, "File : {0} ", file.getName());
            Map<String, ReportEntity> report = this.createReportEntities(file);
            reports[i] = report;
        }

        Map<String, ReportEntity> resultAverage = new HashMap<>();
        int counter = 0;

        LOGGER.log(Level.INFO, "Starting Average Process...");
        Map<String, ReportEntity> report = reports[0];

        for (Map.Entry<String, ReportEntity> entrySet : report.entrySet()) {
            String key = entrySet.getKey();
            ReportEntity[] res = new ReportEntity[10];
            res[0] = entrySet.getValue();
            LOGGER.log(Level.FINE, "First Report Entity {0}", res[0]);
            for (int i = 1; i < 10; i++) {
                ReportEntity re = (ReportEntity) reports[i].get(key);
                if (re == null) {
                    throw new Exception("One of the files is different.");
                }
                res[i] = re;
                reports[i].remove(key);
            }

            ReportEntity average = this.getAverageReport(res);
            LOGGER.log(Level.FINE, "Average Report Entity {0}", average);
            resultAverage.put(key, average);

            counter++;
        }

        LOGGER.log(Level.INFO, "{0} Report Entities Averaged", counter);

        this.writeResult(resultAverage, reportFiles.get(0));
    }

    public void compute10FoldFiles() throws Exception {
        if (reportFiles.size() != 10) {
            throw new Exception("The files are not correct for 10-fold.");
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
            LoggerSetup.setup("logs/" + filename + ".txt", "logs/" + filename + ".html", Level.INFO);
            return true;
        } catch (IOException ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
            return false;
        }
    }

    private Pair<File, File> createResultFiles(File file1) throws Exception {
        String address = file1.getPath().replace(file1.getName(), "");
        address += "Average";
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

    private ReportEntity getAverageReport(ReportEntity[] reportEntities) throws Exception {
        if (reportEntities.length < 2) {
            throw new Exception("Cannot average less than 2 files.");
        }

        ReportRow[] rows = new ReportRow[reportEntities.length];
        for (int i = 0; i < reportEntities.length; i++) {
            rows[i] = reportEntities[i].getConfigResult();
        }

        ReportRow average = getAverageRow(rows);
        average.setIsClassResult(false);
        ReportEntity re = new ReportEntity(average);

        for (Map.Entry<String, ReportRow> entrySet : reportEntities[0].getClassResults().entrySet()) {
            String key = entrySet.getKey();

            ReportRow[] classRows = new ReportRow[reportEntities.length];
            for (int i = 0; i < reportEntities.length; i++) {
                classRows[i] = reportEntities[i].getClassResults().get(key);
            }

            ReportRow classAverage = this.getAverageRow(classRows);
            classAverage.setIsClassResult(true);
            classAverage.setClassName(classAverage.getConfig().split(" ")[0]);
            re.getClassResults().put(key, classAverage);
        }

        Map<String, ReportRow> sortedClasses = this.sortClassesReport(re.getClassResults());
        re.setClassResults(sortedClasses);
        return re;
    }

    private ReportRow getAverageRow(ReportRow[] rows) {
        // guess,run,config,good,bad,%,recall,mca,ms
        ReportRow averageRR = new ReportRow();

        ReportRow first = rows[0];
        averageRR.setGuess(first.getGuess());
        averageRR.setRun(first.getRun());
        averageRR.setConfig(first.getConfig());
        double goodAvg = first.getGood();
        double badAvg = first.getBad();
        double precisionAvg = first.getPrecision();
        double recallAvg = first.getRecall();
        double mcaAvg = first.getMca();
        double msAvg = first.getMs();
        double num = rows.length;

        for (int i = 1; i < num; i++) {
            averageRR.setRun(averageRR.getRun() + "-" + rows[i].getRun());
            goodAvg += rows[i].getGood();
            badAvg += rows[i].getBad();
            precisionAvg += rows[i].getPrecision();
            recallAvg += rows[i].getRecall();
            mcaAvg += rows[i].getMca();
            msAvg += rows[i].getMs();
        }

        averageRR.setGood(goodAvg / num);
        averageRR.setBad(badAvg / num);
        averageRR.setPrecision(precisionAvg / num);
        averageRR.setRecall(recallAvg / num);
        averageRR.setMca(mcaAvg / num);
        averageRR.setMs(msAvg / num);
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

    private void computeResults() throws IOException, Exception {
        for (File reportFile : reportFiles) {
            ClassificationResult result = this.computeResult(reportFile);
            File resultFile = this.createResultFile(reportFile);
            String resultFilePath = resultFile.getPath();

            FileWriter.appendString(result.toString(), resultFilePath);
        }
    }

    private ClassificationResult computeResult(File reportFile) throws IOException, Exception {
        LOGGER.log(Level.INFO, "Computing results for file {0}", reportFile.getName());
        List<FileResult> fileResults = this.getFileResults(reportFile);
        List<CategoryResult> categoryResults = computeCategoryResults(fileResults);
        return new ClassificationResult(categoryResults);
    }

    private List<FileResult> getFileResults(File file) throws IOException {
        List<FileResult> fileResults = new ArrayList<>();

        List<String> lines = FileReader.ReadAllLines(file);
        FileResult fr = new FileResult();

        for (String line : lines) {
            if (line.startsWith("                 File: ")) {
                fr = new FileResult();
                fr.setFileName(line.substring(line.indexOf(": ") + 2));
            } else if (line.startsWith("         Subject's ID: ")) {
                fr.setIdentifiedObject(line.substring(line.indexOf(": ") + 2));
            } else if (line.startsWith("Expected subject's ID: ")) {
                fr.setExpectedObject(line.substring(line.indexOf(": ") + 2, line.indexOf("(") - 1));
            } else if (line.startsWith("     Expected subject:")) {
                fr.setExpectedName(line.substring(line.indexOf(": ") + 2));
            } else if (line.startsWith("       Second Best ID: ")) {
                fr.setSecondBest(line.substring(line.indexOf(": ") + 2));
                if (fr.validate()) {
                    LOGGER.log(Level.INFO, "Found {0}", fr);
                    fileResults.add(fr);
                    fr = new FileResult();
                } else {
                    LOGGER.log(Level.SEVERE, "File result {0} is not valid", fr);
                }
            }
        }

        return fileResults;
    }

    private List<CategoryResult> computeCategoryResults(List<FileResult> fileResults) {
        List<CategoryResult> categoryResults = new ArrayList<>();
        for (FileResult fileResult : fileResults) {
            String category = fileResult.getExpectedObject();
            CategoryResult catResult = this.findCategoryResult(categoryResults, category);
            catResult.setCategoryName(fileResult.getExpectedName());

            if (fileResult.isCorrect()) {
                catResult.setTruePositives(catResult.getTruePositives() + 1);
            } else {
                catResult.setFalseNegatives(catResult.getFalseNegatives() + 1);
                CategoryResult falseResult = this.findCategoryResult(categoryResults, fileResult.getIdentifiedObject());
                falseResult.setFalsePositives(falseResult.getFalsePositives() + 1);
            }
        }

        return categoryResults;
    }

    private CategoryResult findCategoryResult(List<CategoryResult> results, String category) {
        for (CategoryResult result : results) {
            if (result.getCategory().compareTo(category) == 0) {
                return result;
            }
        }

        // not found
        CategoryResult catResult = new CategoryResult(category);
        results.add(catResult);
        return catResult;
    }

    private File createResultFile(File file) throws Exception {
        String address = file.getPath().replace(file.getName(), "");
        String fileName = file.getName().replace("." + this.extension, "");

        String resultName = fileName + "-Result";
        File resultFile = new File(address + resultName + ".txt");
        if (resultFile.exists()) {
            LOGGER.log(Level.FINE, "Result file already exists : {0}", resultFile.getPath());
            resultFile.delete();
        }

        resultFile.createNewFile();

        return resultFile;
    }
}
