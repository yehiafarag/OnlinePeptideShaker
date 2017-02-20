package com.uib.onlinepeptideshaker.model;

import com.compomics.util.experiment.io.massspectrometry.MgfIndex;
import com.compomics.util.io.SerializationUtils;
import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.HistoriesClient;
import com.github.jmchilton.blend4j.galaxy.ToolsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Dataset;
import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.HistoryContents;
import com.github.jmchilton.blend4j.galaxy.beans.HistoryDataset;
import com.github.jmchilton.blend4j.galaxy.beans.Tool;
import com.github.jmchilton.blend4j.galaxy.beans.ToolExecution;
import com.github.jmchilton.blend4j.galaxy.beans.ToolInputs;
import com.github.jmchilton.blend4j.galaxy.beans.ToolParameter;
import com.github.jmchilton.blend4j.galaxy.beans.ToolSection;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs.WorkflowInput;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;
import com.github.jmchilton.blend4j.galaxy.beans.collection.request.CollectionDescription;
import com.github.jmchilton.blend4j.galaxy.beans.collection.request.HistoryDatasetElement;
import com.github.jmchilton.blend4j.galaxy.beans.collection.response.CollectionResponse;
import com.github.wolfie.refresher.Refresher;
import static com.sun.org.apache.xerces.internal.util.PropertyState.is;
import com.uib.onlinepeptideshaker.model.beans.GalaxyHistory;
import com.uib.onlinepeptideshaker.model.beans.IndexPoint;
import com.uib.onlinepeptideshaker.model.beans.WebTool;
import com.vaadin.server.VaadinService;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.ebi.pride.tools.braf.BufferedRandomAccessFile;

/**
 * This class represents the business logic layer (BLL) where the main
 * computations functions will be located
 *
 * @author Yehia Farag
 */
public abstract class LogicLayer {

    /**
     * FASTA files map.
     */
    private final Map<String, String> fastaFilesMap;
    /**
     * SearchGUIResults files map.
     */
    private final Map<String, String> searchGUIResultsFilesMap;
    /**
     * MGF (spectra) files map.
     */
    private final Map<String, String> mgfFilesMap;
    /**
     * Galaxy server instance.
     */
    private GalaxyInstance GALAXY_INSTANCE;
    /**
     * Galaxy tools client instance.
     */
    private ToolsClient TOOLS_CLIENT;

    /**
     * Galaxy PeptideShaker tool.
     */
    private WebTool Peptide_Shaker_Tool;
    /**
     * Galaxy SearchGUI tool.
     */
    private WebTool Search_GUI_Tool;
    /**
     * Galaxy NelsUtil_tool tool.
     */
    private WebTool nelsUtil_tool;
    private Tool selectTool;
    /**
     * Galaxy History bean.
     */
    private GalaxyHistory currentGalaxyHistory;
    /**
     * PeptideShaker results to view map.
     */
    private final Map<String, String[]> peptideShakerVisualizationMap;

    private CollectionResponse collectionResponse;

    private History jobsHistory;

    private final Refresher REFRESHER;

    /**
     * Peptides indexes map.
     */
    private final HashMap<String, IndexPoint> peptideIndexes;
    /**
     * PSM indexes map.
     */
    private final HashMap<String, IndexPoint> psmIndexes;

    private final MgfIndex mgfFilesIndex;

    /**
     * Initialize the main logic layer
     *
     */
    public LogicLayer(Refresher REFRESHER) {
        fastaFilesMap = new LinkedHashMap<>();
        mgfFilesMap = new LinkedHashMap<>();
        searchGUIResultsFilesMap = new LinkedHashMap<>();
        peptideShakerVisualizationMap = new LinkedHashMap<>();
        this.REFRESHER = REFRESHER;
        peptideIndexes = getPeptidesIndexMap();
        this.psmIndexes = getPsmIndexMap();
        mgfFilesIndex = getMGFileIndex();
    }

    /**
     * Get PeptideShaker web tool that has all the required tool information
     *
     * @return WebTool Peptide_Shaker_Tool
     */
    public WebTool getPeptide_Shaker_Tool() {
        return Peptide_Shaker_Tool;
    }

    /**
     * Get SearchGUI web tool that has all the required tool information
     *
     * @return WebTool Search_GUI_Tool
     */
    public WebTool getSearch_GUI_Tool() {
        return Search_GUI_Tool;
    }

