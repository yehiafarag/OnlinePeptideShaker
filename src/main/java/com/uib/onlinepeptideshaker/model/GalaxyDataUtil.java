package com.uib.onlinepeptideshaker.model;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.beans.HistoryContents;
import com.uib.onlinepeptideshaker.model.beans.OnlinePeptideShakerHistory;
import com.uib.onlinepeptideshaker.model.beans.IndexPoint;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServletRequest;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
import org.codehaus.jackson.map.ObjectMapper;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * This class responsible for handling the data interaction with galaxy where
 * the system can get the required data from the server or create local file
 * system in case it is more efficient
 *
 * @author Yehia Farag
 */
public class GalaxyDataUtil {

    /**
     * Galaxy server instance.
     */
    private final GalaxyInstance GALAXY_INSTANCE;
    /**
     * Request cookies to attach to every request for galaxy used mainly for
     * securing sessions
     */
    private String cookiesRequestProperty;
    /**
     * The galaxy server is support byte serving or not
     */
    private boolean byteServing = false;
    /**
     * PeptideShaker results to view map.
     */
    private final Map<String, String[]> peptideShakerVisualizationMap;
    /**
     * Current loaded proteins file.
     */
    private String proteinsFilepath;
    /**
     * Current loaded peptides file.
     */
    private String peptidesFilepath;
    /**
     * proteinsLine to bytes counter.
     */
    private long proteinsFilePointer;
    /**
     * Peptides indexes map.
     */
    private LinkedHashMap<String, IndexPoint> peptideIndexes;
    /**
     * Protein file utility.
     */
    private final ProteinFile proteinFile;
    /**
     * Peptides file utility..
     */
    private final PeptideFile peptideFile;
    /**
     * Galaxy History bean reference.
     */
    private OnlinePeptideShakerHistory currentGalaxyHistory;

    private Boolean purgeSupport;

    /**
     * Constructor to initialize the class and set the interaction method
     *
     * @param GALAXY_INSTANCE
     */
    public GalaxyDataUtil(GalaxyInstance GALAXY_INSTANCE, Map<String, String[]> peptideShakerVisualizationMap, OnlinePeptideShakerHistory currentGalaxyHistory) {

        this.GALAXY_INSTANCE = GALAXY_INSTANCE;
        this.peptideShakerVisualizationMap = peptideShakerVisualizationMap;
        this.currentGalaxyHistory = currentGalaxyHistory;
        this.proteinFile = new ProteinFile();
        this.peptideFile = new PeptideFile();
        purgeSupport = Boolean.valueOf(GALAXY_INSTANCE.getConfigurationClient().getRawConfiguration().getOrDefault("allow_user_dataset_purge", false).toString());

        Cookie[] cookies = ((VaadinServletRequest) VaadinService.getCurrentRequest()).getCookies();
        for (Cookie cookie : cookies) {
            cookiesRequestProperty += ";" + cookie.getName() + "=" + cookie.getValue();
        }
        cookiesRequestProperty = cookiesRequestProperty.replaceFirst(";", "");
        proteinFile.setCookiesRequestProperty(cookiesRequestProperty);
        peptideFile.setCookiesRequestProperty(cookiesRequestProperty);
    }

    public void loadPeptideShakerDataVisulization(String jobId) {

        //check files first
        proteinFile.setFileId("proteinTable_" + jobId);
        proteinFile.updateFile(GALAXY_INSTANCE.getGalaxyUrl(), peptideShakerVisualizationMap.get(jobId)[0]);
        System.out.println("init prot file now");

//        peptideFile.setFileId("peptideTable_" + jobId);
//        peptideFile.setFileURL(GALAXY_INSTANCE.getGalaxyUrl() + "/datasets" + "/" + peptideShakerVisualizationMap.get(jobId)[1] + "/display?");
//        peptideFile.setFileSize(GALAXY_INSTANCE.getHistoriesClient().showDataset(currentGalaxyHistory.getUsedHistoryId(), peptideShakerVisualizationMap.get(jobId)[1]).getFileSize());
//        peptideFile.updateFile(GALAXY_INSTANCE.getGalaxyUrl(), peptideShakerVisualizationMap.get(jobId)[1]);
        peptideIndexes = getPeptidesIndexMapByteServingServer();

//       List<Map<String,Object>> results =GALAXY_INSTANCE.getSearchClient().search("select * from  hda where id='1acdb822ccd7b108' ").getResults();
//        System.out.println("at results size "+ results.size());
//       for(Map<String,Object> rm:results){
//           for(String key:rm.keySet())
//                System.out.println("at key "+ key+"  "+rm.get(key));
//       }
    }

//    private void initFile(File file, String urlResource) {
//        FileOutputStream fos = null;
//        try {
//            URL proteinsFileUrl = new URL(urlResource);
//            URLConnection conn = proteinsFileUrl.openConnection();
//            conn.addRequestProperty("Cookie", cookiesRequestProperty);
//            conn.setDoInput(true);
//            try (ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream())) {
//                fos = new FileOutputStream(file);
//                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
//                fos.close();
//
//            } catch (MalformedURLException ex) {
//                ex.printStackTrace();
//            } catch (IOException ex) {
//                ex.printStackTrace();
//            } finally {
//                if (fos != null) {
//                    try {
//                        fos.close();
//                    } catch (IOException ex) {
//                        ex.printStackTrace();
//                    }
//                }
//
//            }
//        } catch (MalformedURLException ex) {
//            Logger.getLogger(GalaxyDataUtil.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(GalaxyDataUtil.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
    public Set<Object[]> getProteinsTable() {
        Set<Object[]> proteisnSet = proteinFile.readFullData();
        return proteisnSet;
    }

