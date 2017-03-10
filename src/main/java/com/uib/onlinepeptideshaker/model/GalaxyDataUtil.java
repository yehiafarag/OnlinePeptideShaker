package com.uib.onlinepeptideshaker.model;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.uib.onlinepeptideshaker.model.beans.IndexPoint;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.ui.Table;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;
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
    
    private final ProteinFile proteinFile;

    /**
     * Constructor to initialize the class and set the interaction method
     *
     * @param GALAXY_INSTANCE
     */
    public GalaxyDataUtil(GalaxyInstance GALAXY_INSTANCE, Map<String, String[]> peptideShakerVisualizationMap) {

        this.GALAXY_INSTANCE = GALAXY_INSTANCE;
        this.peptideShakerVisualizationMap = peptideShakerVisualizationMap;       
        this.proteinFile = new ProteinFile();
        try {
            Cookie[] cookies = ((VaadinServletRequest) VaadinService.getCurrentRequest()).getCookies();
            for (Cookie cookie : cookies) {
                cookiesRequestProperty += ";" + cookie.getName() + "=" + cookie.getValue();
            }
            cookiesRequestProperty = cookiesRequestProperty.replaceFirst(";", "");
            URL website = new URL(GALAXY_INSTANCE.getGalaxyUrl() + "/datasets");
            URLConnection conn = website.openConnection();
            conn.addRequestProperty("Cookie", cookiesRequestProperty);
            conn.setDoInput(true);
            Map<String, List<String>> headers = conn.getHeaderFields();
            if (headers.get("Server") != null && headers.get("Server").get(0).contains("PasteWSGIServer")) {
                System.err.println("at fucken server ");
                byteServing = false;
            } else {
                System.err.println("at beautiful  server " + headers.get("Server"));
                byteServing = true;
            }
           
              File userFolder = new File(GALAXY_INSTANCE.getApiKey());
            if (!userFolder.exists()) {
                userFolder.mkdir();
            } 
           this.proteinFile.setSupportByteServing(false);
            this.proteinFile.setPathToFolder(userFolder.getAbsolutePath());
            this.proteinFile.setCookiesRequestProperty(cookiesRequestProperty);
        } catch (IOException ex) {
            ex.printStackTrace();
        } 
           


    }

    public void loadPeptideShakerDataVisulization(String jobId) {
        proteinsFilePointer = 0;
        proteinFile.setFileId("proteinTable_" + jobId);
        proteinFile.setFileURL(GALAXY_INSTANCE.getGalaxyUrl() + "/datasets" + "/" + peptideShakerVisualizationMap.get(jobId)[0] + "/display?");
        proteinFile.updateFile();
//        try {
          

//            File proteinsFile = new File(userFolder, "proteinTable_" + jobId);
//            proteinsFilepath = proteinsFile.getAbsolutePath();
//            if (!proteinsFile.exists()) {
//                proteinsFile.createNewFile();
//                initFile(proteinsFile, GALAXY_INSTANCE.getGalaxyUrl() + "/datasets" + "/" + peptideShakerVisualizationMap.get(jobId)[0] + "/display?");
//            }
//            if (byteServing) {
//                peptideIndexes = getPeptidesIndexMapByteServingServer();
//                return;
//            }
//            File peptideFile = new File(userFolder, "peptideTable_" + jobId);
//            peptidesFilepath = peptideFile.getAbsolutePath();
//            if (peptideFile.exists()) {
//                peptideIndexes = getPeptidesIndexMap();
//                return;
//            }
//            peptideFile.createNewFile();
//            initFile(peptideFile, GALAXY_INSTANCE.getGalaxyUrl() + "/datasets" + "/" + peptideShakerVisualizationMap.get(jobId)[1] + "/display?");
//            /**
//             * To be replaced with download file after generate it on galaxy
//             * server.
//             *
//             */
//            peptideIndexes = getPeptidesIndexMap();
//
//        } catch (FileNotFoundException ex) {
//            ex.printStackTrace();
//        } catch (IOException ex) {
//            ex.printStackTrace();
//        } finally {
//        }

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
        Set<Long> points = new TreeSet<>();
        for (String key : peptideIndexes.keySet()) {
            if (key.contains(accession)) {
                points.add(peptideIndexes.get(key).getStartPoint());
            }
        }
        System.out.println("at points size "+points.size()+" "+accession);
        if (points.isEmpty()) {
            return null;
        }
        if (byteServing) {
            return getPeptidesOnFly(points);
        } else {
            return getPeptidesFromLocalFile(points);
        }
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
                point.setLength((line.toCharArray().length));
                indexes.put(title, point);

            }
        } catch (IOException ex) {
            Logger.getLogger(LogicLayer.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return indexes;
    }

}