    /**
     * Get NelsUtility web tool that has all the required tool information
     *
     * @return WebTool NelsUtil_tool
     */
    public WebTool getNelsUtil_tool() {
        return nelsUtil_tool;
    }

    /**
     * Initialize the main logic layer components
     *
     * @param GALAXY_INSTANCE
     */
    public void initializeTheLogicLayer(GalaxyInstance GALAXY_INSTANCE) {
        this.GALAXY_INSTANCE = GALAXY_INSTANCE;
        this.TOOLS_CLIENT = GALAXY_INSTANCE.getToolsClient();
        this.initializeToolsModel();
        this.currentGalaxyHistory = new GalaxyHistory();
        this.initGalaxyHistory(null);

    }

    /**
     * Initialize the logic of the tools
     *
     * @param galaxyInstance Galaxy server instance
     */
    private void initializeToolsModel() {
        for (ToolSection toolSection : TOOLS_CLIENT.getTools()) {
            if (toolSection == null || toolSection.getElems() == null) {
                continue;
            }
            for (Tool galaxyTool : toolSection.getElems()) {
                if (galaxyTool.getName() == null) {
                    continue;
                }
                if (galaxyTool.getLink().contains("/tool_runner?tool_id=Grep1")) {
                    selectTool = galaxyTool;
                } else if (galaxyTool.getLink().contains("toolshed.g2.bx.psu.edu%2Frepos%2Fgalaxyp%2Fpeptideshaker%2Fsearch_gui%2F2.9.0")) {
                    if (Search_GUI_Tool == null) {
                        Search_GUI_Tool = new WebTool();
                    }
                    initTool(toolSection.getId(), galaxyTool, Search_GUI_Tool);
                } else if (galaxyTool.getLink().contains("toolshed.g2.bx.psu.edu%2Frepos%2Fgalaxyp%2Fpeptideshaker%2Fpeptide_shaker%2F1.11.0")) {
                    if (Peptide_Shaker_Tool == null) {
                        Peptide_Shaker_Tool = new WebTool();
                    }
                    initTool(toolSection.getId(), galaxyTool, Peptide_Shaker_Tool);
                }

            }

        }

    }

    /**
     * Initialize the tool attributes
     *
     * @param toolSectionId Galaxy tool section id
     * @param galaxyTool Galaxy tool instance
     * @param webTool web tool instance
     */
    private void initTool(String toolSectionId, Tool galaxyTool, WebTool webTool) {
        webTool.setId(galaxyTool.getId());
        webTool.setToolName(galaxyTool.getName());
        webTool.setToolSection(toolSectionId);
        webTool.addTool(toolSectionId, galaxyTool.getId());

    }