    public Set<Object[]> getPeptides(String accession, String jobId) {
        Set<IndexPoint> points = new TreeSet<>();
        for (String key : peptideIndexes.keySet()) {
            if (key.contains(accession)) {
                points.add(peptideIndexes.get(key));
            }
        }
        if (points.isEmpty()) {
            return new LinkedHashSet<>();
        }
        Set<String> lines = peptideFile.getDataFromRanges(points);
        Set<Object[]> peptidesSet = new LinkedHashSet<>();
        for (String line : lines) {
            String[] arr = line.split("\\t");
            if (arr.length < 16) {
                System.out.println("error in the peptide file rerader " + line);
                continue;
            }
            Object[] obj = new Object[]{arr[0], arr[1], arr[4], arr[5], arr[6], arr[13], arr[15]};
            peptidesSet.add(obj);

        }
        return peptidesSet;

//        if (byteServing) {
//            return getPeptidesOnFly(points);
//        } else {
//            return getPeptidesFromLocalFile(points);
//        }
    }

    private Set<Object[]> getPeptidesFromLocalFile(Set<Long> points) {

        Set<Object[]> peptideSet = new LinkedHashSet<>();
        BufferedRandomAccessFile bufferedRandomAccessFile;
        try {//           
            bufferedRandomAccessFile = new BufferedRandomAccessFile(peptidesFilepath, "r", 1024 * 100);

            for (long l : points) {
                bufferedRandomAccessFile.seek(l);
                String[] arr = bufferedRandomAccessFile.getNextLine().split("\\t");
                Object[] obj = new Object[]{arr[0], arr[1], arr[4], arr[5], arr[6], arr[13], arr[15]};
                peptideSet.add(obj);
            }
            /**
             * escape header
             */

            bufferedRandomAccessFile.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return peptideSet;
    }

    private Set<Object[]> getPeptidesOnFly(Set<Long> points) {
//        Set<Object[]> peptideSet = new LinkedHashSet<>();
//        String fileID = this.peptideShakerVisualizationMap.get(jobId)[1];
//        String path = currentGalaxyHistory.getHistoryDatasetsMap().get(fileID).getUrl();
//
//        BufferedReader br = null;
//        long pointer = 0;
//        URL website;
//        website = new URL(path);
//
//        try {
//            InputStream inputStream = website.openStream();
//            br = new BufferedReader(new InputStreamReader(inputStream));
//            String line;
//            for (long startIndex : points) {
//                br.skip(startIndex - pointer);
//                line = br.readLine();
//                if (line == null) {
//                    pointer = startIndex;
//                    System.out.println("at error com.uib.onlinepeptideshaker.model.LogicLayer.getPeptides()");
//                    continue;
//                }
//                pointer = startIndex + line.toCharArray().length + 1;
//                String[] arr = line.split("\\t");
//                Object[] obj = new Object[]{arr[0], arr[1], arr[4], arr[5], arr[6], arr[13], arr[15]};
//                peptideSet.add(obj);
//            }
//        } catch (MalformedURLException ex) {
//            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
//        } catch (FileNotFoundException ex) {
//            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
//        } catch (IOException ex) {
//            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
//        } finally {
//            if (br != null) {
//                try {
//                    br.close();
//                } catch (IOException e) {
//                    System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + e.getLocalizedMessage());
//                }
//            }
//        }
        return null;
    }

    /**
     * Returns the index of all peptides in the file.
     *
     * @return index map of peptides
     */
    private LinkedHashMap<String, IndexPoint> getPeptidesIndexMap() {
        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        File fileToRead = new File(peptidesFilepath);
        System.out.println("com.uib.onlinepeptideshaker.model.GalaxyDataUtil.getPeptidesIndexMap() " + fileToRead.exists());
        LinkedHashMap<String, IndexPoint> indexes = new LinkedHashMap<>();
        BufferedRandomAccessFile bufferedRandomAccessFile;
        try {
            bufferedRandomAccessFile = new BufferedRandomAccessFile(fileToRead, "r", 1024 * 100);
            long currentIndex = 0;
            String title;
            int lineCounter = 0;
            String line;
            bufferedRandomAccessFile.getNextLine();
            while ((line = bufferedRandomAccessFile.getNextLine()) != null) {
                title = line.split("\\t")[1].replace(";", "_") + "__" + lineCounter++;
                currentIndex = bufferedRandomAccessFile.getFilePointer();
                IndexPoint point = new IndexPoint();
                point.setStartPoint(currentIndex - (line.toCharArray().length) - 1);
                point.setLength((line.toCharArray().length));
                indexes.put(title, point);

            }
            bufferedRandomAccessFile.close();
        } catch (IOException ex) {
            Logger.getLogger(LogicLayer.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return indexes;
    }

    /**
     * @deprecated Will be removed once we implement it on server Returns the
     * index of all peptides in the file.
     *
     * @return index map of peptides
     */
    private LinkedHashMap<String, IndexPoint> getPeptidesIndexMapByteServingServer() {
        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        File fileToRead = new File(basepath + "/VAADIN/Galaxy7-[Peptide_Shaker_on_data_6__Peptide_Report].tabular");
        LinkedHashMap<String, IndexPoint> indexes = new LinkedHashMap<>();
        BufferedRandomAccessFile bufferedRandomAccessFile;
        try {
            bufferedRandomAccessFile = new BufferedRandomAccessFile(fileToRead, "r", 1024 * 100);
            long currentIndex = 0;
            String title;
            int lineCounter = 0;
            String line;
            bufferedRandomAccessFile.getNextLine();
            while ((line = bufferedRandomAccessFile.getNextLine()) != null) {
                title = line.split("\\t")[1].replace(";", "_") + "__" + lineCounter++;
                currentIndex = bufferedRandomAccessFile.getFilePointer();
                IndexPoint point = new IndexPoint();
                point.setStartPoint(currentIndex - (line.toCharArray().length) - 1);
//                if (line.toCharArray().length != (line.getBytes().length)) {
//                    System.out.println("at line to char array " + (line.toCharArray().length) + "    " + (line.getBytes().length));
//                }
                point.setLength((line.toCharArray().length));
                indexes.put(title, point);

            }
        } catch (IOException ex) {
            Logger.getLogger(LogicLayer.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return indexes;
    }

    public void permanentDeleteHistory(String HistoryId) {
        List<HistoryContents> hcList = GALAXY_INSTANCE.getHistoriesClient().showHistoryContents(HistoryId);
        for (HistoryContents hda : hcList) {
            if (!hda.isDeleted()) {
                deleteDataset(HistoryId, hda.getId());
            }

        }

    }

    private void deleteDataset(String historyId, String dsId) {
        try {

            URL url = new URL(GALAXY_INSTANCE.getGalaxyUrl() + "/api/histories/" + historyId + "/contents/datasets/" + dsId + "?key=" + GALAXY_INSTANCE.getApiKey());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            System.out.println("at cookie to delete " + cookiesRequestProperty);
            conn.addRequestProperty("Cookie", cookiesRequestProperty);
            conn.addRequestProperty("Accept-Encoding", "gzip, deflate, sdch, br");
            conn.addRequestProperty("Accept-Language", "ar,en-US;q=0.8,en;q=0.6,en-GB;q=0.4");
            conn.addRequestProperty("Cache-Control", "no-cache");
            conn.addRequestProperty("Connection", "keep-alive");
            conn.addRequestProperty("DNT", "1");
            conn.addRequestProperty("X-Requested-With", "XMLHttpRequest");
            conn.addRequestProperty("Pragma", "no-cache");
            conn.setDoOutput(true);
            conn.setDoInput(true);

            conn.setRequestMethod("PUT");
            conn.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8")) {
                final ObjectMapper mapper = new ObjectMapper();
                HashMap<String, Object> payLoadParamMap = new LinkedHashMap<>();
                payLoadParamMap.put("deleted", Boolean.TRUE);
                if (purgeSupport) {
                    payLoadParamMap.put("purged", Boolean.TRUE);
                }
                String payload = mapper.writer().writeValueAsString(payLoadParamMap);
                writer.write(payload);
            }
            conn.connect();
            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder jsonString = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                jsonString.append(line);
            }
            br.close();
            conn.disconnect();
            System.out.println("com.uib.onlinepeptideshaker.model.GalaxyDataUtil.permanentDeleteHistory()");

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
            Logger.getLogger(GalaxyDataUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(GalaxyDataUtil.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
