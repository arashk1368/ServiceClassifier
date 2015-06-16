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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Arash Khodadadi <http://www.arashkhodadadi.com/>
 */
public class LatexConvertor {

    private final static Logger LOGGER = Logger.getLogger(ReportsAnalayzer.class.getName());
    private final List<File> reportFiles;
    private final String extension;

    public LatexConvertor(String reportsFolderAddress, String extension) {
        this.extension = extension;
        this.reportFiles = DirectoryUtil.getAllFiles(reportsFolderAddress, extension);
    }

    public static void main(String[] argv) {
        try {
            createLogFile();

            LatexConvertor convertor = new LatexConvertor("reports/test/", "txt");
            convertor.convertAllFiles(true, 100);
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        }
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

    private void convertAllFiles(boolean topOnly, int numberOfConfigs) throws IOException, Exception {
        for (File reportFile : reportFiles) {
            List<String> lines = FileReader.ReadAllLines(reportFile);
            LOGGER.log(Level.INFO, "Creating Latex Table for {0}", reportFile.getPath());

            File latex = this.createResultFile(reportFile);
            LOGGER.log(Level.INFO, "Result Latex File : {0}", latex.getPath());
            this.writeHeader(latex);
            int counter = numberOfConfigs;
            double maxPrecision = -1;

            for (String line : lines) {
                if (!line.startsWith("1st") && !line.startsWith("2nd")) {
                    LOGGER.log(Level.FINE, "Ignoring {0}", line);
                    continue;
                }

                double[] precision = new double[1];
                boolean[] isConfig = new boolean[1];
                String row = this.getRow(line, isConfig, precision);
                if (isConfig[0]) {
                    if (maxPrecision == -1) {
                        maxPrecision = precision[0];
                    } else if (maxPrecision != precision[0] && topOnly) {
                        break;
                    }
                    counter--;
                    if (counter < 0) {
                        break;
                    }
                }

                LOGGER.log(Level.FINE, "Row {0}", row);
                FileWriter.appendString(row, latex.getPath());
            }

            FileWriter.appendString("\\end{longtabu}", latex.getPath());
        }
    }

    private File createResultFile(File input) throws Exception {
        String address = input.getPath().replace(input.getName(), "");
        address += "Latex-Tables";
        DirectoryUtil.createDir(address);
        address += "/";
        String fileName = input.getName().replace("." + this.extension, "");
        if (fileName.contains("-2Fold")) {
            fileName = fileName.replace("-2Fold", "");
        }

        String ltxName = fileName + "-LtxTable";
        File latex = new File(address + ltxName + ".tex");
        if (latex.exists()) {
            LOGGER.log(Level.FINE, "Latex file already exists : {0}", latex.getPath());
            latex.delete();
        }
        latex.createNewFile();

        return latex;
    }

    private void writeHeader(File latex) throws IOException {
        String header = "\\begin{longtabu} to \\textwidth {|c|c|l|c|c|c|c|c|c|}" + "\n";
        header += "\\caption{Classification Report}\\\\ \\hline" + "\n";
        header += "\\label{tab:CompleteClassificationReport}" + "\n";
        header += "guess&run&config&good&bad&precision&recall&mca&ms \\\\ \\hline" + "\n";
        FileWriter.appendString(header, latex.getPath());
    }

    private String getRow(String line, boolean[] isConfig, double[] precision) {
        String row = "";
        String[] contents = line.split(",");
        contents[2] = contents[2].replace("-noclustering", "");
        contents[2] = contents[2].replace("-median", "");
        contents[2] = contents[2].replace("--nothreshold", "");
        contents[2] = contents[2].replace("--niolocking", "");
        contents[2] = contents[2].replace("-dynaclass", "");

        if (contents[2].contains("(")) {
            contents[2] = contents[2].substring(0, contents[2].indexOf("("));
        } else {
            String table = "\\begin{tabular}[c]{@{}l@{}} ";
            for (String config : contents[2].split(" ")) {
                if (config.contains("-")) {
                    table += config + "\\\\ ";
                }
            }
            table = table.substring(0, table.length() - 3);
            table += " \\end{tabular}";
            contents[2] = table;
            isConfig[0] = true;
            precision[0] = Double.parseDouble(contents[5]);
        }

        for (String content : contents) {
            row += content + "&";
        }

        row = row.substring(0, row.length() - 1);
        row += " \\\\ \\hline" + "\n";
        return row;
    }
}