    /**
     * Initialize the Galaxy user history
     *
     * @param historyId Galaxy history id
     */
    private void initGalaxyHistory(String historyId) {

        fastaFilesMap.clear();
        mgfFilesMap.clear();
        peptideShakerVisualizationMap.clear();
        if (historyId == null) {
            if (GALAXY_INSTANCE.getHistoriesClient().getHistories().isEmpty()) {
                GALAXY_INSTANCE.getHistoriesClient().create(new History("Online-PeptideShaker-History"));
            }
            currentGalaxyHistory.setUsedHistoryId(GALAXY_INSTANCE.getHistoriesClient().getHistories().get(0).getId());
        } else {
            currentGalaxyHistory.setUsedHistoryId(historyId);
        }
        Map<String, String> historySecMap = new LinkedHashMap<>();
        for (History history : GALAXY_INSTANCE.getHistoriesClient().getHistories()) {
            if (!history.getName().equals("Web-Peptideshaker") && !history.getName().equals("Online-PeptideShaker-History")) {
                GALAXY_INSTANCE.getHistoriesClient().deleteHistory(history.getId());
                GALAXY_INSTANCE.getHistoriesClient().deleteHistoryRequest(history.getId());
            }
            historySecMap.put(history.getId(), history.getName());
            if (history.getId().equalsIgnoreCase(currentGalaxyHistory.getUsedHistoryId())) {
                currentGalaxyHistory.setUsedHistory(history);
            }
        }
        int index = 1;
        currentGalaxyHistory.setAvailableGalaxyHistoriesMap(historySecMap);
        Map<String, HistoryContents> galaxyDatasetMap = new LinkedHashMap<>();
        for (HistoryContents content : GALAXY_INSTANCE.getHistoriesClient().showHistoryContents(currentGalaxyHistory.getUsedHistoryId())) {
            if (content.isDeleted()) {
                continue;
            }
            Dataset ds = GALAXY_INSTANCE.getHistoriesClient().showDataset(currentGalaxyHistory.getUsedHistoryId(), content.getId());
            if (!ds.getState().equalsIgnoreCase("ok")) {
                trackHistory(content.getId());
            }
            if (ds.getDataTypeExt().equalsIgnoreCase("tabular")) {
                String jobId = GALAXY_INSTANCE.getHistoriesClient().showProvenance(currentGalaxyHistory.getUsedHistoryId(), content.getId()).getJobId();

                String[] arr;
                if (this.peptideShakerVisualizationMap.containsKey(jobId)) {
                    arr = this.peptideShakerVisualizationMap.get(jobId);
                } else {
                    arr = new String[4];
                }
                if (ds.getName().endsWith("Peptide Report")) {
                    arr[1] = ds.getId();
                    this.peptideShakerVisualizationMap.put(jobId, arr);
                } else if (ds.getName().endsWith("Protein Report")) {
                    arr[0] = ds.getId();
                    this.peptideShakerVisualizationMap.put(jobId, arr);
                } else if (ds.getName().endsWith("PSM Report")) {
                    arr[2] = ds.getId();
                    this.peptideShakerVisualizationMap.put(jobId, arr);
                } else {
//                    this.peptideShakerVisualizationMap.remove(jobId);
                }
            }

            content.setHistoryContentType(ds.getDataTypeExt());
            if (content.getHistoryContentType().equalsIgnoreCase("fasta")) {
                fastaFilesMap.put(ds.getId(), (index) + " - " + ds.getName());
            } else if (content.getHistoryContentType().equalsIgnoreCase("mgf")) {
                mgfFilesMap.put(ds.getId(), (index) + " - " + ds.getName());
            } else if (content.getHistoryContentType().equalsIgnoreCase("searchgui_archive")) {
                searchGUIResultsFilesMap.put(ds.getId(), (index) + " - " + ds.getName());
            }
            content.setUrl(ds.getFullDownloadUrl());
            content.setName((index++) + " - " + ds.getName());
            galaxyDatasetMap.put(content.getId(), content);
        }
        currentGalaxyHistory.setHistoryDatasetsMap(galaxyDatasetMap);
        currentGalaxyHistory.setPeptideShakerVisualizationMap(peptideShakerVisualizationMap);
        updateHistoryPresenter(currentGalaxyHistory);

    }

    /**
     * Get current selected history
     *
     * @return GalaxyHistory current selected history bean
     */
    public GalaxyHistory getCurrentGalaxyHistory() {
        return currentGalaxyHistory;
    }

    /**
     * Create new Galaxy user history
     *
     * @param historyName new Galaxy history name
     */
    public void createNewHistory(String historyName) {
        History newHistory = GALAXY_INSTANCE.getHistoriesClient().create(new History(historyName));
        initGalaxyHistory(newHistory.getId());

    }

    /**
     * Delete history dataset from galaxy server
     *
     * @param historyId Galaxy history id
     * @param datasetId Galaxy history dataset id
     */
    public void deleteGalaxyHistoryDataseyt(String historyId, String datasetId) {
        final HistoryDataset hd = new HistoryDataset();
        hd.setSource(HistoryDataset.Source.HDA);
        hd.setContent(datasetId);

    }

    /**
     * Update the selected history in galaxy.
     *
     * @param historyId selected history id could be from exist history or new.
     */
    public void updateSelectedHistory(String historyId) {
        initGalaxyHistory(historyId);

    }

    /**
     * Get FASTA files map.
     *
     * @return Map dataset id and and dataset name
     */
    public Map<String, String> getFastaFilesMap() {
        return fastaFilesMap;
    }

    /**
     * Get SearchGUIResults files map.
     *
     * @return Map dataset id and and dataset name
     */
    public Map<String, String> getSearchGUIResultsFilesMap() {
        return searchGUIResultsFilesMap;
    }

