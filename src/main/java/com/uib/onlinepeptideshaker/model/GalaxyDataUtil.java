package com.uib.onlinepeptideshaker.model;

import com.compomics.util.experiment.io.massspectrometry.MgfIndex;
import com.compomics.util.io.SerializationUtils;
import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.uib.onlinepeptideshaker.model.beans.OnlinePeptideShakerHistory;
import com.uib.onlinepeptideshaker.model.beans.IndexPoint;
import com.uib.onlinepeptideshaker.model.beans.PeptideShakerViewBean;
import com.uib.onlinepeptideshaker.model.core.ReadableFile;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServletRequest;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
     * PeptideShaker system history.
     */
    private final OnlinePeptideShakerHistory systemHistory;
    /**
     * Request cookies to attach to every request for galaxy used mainly for
     * securing sessions.
     */
    private String cookiesRequestProperty;
    /**
     * User can permanently delete datasets.
     */
    private Boolean purgeSupport;
    /**
     * Main user folder absolute path.
     */
    private final String userFolderURL;
    /**
     * Protein file utility.
     */
    private ProteinFile proteinFile;
    /**
     * Peptides file utility..
     */
    private PeptideFile peptideFile;
    /**
     * PSM file utility..
     */
    private ReadableFile psmFile;
    /**
     * MGF file utility..
     */
    private ReadableFile mgfFile;
    /**
     * Peptides indexes map.
     */
    private LinkedHashMap<String, IndexPoint> peptideIndexes;
    /**
     * Peptides indexes map.
     */
    private LinkedHashMap<String, IndexPoint> psmIndexes;
    /**
     * MGF indexes map.
     */
    private MgfIndex mgfFilesIndex;

    /**
     * Constructor to initialize the class and set the interaction method
     *
     * @param GALAXY_INSTANCE Current GalaxyServer instance
     * @param systemHistory the peptideShaker system history
     * @param userFolderURL Main user folder url
     */
    public GalaxyDataUtil(GalaxyInstance GALAXY_INSTANCE, OnlinePeptideShakerHistory systemHistory, String userFolderURL) {
        this.userFolderURL = userFolderURL;
        this.GALAXY_INSTANCE = GALAXY_INSTANCE;
        this.systemHistory = systemHistory;

        purgeSupport = Boolean.valueOf(GALAXY_INSTANCE.getConfigurationClient().getRawConfiguration().getOrDefault("allow_user_dataset_purge", false).toString());
        Cookie[] cookies = ((VaadinServletRequest) VaadinService.getCurrentRequest()).getCookies();
        for (Cookie cookie : cookies) {
            cookiesRequestProperty += ";" + cookie.getName() + "=" + cookie.getValue();
        }
        cookiesRequestProperty = cookiesRequestProperty.replaceFirst(";", "");
    }

    /**
     * Load peptide shaker results
     *
     * @param results PeptideShakerResults bean
     *
     */
    public void loadPeptideShakerDataVisulization(PeptideShakerViewBean results) {
        /**
         * download protein file <indexes>*
         */
        this.proteinFile = new ProteinFile(results.getId() + "_Protein.tem", userFolderURL, results.getProteinFileURL(), cookiesRequestProperty);
        //download peptide index file
        this.peptideIndexes = getPeptidesIndexMapByteServingServer(GALAXY_INSTANCE.getHistoriesClient().showDataset(systemHistory.getCurrent_galaxy_history(), results.getPeptideFileId()).getFullDownloadUrl());
        //init peptide file
        this.peptideFile = new PeptideFile(GALAXY_INSTANCE.getGalaxyUrl(), results.getPeptideFileId(), cookiesRequestProperty);
        //download psm file index
        this.psmIndexes = getPsmIndexMap(GALAXY_INSTANCE.getHistoriesClient().showDataset(systemHistory.getCurrent_galaxy_history(), results.getPSMFileId()).getFullDownloadUrl());
        //init psm file
        this.psmFile = new ReadableFile(GALAXY_INSTANCE.getGalaxyUrl(), results.getPSMFileId(), cookiesRequestProperty) {
            @Override
            public Set<Object[]> getDataFromRange(long start, long end) {
                return null;
            }

        };
        //download MGF index
        this.mgfFilesIndex = getMGFileIndex();
        //init mgf file util
//        System.out.println("at updated MGF "+systemHistory.getReIndexedFile(results.getMgfIds().get(0)));
        this.mgfFile = new ReadableFile(GALAXY_INSTANCE.getGalaxyUrl(), systemHistory.getReIndexedFile(results.getMgfIds().get(0)), cookiesRequestProperty) {
            @Override
            public Set<Object[]> getDataFromRange(long start, long end) {
                return null;
            }
        };
        //download FastaFile Index
        //check files first
//        peptideFile.setFileId("peptideTable_" + jobId);
//        peptideFile.setFileURL(GALAXY_INSTANCE.getGalaxyUrl() + "/datasets" + "/" + peptideShakerVisualizationMap.get(jobId)[1] + "/display?");
//        peptideFile.setFileSize(GALAXY_INSTANCE.getHistoriesClient().showDataset(currentGalaxyHistory.getUsedHistoryId(), peptideShakerVisualizationMap.get(jobId)[1]).getFileSize());
//        peptideFile.updateFile(GALAXY_INSTANCE.getGalaxyUrl(), peptideShakerVisualizationMap.get(jobId)[1]);
//       List<Map<String,Object>> results =GALAXY_INSTANCE.getSearchClient().search("select * from  hda where id='1acdb822ccd7b108' ").getResults();
//        System.out.println("at results size "+ results.size());
//       for(Map<String,Object> rm:results){
//           for(String key:rm.keySet())
//                System.out.println("at key "+ key+"  "+rm.get(key));
//       }
    }

    /**
     * Get the information required for initializing the protein table
     *
     * @return Set<Object[]> for protein table
     */
    public Set<Object[]> getProteinsTable() {
        Set<Object[]> proteisnSet = proteinFile.readFullData();
        return proteisnSet;
    }

    /**
     * @deprecated Will be removed once we implement it on server Returns the
     * index of all peptides in the file.
     *
     * @return index map of peptides
     */
    private LinkedHashMap<String, IndexPoint> getPeptidesIndexMapByteServingServer(String peptideFileUrl) {
        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
         LinkedHashMap<String, IndexPoint> indexes = new LinkedHashMap<>();
         try {
               File fileToRead = new File(userFolderURL, "peptide.txt");
            URL website = new URL(peptideFileUrl);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(fileToRead);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        BufferedRandomAccessFile bufferedRandomAccessFile;
      
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

    /**
     * @deprecated Returns the index of all PSM indexes in the given psm file.
     *
     * @return index map of psms
     */
    private LinkedHashMap<String, IndexPoint> getPsmIndexMap(String psmUrl) {
//        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        LinkedHashMap<String, IndexPoint> indexes = new LinkedHashMap<>();
        try {
            File fileToRead = new File(userFolderURL, "psm.txt");
            URL website = new URL(psmUrl);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(fileToRead);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            BufferedRandomAccessFile bufferedRandomAccessFile;

            bufferedRandomAccessFile = new BufferedRandomAccessFile(fileToRead, "r", 1024 * 100);

            long currentIndex = 0;
            String title;
            int lineCounter = 0;
            String line;
            bufferedRandomAccessFile.getNextLine();
            while ((line = bufferedRandomAccessFile.getNextLine()) != null) {
//            line = line.trim();
                title = line.split("\\t")[2] + "__" + lineCounter++;
                currentIndex = bufferedRandomAccessFile.getFilePointer();
                IndexPoint point = new IndexPoint();
                point.setStartPoint(currentIndex - line.toCharArray().length - 1);
                point.setLength((line.toCharArray().length));
                indexes.put(title, point);

            }

        } catch (IOException ex) {
            Logger.getLogger(LogicLayer.class
                    .getName()).log(Level.SEVERE, null, ex);
        }

        return indexes;
    }

    /**
     * Get peptides information for the selected protein
     *
     * @param accession selected protein accession
     * @return set of object array to initialize peptides table
     */
    public Set<Object[]> getPeptidesInformationForSelectedProtein(String accession) {
        Set<IndexPoint> points = new TreeSet<>();
        for (String key : peptideIndexes.keySet()) {
            if (key.contains(accession)) {
                points.add(peptideIndexes.get(key));
            }
        }
        if (points.isEmpty()) {
            return new LinkedHashSet<>();
        }
        Set<String> lines = peptideFile.getDataAsLines(points);
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
    }

    /**
     * Get PSM information required for initializing and updating PSM table
     *
     * @param peptideSequence protein accession
     * @return Set of Object array required as data source for the table
     */
    public Set<Object[]> getPsmInformationForSelectedPeptide(String peptideSequence) {
        Set<IndexPoint> points = new TreeSet<>();
        for (String key : psmIndexes.keySet()) {
            if (key.contains(peptideSequence)) {
                points.add(psmIndexes.get(key));
            }
        }
        if (points.isEmpty()) {
            return new LinkedHashSet<>();
        }
        Set<String> lines = psmFile.getDataAsLines(points);
        Set<Object[]> psmSet = new LinkedHashSet<>();
        for (String line : lines) {
            String[] arr = line.split("\\t");
            Object[] obj = new Object[]{arr[0], arr[1], arr[2], arr[14], arr[10], arr[5], arr[6], arr[19]};
            psmSet.add(obj);
        }
        return psmSet;
    }

    /**
     * Get MGF information required for initializing and updating MGF table
     *
     * @param spectraTitle selected spectra title
     * @return Set of Object array required as data source for the table
     */
    public Set<Object[]> getMgfInformationForSelectedSpectra(String spectraTitle) {
        long startIndex = mgfFilesIndex.getIndex(spectraTitle);
//        int currentSpectraIndex = mgfFilesIndex.getSpectrumIndex(spectraTitle);
//        long endIndex = mgfFilesIndex.getIndex(mgfFilesIndex.getSpectrumTitle(currentSpectraIndex + 1));
        Set<Object[]> MGFSet = new LinkedHashSet<>();
        int index = 1;
        Set<String> lines = mgfFile.getDataAsLines(startIndex, "END IONS");
        for (String line : lines) {
            String[] arr = line.split(" ");
            MGFSet.add(new Object[]{index++, arr[0], arr[1]});
        }
        return MGFSet;
    }

    /**
     * @deprecated Deserializes the index of an mgf file.
     *
     * @param mgfIndex the mgf index cui file
     * @return the corresponding mgf index object
     * @throws FileNotFoundException exception thrown whenever the file was not
     * found
     * @throws IOException exception thrown whenever an error was encountered
     * while reading the file
     * @throws ClassNotFoundException exception thrown whenever an error
     * occurred while deserializing the object
     */
    private MgfIndex getMGFileIndex() {
        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        File mgfIndex = new File(basepath + "/VAADIN/qExactive01819.mgf.cui");
        try {
            return (MgfIndex) SerializationUtils.readObject(mgfIndex);
        } catch (IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
            Logger.getLogger(LogicLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

//    
//    
//    
//    /**
//     * The galaxy server is support byte serving or not
//     */
//    private boolean byteServing = false;
//    /**
//     * PeptideShaker results to view map.
//     */
////    private final Map<String, String[]> peptideShakerVisualizationMap;
//    /**
//     * Current loaded proteins file.
//     */
//    private String proteinsFilepath;
//    /**
//     * Current loaded peptides file.
//     */
//    private String peptidesFilepath;
//    /**
//     * proteinsLine to bytes counter.
//     */
//    private long proteinsFilePointer;
// 
//   
//    /**
//     * Galaxy History bean reference.
//     */
////    private OnlinePeptideShakerHistory currentGalaxyHistory;
//
//    
//
////    private void initFile(File file, String urlResource) {
////        FileOutputStream fos = null;
////        try {
////            URL proteinsFileUrl = new URL(urlResource);
////            URLConnection conn = proteinsFileUrl.openConnection();
////            conn.addRequestProperty("Cookie", cookiesRequestProperty);
////            conn.setDoInput(true);
////            try (ReadableByteChannel rbc = Channels.newChannel(conn.getInputStream())) {
////                fos = new FileOutputStream(file);
////                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
////                fos.close();
////
////            } catch (MalformedURLException ex) {
////                ex.printStackTrace();
////            } catch (IOException ex) {
////                ex.printStackTrace();
////            } finally {
////                if (fos != null) {
////                    try {
////                        fos.close();
////                    } catch (IOException ex) {
////                        ex.printStackTrace();
////                    }
////                }
////
////            }
////        } catch (MalformedURLException ex) {
////            Logger.getLogger(GalaxyDataUtil.class.getName()).log(Level.SEVERE, null, ex);
////        } catch (IOException ex) {
////            Logger.getLogger(GalaxyDataUtil.class.getName()).log(Level.SEVERE, null, ex);
////        }
////    }
//
//   
//
//    private Set<Object[]> getPeptidesFromLocalFile(Set<Long> points) {
//
//        Set<Object[]> peptideSet = new LinkedHashSet<>();
//        BufferedRandomAccessFile bufferedRandomAccessFile;
//        try {//           
//            bufferedRandomAccessFile = new BufferedRandomAccessFile(peptidesFilepath, "r", 1024 * 100);
//
//            for (long l : points) {
//                bufferedRandomAccessFile.seek(l);
//                String[] arr = bufferedRandomAccessFile.getNextLine().split("\\t");
//                Object[] obj = new Object[]{arr[0], arr[1], arr[4], arr[5], arr[6], arr[13], arr[15]};
//                peptideSet.add(obj);
//            }
//            /**
//             * escape header
//             */
//
//            bufferedRandomAccessFile.close();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return peptideSet;
//    }
//
//    private Set<Object[]> getPeptidesOnFly(Set<Long> points) {
////        Set<Object[]> peptideSet = new LinkedHashSet<>();
////        String fileID = this.peptideShakerVisualizationMap.get(jobId)[1];
////        String path = currentGalaxyHistory.getHistoryDatasetsMap().get(fileID).getUrl();
////
////        BufferedReader br = null;
////        long pointer = 0;
////        URL website;
////        website = new URL(path);
////
////        try {
////            InputStream inputStream = website.openStream();
////            br = new BufferedReader(new InputStreamReader(inputStream));
////            String line;
////            for (long startIndex : points) {
////                br.skip(startIndex - pointer);
////                line = br.readLine();
////                if (line == null) {
////                    pointer = startIndex;
////                    System.out.println("at error com.uib.onlinepeptideshaker.model.LogicLayer.getPeptidesInformationForSelectedProtein()");
////                    continue;
////                }
////                pointer = startIndex + line.toCharArray().length + 1;
////                String[] arr = line.split("\\t");
////                Object[] obj = new Object[]{arr[0], arr[1], arr[4], arr[5], arr[6], arr[13], arr[15]};
////                peptideSet.add(obj);
////            }
////        } catch (MalformedURLException ex) {
////            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
////        } catch (FileNotFoundException ex) {
////            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
////        } catch (IOException ex) {
////            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
////        } finally {
////            if (br != null) {
////                try {
////                    br.close();
////                } catch (IOException e) {
////                    System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + e.getLocalizedMessage());
////                }
////            }
////        }
//        return null;
//    }
//
//    /**
//     * Returns the index of all peptides in the file.
//     *
//     * @return index map of peptides
//     */
//    private LinkedHashMap<String, IndexPoint> getPeptidesIndexMap() {
//        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
//        File fileToRead = new File(peptidesFilepath);
//        System.out.println("com.uib.onlinepeptideshaker.model.GalaxyDataUtil.getPeptidesIndexMap() " + fileToRead.exists());
//        LinkedHashMap<String, IndexPoint> indexes = new LinkedHashMap<>();
//        BufferedRandomAccessFile bufferedRandomAccessFile;
//        try {
//            bufferedRandomAccessFile = new BufferedRandomAccessFile(fileToRead, "r", 1024 * 100);
//            long currentIndex = 0;
//            String title;
//            int lineCounter = 0;
//            String line;
//            bufferedRandomAccessFile.getNextLine();
//            while ((line = bufferedRandomAccessFile.getNextLine()) != null) {
//                title = line.split("\\t")[1].replace(";", "_") + "__" + lineCounter++;
//                currentIndex = bufferedRandomAccessFile.getFilePointer();
//                IndexPoint point = new IndexPoint();
//                point.setStartPoint(currentIndex - (line.toCharArray().length) - 1);
//                point.setLength((line.toCharArray().length));
//                indexes.put(title, point);
//
//            }
//            bufferedRandomAccessFile.close();
//        } catch (IOException ex) {
//            Logger.getLogger(LogicLayer.class
//                    .getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return indexes;
//    }
//
//   
//
//    public void permanentDeleteHistory(String HistoryId) {
//        List<HistoryContents> hcList = GALAXY_INSTANCE.getHistoriesClient().showHistoryContents(HistoryId);
//        for (HistoryContents hda : hcList) {
//            if (!hda.isDeleted()) {
//                deleteDataset(HistoryId, hda.getId());
//            }
//
//        }
//
//    }
//
//    private void deleteDataset(String historyId, String dsId) {
//        try {
//
//            URL url = new URL(GALAXY_INSTANCE.getGalaxyUrl() + "/api/histories/" + historyId + "/contents/datasets/" + dsId + "?key=" + GALAXY_INSTANCE.getApiKey());
//            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//            System.out.println("at cookie to delete " + cookiesRequestProperty);
//            conn.addRequestProperty("Cookie", cookiesRequestProperty);
//            conn.addRequestProperty("Accept-Encoding", "gzip, deflate, sdch, br");
//            conn.addRequestProperty("Accept-Language", "ar,en-US;q=0.8,en;q=0.6,en-GB;q=0.4");
//            conn.addRequestProperty("Cache-Control", "no-cache");
//            conn.addRequestProperty("Connection", "keep-alive");
//            conn.addRequestProperty("DNT", "1");
//            conn.addRequestProperty("X-Requested-With", "XMLHttpRequest");
//            conn.addRequestProperty("Pragma", "no-cache");
//            conn.setDoOutput(true);
//            conn.setDoInput(true);
//
//            conn.setRequestMethod("PUT");
//            conn.setRequestProperty("Accept", "application/json, text/javascript, */*; q=0.01");
//            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
//            try (OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8")) {
//                final ObjectMapper mapper = new ObjectMapper();
//                HashMap<String, Object> payLoadParamMap = new LinkedHashMap<>();
//                payLoadParamMap.put("deleted", Boolean.TRUE);
//                if (purgeSupport) {
//                    payLoadParamMap.put("purged", Boolean.TRUE);
//                }
//                String payload = mapper.writer().writeValueAsString(payLoadParamMap);
//                writer.write(payload);
//            }
//            conn.connect();
//            BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
//            StringBuilder jsonString = new StringBuilder();
//            String line;
//            while ((line = br.readLine()) != null) {
//                jsonString.append(line);
//            }
//            br.close();
//            conn.disconnect();
//            System.out.println("com.uib.onlinepeptideshaker.model.GalaxyDataUtil.permanentDeleteHistory()");
//
//        } catch (MalformedURLException ex) {
//            ex.printStackTrace();
//            Logger.getLogger(GalaxyDataUtil.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            ex.printStackTrace();
//            Logger.getLogger(GalaxyDataUtil.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }
}
