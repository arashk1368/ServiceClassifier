/*
 * Copyright 2014 Arash khodadadi.
 * <http://www.arashkhodadadi.com/>
 */
package cloudservices.brokerage.serviceclassification;

import cloudservices.brokerage.commons.utils.file_utils.DirectoryUtil;
import cloudservices.brokerage.commons.utils.file_utils.FileReader;
import cloudservices.brokerage.commons.utils.logging.LoggerSetup;
import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.BaseDAO;
import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.DAOException;
import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.v3.CategoryDAO;
import cloudservices.brokerage.crawler.crawlingcommons.model.DAO.v3.ServiceDescriptionSnapshotDAO;
import cloudservices.brokerage.crawler.crawlingcommons.model.entities.v3.Category;
import cloudservices.brokerage.crawler.crawlingcommons.model.entities.v3.ServiceDescriptionSnapshot;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.cfg.Configuration;

/**
 *
 * @author Arash Khodadadi <http://www.arashkhodadadi.com/>
 */
public class FinalClassification {

    private final static Logger LOGGER = Logger.getLogger(ReportsAnalayzer.class.getName());
    private final File report;
    private final HashMap<String, Long> idMap;

    public FinalClassification(File report, HashMap<String, Long> idMap) {
        this.report = report;
        this.idMap = idMap;
    }

    public static void main(String[] argv) {
        int saved = 0;
        try {
            createLogFile();

            LOGGER.log(Level.SEVERE, "Analyzing Final Classification Report...");

            Configuration configuration = new Configuration();
            configuration.configure("v4hibernate.cfg.xml");
            BaseDAO.openSession(configuration);

            HashMap<String, Long> idMap = new HashMap<>();
            idMap.put("1", 1L);
            idMap.put("2", 2L);
            idMap.put("3", 3L);
            idMap.put("4", 4L);
            idMap.put("5", 5L);

            FinalClassification fc = new FinalClassification(new File("final.log"), idMap);
            saved = fc.addToRepo(".wadl");

        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            BaseDAO.closeSession();
            LOGGER.log(Level.SEVERE, "{0} files saved", saved);
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

    private int addToRepo(String extension) throws IOException, DAOException {
        List<FileResult> fileResults = this.getFileResults(this.report);
        HashMap<String, Category> categories = this.findCategories();
        ServiceDescriptionSnapshotDAO sdDAO = new ServiceDescriptionSnapshotDAO();
        int count = 0;
        for (FileResult fileResult : fileResults) {
            String file = fileResult.getFileName();
            String id = file.substring(file.lastIndexOf("/") + 1, file.lastIndexOf(extension));
            ServiceDescriptionSnapshot sd = sdDAO.getById(Long.parseLong(id));
            sd.setIsProcessed(true);
            sd.setPrimaryCategoryPlain(categories.get(fileResult.getIdentifiedObject()));
            sd.setSecondaryCategoryPlain(categories.get(fileResult.getSecondBest()));
            sdDAO.saveOrUpdate(sd);
            count++;
        }

        return count;
    }

    private HashMap<String, Category> findCategories() throws DAOException {
        HashMap<String, Category> categories = new HashMap<>();
        CategoryDAO catDAO = new CategoryDAO();
        for (Map.Entry<String, Long> entrySet : this.idMap.entrySet()) {
            String key = entrySet.getKey();
            Long value = entrySet.getValue();
            Category category = (Category) catDAO.load(Category.class, value);
            categories.put(key, category);
        }

        return categories;
    }
}