    /**
     * Get MGF (spectra) files map.
     *
     * @return Map dataset id and and dataset name
     */
    public Map<String, String> getMgfFilesMap() {
        return mgfFilesMap;
    }

    public void executeWorkFlow(String fastaFileId, List<String> mgfIdsList, List<String> searchEnginesList) {
        Workflow selectedWf;
        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        File file;
        WorkflowInput input2;
        if (mgfIdsList.size() > 1) {
            file = new File(basepath + "/VAADIN/Galaxy-Workflow-onlinepeptideshaker_collection.ga");
            input2 = prepareWorkflowCollectionList(WorkflowInputs.InputSourceType.HDCA, mgfIdsList);
        } else {
            file = new File(basepath + "/VAADIN/Galaxy-Workflow-onlinepeptideshaker.ga");
            input2 = new WorkflowInputs.WorkflowInput(mgfIdsList.get(0), WorkflowInputs.InputSourceType.HDA);
        }
        String json = readWorkflowFile(file);
        selectedWf = GALAXY_INSTANCE.getWorkflowsClient().importWorkflow(json);

        WorkflowInputs workflowInputs = new WorkflowInputs();
        workflowInputs.setWorkflowId(selectedWf.getId());
        workflowInputs.setDestination(new WorkflowInputs.ExistingHistory(currentGalaxyHistory.getUsedHistoryId()));

        WorkflowInput input = new WorkflowInputs.WorkflowInput(fastaFileId, WorkflowInputs.InputSourceType.HDA);

        workflowInputs.setInput("0", input);
        workflowInputs.setInput("1", input2);
        workflowInputs.setToolParameter(Peptide_Shaker_Tool.getToolId(), new ToolParameter("outputs", "cps"));

        final WorkflowOutputs output = GALAXY_INSTANCE.getWorkflowsClient().runWorkflow(workflowInputs);
//        while (GALAXY_INSTANCE.getJobsClient().showJob(GALAXY_INSTANCE.getHistoriesClient().showProvenance(currentGalaxyHistory.getUsedHistoryId(), output.getOutputIds().get(1)).getJobId()).getState().equalsIgnoreCase("running")) {
//        }
        updateSelectedHistory(currentGalaxyHistory.getUsedHistoryId());
        updateHistoryPresenter(currentGalaxyHistory);

        GALAXY_INSTANCE.getWorkflowsClient().deleteWorkflowRequest(selectedWf.getId());

        updateSelectedHistory(currentGalaxyHistory.getUsedHistoryId());
        updateHistoryPresenter(currentGalaxyHistory);
        if (collectionResponse != null) {
            collectionResponse.setPurged(true);

            collectionResponse = null;
        }
    }

    public void executeSearchGUITool(String fastaFileId, List<String> mgfIdsList, List<String> searchEnginesList) {

        Map<String, Object> parameters = new HashMap<>();
        boolean selectAll = searchEnginesList.isEmpty();
        parameters.put("X!Tandem", String.valueOf(searchEnginesList.contains("X!Tandem") || selectAll));
        parameters.put("MSGF", String.valueOf(searchEnginesList.contains("MS-GF+") || selectAll));
        parameters.put("OMSSA", String.valueOf(searchEnginesList.contains("OMSSA") || selectAll));
        parameters.put("Comet", String.valueOf(searchEnginesList.contains("Comet") || selectAll));

        final HashMap inputDict = new HashMap();

        final HashMap values = initToolDatasetDict(fastaFileId);
        inputDict.put("bach", Boolean.FALSE);
        inputDict.put("values", values);
        parameters.put("input_database", inputDict);

        final HashMap inputDict2 = new HashMap();
        HashSet mgfDictValues = new HashSet();
        for (String mgfId : mgfIdsList) {
            mgfDictValues.add(this.initToolDatasetDict(mgfId));
        }
        inputDict2.put("bach", Boolean.FALSE);
        inputDict2.put("values", mgfDictValues);
        parameters.put("peak_lists", inputDict2);

        final ToolInputs toolInput = new ToolInputs(Search_GUI_Tool.getId(), parameters);
        toolInput.setHistoryId(currentGalaxyHistory.getUsedHistoryId());
        ToolExecution exc = GALAXY_INSTANCE.getToolsClient().create(currentGalaxyHistory.getUsedHistory(), toolInput);
//        trackHistory(exc.getOutputs().get(0).getId());
        updateSelectedHistory(currentGalaxyHistory.getUsedHistoryId());
//        

//        updateHistoryPresenter(currentGalaxyHistory);
    }

    public void executePeptideShakerTool(String searchGUIResultsFileID) {

        Map<String, Object> parameters = new HashMap<>();
        final HashMap inputDict = new HashMap();

        final HashMap values = initToolDatasetDict(searchGUIResultsFileID);
        inputDict.put("bach", Boolean.FALSE);
        inputDict.put("values", values);
        parameters.put("searchgui_input", inputDict);
        parameters.put("outputs", "cps");
        final ToolInputs toolInput = new ToolInputs(Peptide_Shaker_Tool.getId(), parameters);
        toolInput.setHistoryId(currentGalaxyHistory.getUsedHistoryId());

        ToolExecution exc = GALAXY_INSTANCE.getToolsClient().create(currentGalaxyHistory.getUsedHistory(), toolInput);
        updateSelectedHistory(currentGalaxyHistory.getUsedHistoryId());
    }

    private String readWorkflowFile(File file) {
        String json = "";
        String line;

        try {
            FileReader fileReader = new FileReader(file);
            try ( // Always wrap FileReader in BufferedReader.
                    BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                while ((line = bufferedReader.readLine()) != null) {
                    json += (line);
                }
                // Always close files.
            }
        } catch (FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '" + "'");
        } catch (IOException ex) {
            System.out.println("Error reading file '" + "'");
        }
        return json;
    }

    private HashMap initToolDatasetDict(String datasetId) {
        final HashMap values = new HashMap();
        values.put("src", "hda");
        values.put("id", datasetId);
        return values;

    }

    public abstract void updateHistoryPresenter(GalaxyHistory currentGalaxyHistory);

    /**
     * Prepares a work flow which takes as input a collection list.
     *
     * @param inputSource The type of input source for this work flow.
     * @return A WorkflowInputs describing the work flow.
     * @throws InterruptedException
     */
    private WorkflowInput prepareWorkflowCollectionList(WorkflowInputs.InputSourceType inputSource, List<String> dsIds) {
        String historyId = currentGalaxyHistory.getUsedHistoryId();
        collectionResponse = constructFileCollectionList(historyId, dsIds);
        return new WorkflowInputs.WorkflowInput(collectionResponse.getId(),
                inputSource);

    }

    /**
     * Constructs a list collection from the given files within the given
     * history.
     *
     * @param historyId The id of the history to build the collection within.
     * @param inputIds The IDs of the files to add to the collection.
     * @return A CollectionResponse object for the constructed collection.
     */
    private CollectionResponse constructFileCollectionList(String historyId, List<String> inputIds) {
        HistoriesClient historiesClient = GALAXY_INSTANCE.getHistoriesClient();
        CollectionDescription collectionDescription = new CollectionDescription();
        collectionDescription.setCollectionType("list");
        collectionDescription.setName("collection");
        for (String inputId : inputIds) {
            HistoryDatasetElement element = new HistoryDatasetElement();
            element.setId(inputId);
            element.setName(inputId);

            collectionDescription.addDatasetElement(element);
        }

        return historiesClient.createDatasetCollection(historyId, collectionDescription);
    }

    private void trackHistory(String historyDsId) {
        GalaxyServerRefresher gref = new GalaxyServerRefresher() {
            @Override
            public boolean process() {
                System.out.println("at .process()  " + GALAXY_INSTANCE.getHistoriesClient().showDataset(currentGalaxyHistory.getUsedHistoryId(), historyDsId).getState());
                return (GALAXY_INSTANCE.getHistoriesClient().showDataset(currentGalaxyHistory.getUsedHistoryId(), historyDsId).getState().equalsIgnoreCase("ok"));
            }

            @Override
            public void end() {
                REFRESHER.removeListener(this);
                updateHistoryPresenter(currentGalaxyHistory);
                System.err.println("end !");
            }
        };
        REFRESHER.addListener(gref);
        REFRESHER.setRefreshInterval(10000);
    }

    public Set<Object[]> loadPeptideShakerResults(String dataId) {
        String fileID = this.peptideShakerVisualizationMap.get(dataId)[0];
        String path = currentGalaxyHistory.getHistoryDatasetsMap().get(fileID).getUrl();
        URL website;
        Set<Object[]> proteisnSet = new LinkedHashSet<>();
        BufferedReader br = null;
        try {//           
            website = new URL(path);
            InputStream inputStream = website.openStream();
            String line;
            br = new BufferedReader(new InputStreamReader(inputStream));
            /**
             * escape header
             *
             */
            br.readLine();
            long byteCounter = 0;
            int lineNumber = 0;
            while ((line = br.readLine()) != null) {
                byteCounter += line.getBytes().length;
                String[] arr = line.split("\\t");
                if (arr.length > 25) {
                    Object[] obj = new Object[]{arr[0], arr[1], arr[2], arr[3], arr[5], arr[6], arr[17]};
                    proteisnSet.add(obj);
                    lineNumber++;
                }
                if (lineNumber == 500) {
                    lineNumber = 0;
                    inputStream.close();
                    inputStream = website.openStream();
                    inputStream.skip(byteCounter);
                    br = new BufferedReader(new InputStreamReader(inputStream));
                }
            }

        } catch (MalformedURLException ex) {
            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
        } catch (FileNotFoundException ex) {
            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
        } catch (IOException ex) {
            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + e.getLocalizedMessage());
                }
            }
        }
        return proteisnSet;// readProteinsFile(fileToRead);

    }

    public Set<Object[]> getPeptides(String accession, String jobId) {
        Set<Object[]> proteisnSet = new LinkedHashSet<>();
        try {
            Set<IndexPoint> points = new LinkedHashSet<>();
            for (String key : peptideIndexes.keySet()) {
                if (key.contains(accession)) {
                    points.add(peptideIndexes.get(key));
                }

            }
            if (points.isEmpty()) {
                return null;
            }
            String fileID = this.peptideShakerVisualizationMap.get(jobId)[1];
            String path = currentGalaxyHistory.getHistoryDatasetsMap().get(fileID).getUrl();

            BufferedReader br = null;
            URL website;
            website = new URL(path);

            for (IndexPoint point : points) {

                try {//
                    InputStream inputStream = website.openStream();
                    inputStream.skip(point.getStartPoint());
                    String line;
                    br = new BufferedReader(new InputStreamReader(inputStream));
                    while ((line = br.readLine()) != null) {
                        String[] arr = line.split("\\t");
                        if (arr.length < 16) {
                            continue;
                        }
                        Object[] obj = new Object[]{arr[0], arr[1], arr[4], arr[5], arr[6], arr[13], arr[15]};
                        proteisnSet.add(obj);
                        break;
                    }

                } catch (MalformedURLException ex) {
                    System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
                } catch (FileNotFoundException ex) {
                    System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
                } catch (IOException ex) {
                    System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + e.getLocalizedMessage());
                        }
                    }
                }

            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(LogicLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return proteisnSet;
    }

    public Set<Object[]> getPsm(String accession, String jobId) {
        Set<Object[]> proteisnSet = new LinkedHashSet<>();
        try {
            Set<IndexPoint> points = new LinkedHashSet<>();
            for (String key : psmIndexes.keySet()) {

                if (key.split("__")[0].equals(accession)) {
                    points.add(psmIndexes.get(key));
                    System.err.println("at key is " + key + "  " + accession);
                }

            }
            if (points.isEmpty()) {
                return proteisnSet;
            }
            String fileID = this.peptideShakerVisualizationMap.get(jobId)[2];
            String path = currentGalaxyHistory.getHistoryDatasetsMap().get(fileID).getUrl();

            BufferedReader br = null;
            URL website;
            website = new URL(path);

            for (IndexPoint point : points) {

                try {//
                    InputStream inputStream = website.openStream();
                    inputStream.skip(point.getStartPoint());
                    String line;
                    br = new BufferedReader(new InputStreamReader(inputStream));
                    while ((line = br.readLine()) != null) {
                        String[] arr = line.split("\\t");
                        System.out.println("at arr length " + arr.length + "   " + line);
                        if (arr.length < 20) {
                            continue;
                        }
                        Object[] obj = new Object[]{arr[0], arr[1], arr[2], arr[14], arr[10], arr[5], arr[6], arr[19]};
                        proteisnSet.add(obj);
                        break;
                    }

                } catch (MalformedURLException ex) {
                    System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
                } catch (FileNotFoundException ex) {
                    System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
                } catch (IOException ex) {
                    System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
                } finally {
                    if (br != null) {
                        try {
                            br.close();
                        } catch (IOException e) {
                            System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + e.getLocalizedMessage());
                        }
                    }
                }

            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(LogicLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return proteisnSet;
    }

    public Set<Object[]> getMGF(String accession, String jobId) {
        Set<Object[]> proteisnSet = new LinkedHashSet<>();
        try {
            String fileID = "bd2f206dec1cc58f";//this.peptideShakerVisualizationMap.get(jobId)[2];
            String path = currentGalaxyHistory.getHistoryDatasetsMap().get(fileID).getUrl();

            BufferedReader br = null;
            URL website;
            website = new URL(path);

            try {//
                InputStream inputStream = website.openStream();
                inputStream.skip(mgfFilesIndex.getIndex(accession));
                String line;
                int index = 0;
                br = new BufferedReader(new InputStreamReader(inputStream));
                boolean notready = true;
                while ((line = br.readLine()) != null) {
                    if (notready) {
                        if (line.startsWith("CHARGE")) {
                            notready = false;
                        }
                        continue;
                    }
                    if (line.contains("END IONS")) {
                        break;
                    }
                    String[] arr = line.replace(" ", "_").split("_");
                    Object[] obj = new Object[]{"" + (index++), arr[0], arr[1]};
                    proteisnSet.add(obj);

                }

            } catch (MalformedURLException ex) {
                System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
            } catch (FileNotFoundException ex) {
                System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
            } catch (IOException ex) {
                System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + ex.getLocalizedMessage());
            } finally {
                if (br != null) {
                    try {
                        br.close();
                    } catch (IOException e) {
                        System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.loadPeptideShakerResults()" + e.getLocalizedMessage());
                    }
                }
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(LogicLayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return proteisnSet;
    }

    private Set<Object[]> readPsmIndexFile(File proteinsFile) {
        Set<Object[]> proteisnSet = new LinkedHashSet<>();
        BufferedReader bufRdr;
        String line;
        int row = 0;
        try {
            FileReader fr = new FileReader(proteinsFile);
            bufRdr = new BufferedReader(fr);
//            bufRdr.readLine();
            while ((line = bufRdr.readLine()) != null && row < 1000) {
                System.err.println("at peptides found " + line);
                String[] arr = line.split("\\t");
                if (arr.length < 8) {
                    continue;
                }
                Object[] obj = new Object[]{arr[0], arr[1], arr[2], arr[5], arr[6], arr[7]};
                proteisnSet.add(obj);
            }

        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
            System.out.println(e.getLocalizedMessage());
        }//e.printStackTrace();}
        return proteisnSet;
    }

    /**
     * Returns the index of all spectra in the given MGF file.
     *
     * @return index map of peptides
     */
    private HashMap<String, IndexPoint> getPeptidesIndexMap() {
        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        File fileToRead = new File(basepath + "/VAADIN/Galaxy7-[Peptide_Shaker_on_data_6__Peptide_Report].tabular");
        HashMap<String, IndexPoint> indexes = new HashMap<>();
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
                point.setStartPoint(currentIndex - (line.getBytes().length));
                point.setLength((line.getBytes().length));
                indexes.put(title, point);
            }
        } catch (IOException ex) {
            Logger.getLogger(LogicLayer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return indexes;
    }

    /**
     * Returns the index of all spectra in the given MGF file.
     *
     * @return index map of peptides
     */
    private HashMap<String, IndexPoint> getPsmIndexMap() {
        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        File fileToRead = new File(basepath + "/VAADIN/Galaxy86-[Peptide_Shaker_on_data_81__PSM_Report].tabular");
        HashMap<String, IndexPoint> indexes = new HashMap<>();
        BufferedRandomAccessFile bufferedRandomAccessFile;
        try {
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
                point.setStartPoint(currentIndex - line.getBytes().length);
                point.setLength((line.getBytes().length));
                indexes.put(title, point);
            }

        } catch (IOException ex) {
            Logger.getLogger(LogicLayer.class.getName()).log(Level.SEVERE, null, ex);
        }

        return indexes;
    }

    /**
     * Deserializes the index of an mgf file.
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

}
