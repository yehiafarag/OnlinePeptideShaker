package com.uib.onlinepeptideshaker.model;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.HistoriesClient;
import com.github.jmchilton.blend4j.galaxy.beans.Dataset;
import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.HistoryContents;
import com.github.jmchilton.blend4j.galaxy.beans.Job;
import com.github.jmchilton.blend4j.galaxy.beans.JobDetails;
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
import com.github.wolfie.refresher.Refresher.RefreshListener;

import com.uib.onlinepeptideshaker.model.beans.OnlinePeptideShakerHistory;
import com.uib.onlinepeptideshaker.model.beans.PeptideShakerViewBean;
import com.uib.onlinepeptideshaker.model.beans.WebTool;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the business logic layer (BLL) where the main
 * computations functions will be located
 *
 * @author Yehia Farag
 */
public abstract class LogicLayer {

    /**
     * Used attributes.
     */
    private final Refresher REFRESHER;
    /**
     * Galaxy server instance.
     */
    private GalaxyInstance GALAXY_INSTANCE;
    /**
     * Galaxy PeptideShaker tool.
     */
    private WebTool Peptide_Shaker_Tool;
    /**
     * Galaxy SearchGUI tool.
     */
    private WebTool Search_GUI_Tool;
    /**
     * Online-PeptideShaker system history bean.
     */
    private OnlinePeptideShakerHistory system_history;
    private File userFolder;

    /**
     * Un-used attributes yet.
     */
//    /**
//     * FASTA files map.
//     */
//    private final Map<String, String> fastaFilesMap;
//    /**
//     * SearchGUIResults files map.
//     */
//    private final Map<String, String> searchGUIResultsFilesMap;
//    /**
//     * MGF (spectra) files map.
//     */
//    private final Map<String, String> mgfFilesMap;
//    /**
//     * MGF (spectra) files map.
//     */
//    private final Map<String, String> mgfFilesReindexMap;
//   
//    /**
//     * Galaxy tools client instance.
//     */
//    private ToolsClient TOOLS_CLIENT;
//
//    /**
//     * Galaxy NelsUtil_tool tool.
//     */
//    private WebTool nelsUtil_tool;
//    /**
//     * Galaxy convert tool.
//     */
//    private Tool converToTabularTool;
//    /**
//     * PeptideShaker results to view map.
//     */
//    private final Map<String, String[]> peptideShakerVisualizationMap;
//
//    private CollectionResponse collectionResponse;
//
//    private History jobsHistory;
//    /**
//     * Peptides indexes map.
//     */
//    private final LinkedHashMap<String, IndexPoint> peptideIndexes;
//    /**
//     * PSM indexes map.
//     */
//    private final HashMap<String, IndexPoint> psmIndexes;
//
//    private final MgfIndex mgfFilesIndex;
//    /**
//     * Data utilities instance.
//     */
    private GalaxyDataUtil dataUtil;
//    private File mgfSubFile;

    /**
     * Initialize the main logic layer
     *
     * @param REFRESHER recheck the connection with galaxy every amount of time
     */
    public LogicLayer(Refresher REFRESHER) {
        this.REFRESHER = REFRESHER;
        this.system_history = new OnlinePeptideShakerHistory();
    }

    /**
     * Initialize the main logic layer components
     *
     * @param GALAXY_INSTANCE instance of the galaxy server
     */
    public void connectToGalaxyServer(GalaxyInstance GALAXY_INSTANCE) {
        if (VaadinSession.getCurrent().getSession().getAttribute("ApiKey") != null) {
            String APIKey = VaadinSession.getCurrent().getSession().getAttribute("ApiKey").toString();
            if (!APIKey.equalsIgnoreCase(GALAXY_INSTANCE.getApiKey())) {
                //clean history and create new folder
                userFolder = new File(APIKey);
                if (userFolder.exists()) {
                    for (File tFile : userFolder.listFiles()) {
                        tFile.delete();
                    }
                }
                userFolder.delete();
            }
        }
        userFolder = new File(GALAXY_INSTANCE.getApiKey() + "");
        userFolder.mkdir();
        VaadinSession.getCurrent().getSession().setAttribute("ApiKey", GALAXY_INSTANCE.getApiKey() + "");

        this.GALAXY_INSTANCE = GALAXY_INSTANCE;
        this.initGalaxyHistory();
        this.initializeToolsModel();
        this.updateSystemHistory();
        this.dataUtil = new GalaxyDataUtil(GALAXY_INSTANCE, system_history, userFolder.getAbsolutePath());
        VaadinSession.getCurrent().getSession().setAttribute("currentGalaxy", GALAXY_INSTANCE);

    }

    /**
     * Initialize the minimum required history at galaxy server
     *
     * @param GALAXY_INSTANCE instance of the galaxy server
     */
    private void initGalaxyHistory() {

        List<History> historiesList = GALAXY_INSTANCE.getHistoriesClient().getHistories();
        Map<String, String> historiesMap = new LinkedHashMap<>();
        if (historiesList.isEmpty()) {
            History h = GALAXY_INSTANCE.getHistoriesClient().create(new History("Online-PeptideShaker-History"));
            system_history.setCurrent_galaxy_history(h.getId());
            system_history.setOnline_peptideShaker_job_history(GALAXY_INSTANCE.getHistoriesClient().create(new History("Online-PeptideShaker-Job-History")).getId());
            historiesMap.put(system_history.getCurrent_galaxy_history(), h.getName());
        } else {
            History tempHistoryMarker = null;
            for (History h : historiesList) {
                if (h.getName().equalsIgnoreCase("Online-PeptideShaker-Job-History")) {
                    system_history.setOnline_peptideShaker_job_history(h.getId());
                } else if (h.getName().equalsIgnoreCase("Online-PeptideShaker-History")) {
                    historiesMap.put(h.getId(), h.getName());
                    system_history.setCurrent_galaxy_history(h.getId());
                } else if (!GALAXY_INSTANCE.getHistoriesClient().showHistoryContents(h.getId()).isEmpty()) {
                    tempHistoryMarker = (h);
                    historiesMap.put(h.getId(), h.getName());
                } else {
                    historiesMap.put(h.getId(), h.getName());
                }
            }
            if (system_history.getOnline_peptideShaker_job_history() == null) {
                History h = GALAXY_INSTANCE.getHistoriesClient().create(new History("Online-PeptideShaker-Job-History"));
                system_history.setOnline_peptideShaker_job_history(h.getId());
            }
            if (system_history.getCurrent_galaxy_history() == null && tempHistoryMarker != null) {
                system_history.setCurrent_galaxy_history(tempHistoryMarker.getId());
            } else if (system_history.getCurrent_galaxy_history() == null) {
                system_history.setCurrent_galaxy_history(GALAXY_INSTANCE.getHistoriesClient().create(new History("Online-PeptideShaker-History")).getId());
            }
        }
        system_history.setAvailableGalaxyHistoriesNameMap(historiesMap);

    }

    /**
     * Initialize the logic of the tools
     *
     * @param galaxyInstance Galaxy server instance
     */
    private void initializeToolsModel() {
        List<ToolSection> sections = this.GALAXY_INSTANCE.getToolsClient().getTools();
        for (ToolSection toolSection : sections) {
            if (toolSection == null || toolSection.getElems() == null) {
                continue;
            }
            for (Tool galaxyTool : toolSection.getElems()) {
                if (galaxyTool.getName() == null) {
                    continue;
                }

                if (galaxyTool.getLink().contains("toolshed.g2.bx.psu.edu%2Frepos%2Fgalaxyp%2Fpeptideshaker%2Fsearch_gui%2F2.9.0")) {
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
     * Initialize System history.
     */
    private void updateSystemHistory() {
        Map<String, List<Dataset>> historyDSMap = new LinkedHashMap<>();
        Map<String, History> historysMap = new LinkedHashMap<>();
        List<History> historyList = GALAXY_INSTANCE.getHistoriesClient().getHistories();
        Map<String, JobDetails> jobInputIds = new LinkedHashMap<>();
        for (History h : historyList) {
            historysMap.put(h.getId(), h);
            List<HistoryContents> contentsList = GALAXY_INSTANCE.getHistoriesClient().showHistoryContents(h.getId());
            List<Dataset> datasetList = new ArrayList<>();
            for (HistoryContents hc : contentsList) {
                if (!hc.isDeleted()) {
                    Dataset ds = GALAXY_INSTANCE.getHistoriesClient().showDataset(h.getId(), hc.getId());//                    System.out.println("at ds info "+ds.getInfo());
                    ds.setInfo(GALAXY_INSTANCE.getHistoriesClient().showProvenance(h.getId(), hc.getId()).getJobId());
                    datasetList.add(ds);

                }
            }

            historyDSMap.put(h.getId(), datasetList);
        }
        List<Job> jobs = GALAXY_INSTANCE.getJobsClient().getJobsForHistory(system_history.getCurrent_galaxy_history());
        for (Job job : jobs) {
            try {
                JobDetails jobDetails = GALAXY_INSTANCE.getJobsClient().showJob(job.getId());
                jobInputIds.put(job.getId(), jobDetails);
            } catch (Exception e) {
            }
        }

        this.system_history.setJobInputIds(jobInputIds);
        this.system_history.setAvailableGalaxyHistoriesMap(historysMap);
        Set<String> datasetToReIndex = this.system_history.setHistoryMap(historyDSMap);
        //prpare map of files to convert
        this.system_history.addNewReIndexedDatasets(reIndexDatasets(datasetToReIndex));
    }

    /**
     * Re-index files on galaxy server by changing the file format to tabular so
     * the server can serve offset or chunk
     *
     * @param datasetToReIndex Set of datasets to re-index.
     */
    private Map<String, String> reIndexDatasets(Set<String> datasetToReIndex) {
        Map<String, String> indexMap = new LinkedHashMap<>();
        for (String ds : datasetToReIndex) {
            indexMap.put(ds, prepareAndConvertToTabular(ds));
        }
        return indexMap;
    }

    /**
     * Get PeptideShaker web tool that has all the required tool information
     *
     * @param datasetId the id of dataset need to be converted
     * @return String the generated dataset id
     */
    private String prepareAndConvertToTabular(String datasetId) {
        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
        File file = new File(basepath + "/VAADIN/Galaxy-Workflow-convertMGF.ga");
        String json = readWorkflowFile(file).replace("updated_MGF", datasetId);
        Workflow selectedWf = GALAXY_INSTANCE.getWorkflowsClient().importWorkflow(json);
        WorkflowInputs workflowInputs = new WorkflowInputs();
        workflowInputs.setWorkflowId(selectedWf.getId());
        workflowInputs.setDestination(new WorkflowInputs.ExistingHistory(system_history.getOnline_peptideShaker_job_history()));
        WorkflowInput input = new WorkflowInputs.WorkflowInput(datasetId, WorkflowInputs.InputSourceType.HDA);
        workflowInputs.setInput("0", input);
        final WorkflowOutputs output = GALAXY_INSTANCE.getWorkflowsClient().runWorkflow(workflowInputs);
        GALAXY_INSTANCE.getWorkflowsClient().deleteWorkflowRequest(selectedWf.getId());
        return output.getOutputIds().get(0);

    }

    /**
     * Read and convert the workflow file into string (json like string) so the
     * system can excute the workflow
     *
     * @param file the input file
     * @return the json string of the file content
     */
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

    /**
     * Initialize the Galaxy user history
     *
     * @param historyId Galaxy history id
     */
    public void loadGalaxyHistory(String historyId) {
        if (historyId != null) {
            system_history.setCurrent_galaxy_history(historyId);
        }
        checkHistory();
//        this.updateHistoryPresenter(system_history);

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
     * Get FASTA files map.
     *
     * @return Map dataset id and and dataset name
     */
    public Map<String, String> getFastaFilesMap() {
        return system_history.getFastaFilesMap();
    }

    /**
     * Get MGF (spectra) files map.
     *
     * @return Map dataset id and and dataset name
     */
    public Map<String, String> getMgfFilesMap() {
        return system_history.getMgfFilesMap();
    }

    /**
     * Get SearchGUIResults files map.
     *
     * @return Map dataset id and and dataset name
     */
    public Map<String, String> getSearchGUIResultsFilesMap() {
        return system_history.getSearchGUIResultsFilesMap();
    }

    /**
     * Run Online Peptide-Shaker work-flow
     *
     * @param fastaFileId fasta file dataset id
     * @param mgfIdsList list of MGF file dataset ids
     * @param searchEnginesList List of selected search engine names
     */
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
        workflowInputs.setDestination(new WorkflowInputs.ExistingHistory(system_history.getCurrent_galaxy_history()));

        WorkflowInput input = new WorkflowInputs.WorkflowInput(fastaFileId, WorkflowInputs.InputSourceType.HDA);

        workflowInputs.setInput("0", input);
        workflowInputs.setInput("1", input2);
        workflowInputs.setToolParameter(Peptide_Shaker_Tool.getToolId(), new ToolParameter("outputs", "cps"));
        final WorkflowOutputs output = GALAXY_INSTANCE.getWorkflowsClient().runWorkflow(workflowInputs);
        GALAXY_INSTANCE.getWorkflowsClient().deleteWorkflowRequest(selectedWf.getId());

        List<Dataset> newDss = new ArrayList<>();
        for (String oDs : output.getOutputIds()) {
            newDss.add(GALAXY_INSTANCE.getHistoriesClient().showDataset(system_history.getCurrent_galaxy_history(), oDs));
        }
        updateGalaxyHistory(newDss);
        checkHistory();

    }

    /**
     * Run SearchGUI tool
     *
     * @param fastaFileId fasta file dataset id
     * @param mgfIdsList list of MGF dataset ids
     * @param searchEnginesList list of search engines names
     */
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
        toolInput.setHistoryId(system_history.getCurrent_galaxy_history());
        ToolExecution exc = GALAXY_INSTANCE.getToolsClient().create(system_history.getAvailableGalaxyHistoriesMap().get(system_history.getCurrent_galaxy_history()), toolInput);
        updateGalaxyHistory(new ArrayList<>(exc.getOutputs()));
        checkHistory();

    }

    /**
     * Run PeptideShaker tool
     *
     * @param searchGUIResultsFileID search GUI file dataset id
     */
    public void executePeptideShakerTool(String searchGUIResultsFileID) {

        Map<String, Object> parameters = new HashMap<>();
        final HashMap inputDict = new HashMap();
        final HashMap values = initToolDatasetDict(searchGUIResultsFileID);
        inputDict.put("bach", Boolean.FALSE);
        inputDict.put("values", values);
        parameters.put("searchgui_input", inputDict);
        parameters.put("outputs", "cps");
        final ToolInputs toolInput = new ToolInputs(Peptide_Shaker_Tool.getId(), parameters);
        toolInput.setHistoryId(system_history.getCurrent_galaxy_history());
        ToolExecution exc = GALAXY_INSTANCE.getToolsClient().create(system_history.getAvailableGalaxyHistoriesMap().get(system_history.getCurrent_galaxy_history()), toolInput);
        List<Dataset> newDss = new ArrayList<>(exc.getOutputs());
        updateGalaxyHistory(newDss);
        checkHistory();
    }

    private HashMap initToolDatasetDict(String datasetId) {
        final HashMap values = new HashMap();
        values.put("src", "hda");
        values.put("id", datasetId);
        return values;

    }

    private void updateGalaxyHistory(List<Dataset> newDatasets) {
        if (newDatasets == null) {
            system_history.updateAllDatasetsReady();
        } else {
            system_history.addNewDatasets(newDatasets);
        }
        this.updateHistoryPresenter(system_history);

    }

    private void checkHistory() {
        REFRESHER.setRefreshInterval(30000);
        REFRESHER.addListener(new RefreshListener() {
            @Override
            public void refresh(Refresher source) {
                boolean ready = GALAXY_INSTANCE.getHistoriesClient().showHistory(system_history.getCurrent_galaxy_history()).isReady();
                if (ready) {
                    System.out.println("at --------------------- at the history is ready --------------------- ");
                    updateGalaxyHistory(null);
                    REFRESHER.removeListener(this);

                } else {
                    System.out.println("--------------------- at the history not ready --------------------- ");

                }
            }
        });

    }

    /**
     * Prepares a work flow which takes as input a collection list.
     *
     * @param inputSource The type of input source for this work flow.
     * @return A WorkflowInputs describing the work flow.
     * @throws InterruptedException
     */
    private WorkflowInput prepareWorkflowCollectionList(WorkflowInputs.InputSourceType inputSource, List<String> dsIds) {
        String historyId = system_history.getCurrent_galaxy_history();
        CollectionResponse collectionResponse = constructFileCollectionList(historyId, dsIds);
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

    /**
     * Load peptide shaker results
     *
     * @param results PeptideShakerResults bean
     * @return set of object array to initialize protein table
     */
    public Set<Object[]> loadPeptideShakerDataVisulization(PeptideShakerViewBean results) {
        dataUtil.loadPeptideShakerDataVisulization(results);
        return dataUtil.getProteinsTable();

    }

    /**
     * Get peptides information for the selected protein
     *
     * @param accession selected protein accession
     * @return set of object array to initialize peptides table
     */
    public Set<Object[]> getPeptides(String accession) {
        Set<Object[]> peptideTableInformation = dataUtil.getPeptidesInformationForSelectedProtein(accession);
        return peptideTableInformation;
    }
     /**
     * Get PSM information required for initializing and updating PSM table
     *
     * @param peptideSequence protein accession
     * @return Set of Object array required as data source for the table
     */
    public Set<Object[]> getPsmInformationForSelectedPeptide(String peptideSequence) {
        Set<Object[]> proteisnSet = dataUtil.getPsmInformationForSelectedPeptide(peptideSequence);
        return proteisnSet;
    }
//
    /**
     *Get MGF information required for initializing and updating MGF table
     * @param spectraTitle selected spectra title
     * @return Set of Object array required as data source for the table
     */
    public Set<Object[]> getMgfInformationForSelectedSpectra(String spectraTitle) {
        return dataUtil.getMgfInformationForSelectedSpectra(spectraTitle);}

//
//    /**
//     * Get NelsUtility web tool that has all the required tool information
//     *
//     * @return WebTool NelsUtil_tool
//     */
//    public WebTool getNelsUtil_tool() {
//        return nelsUtil_tool;
//    }
//
//   
//
//
//    
//
//    private void reFormatMGFandFastaFiles() {
//        for (String mgfFastaFileId : mgfFilesMap.keySet()) {
//            if (!mgfFilesReindexMap.containsKey(mgfFastaFileId)) {
//                mgfFilesReindexMap.put(mgfFastaFileId, prepareAndConvertToTabular(mgfFastaFileId));
//            }
//
//        }
//    }
//
//
//    /**
//     * Get current selected history
//     *
//     * @return OnlinePeptideShakerHistory current selected history bean
//     */
//    public OnlinePeptideShakerHistory getCurrentGalaxyHistory() {
//        return currentGalaxyHistory;
//    }
//
//    /**
//     * Create new Galaxy user history
//     *
//     * @param historyName new Galaxy history name
//     */
//    public void createNewHistory(String historyName) {
//        History newHistory = GALAXY_INSTANCE.getHistoriesClient().create(new History(historyName));
//        loadGalaxyHistory(newHistory.getId());
//
//    }
//
//    /**
//     * Delete history dataset from galaxy server
//     *
//     * @param historyId Galaxy history id
//     * @param datasetId Galaxy history dataset id
//     */
//    public void deleteGalaxyHistoryDataseyt(String historyId, String datasetId) {
//        final HistoryDataset hd = new HistoryDataset();
//        hd.setSource(HistoryDataset.Source.HDA);
//        hd.setContent(datasetId);
//
//    }
//
//    /**
//     * Update the selected history in galaxy.
//     *
//     * @param historyId selected history id could be from exist history or new.
//     */
//    public void updateSelectedHistory(String historyId) {
//        loadGalaxyHistory(historyId);
//
//    }
//
//
//    /**
//     * Get SearchGUIResults files map.
//     *
//     * @return Map dataset id and and dataset name
//     */
//    public Map<String, String> gete() {
//        return searchGUIResultsFilesMap;
//    }
//
//
    /**
     * Update the history listes
     *
     * @param currentGalaxyHistory
     */
    public abstract void updateHistoryPresenter(OnlinePeptideShakerHistory currentGalaxyHistory);

//
//    /**
//     * Constructs a list collection from the given files within the given
//     * history.
//     *
//     * @param historyId The id of the history to build the collection within.
//     * @param inputIds The IDs of the files to add to the collection.
//     * @return A CollectionResponse object for the constructed collection.
//     */
//    private CollectionResponse constructFileCollectionList(String historyId, List<String> inputIds) {
//        HistoriesClient historiesClient = GALAXY_INSTANCE.getHistoriesClient();
//        CollectionDescription collectionDescription = new CollectionDescription();
//        collectionDescription.setCollectionType("list");
//        collectionDescription.setName("collection");
//        for (String inputId : inputIds) {
//            HistoryDatasetElement element = new HistoryDatasetElement();
//            element.setId(inputId);
//            element.setName(inputId);
//
//            collectionDescription.addDatasetElement(element);
//        }
//
//        return historiesClient.createDatasetCollection(historyId, collectionDescription);
//    }
//
//
//    
//
   
//        Set<Object[]> proteisnSet = new LinkedHashSet<>();
//        try {
////               System.err.println("at find folder "+GALAXY_INSTANCE.getJobsClient().getJobs().iterator().next().getUrl());
////            System.out.println("at show " + GALAXY_INSTANCE.getHistoriesClient().showDataset(currentGalaxyHistory.getUsedHistoryId(), "bd2f206dec1cc58f"));
////            final String query = "select * from hda where id= 'bd2f206dec1cc58f' and metadata='accession' ";
////            SearchClient.SearchResponse res = GALAXY_INSTANCE.getSearchClient().search(query);
////            System.out.println("at " + res.getResults());
//
//            String fileID = "bd2f206dec1cc58f";
////            String path = "https://test-fe.cbu.uib.no/galaxy/api/histories/d413a19dec13d11e/contents/132016f833b57406/display";
//            String path = "http://129.177.123.195/datasets/059cb1d74e748f00/display?to_ext=mgf";
////            System.out.println("at download line"+GALAXY_INSTANCE.getHistoriesClient().showDataset(currentGalaxyHistory.getUsedHistoryId(), fileID).getDownloadUrl());
////            System.out.println("at full download line"+GALAXY_INSTANCE.getHistoriesClient().showDataset(currentGalaxyHistory.getUsedHistoryId(), fileID).getFullDownloadUrl());
////            System.out.println("at url line"+GALAXY_INSTANCE.getHistoriesClient().showDataset(currentGalaxyHistory.getUsedHistoryId(), fileID).getUrl());
////            System.out.println("at pro line"+GALAXY_INSTANCE.getHistoriesClient().showProvenance(currentGalaxyHistory.getUsedHistoryId(), fileID).getParameters());
//
////            GalaxyInstance tgalaxyInstance = GalaxyInstanceFactory.get("https://test-fe.cbu.uib.no/galaxy/", "9228c9cd3eccff77b6fc2e8d6f3c7d48");
////            File f = tgalaxyInstance.getHistoriesClient().downloadDatasetRange("d413a19dec13d11e", "132016f833b57406", 100, 200);
////            System.err.println("at path is " + f.length());
////            String path = currentGalaxyHistory.getHistoryDatasetsList().get(fileID).getUrl() + "";
////https://usegalaxy.org/datasets/bbd44e69cb8906b5734d6a35f28f87b2/display?to_ext=tabular 
////    GALAXY_INSTANCE.getHistoriesClient().downloadDatasetRange(currentGalaxyHistory.getUsedHistoryId(), fileID, 100, 300);
//            URL website = new URL(path);
//            long startTime = System.nanoTime();
//            try {
//                long startIndex = mgfFilesIndex.getIndex(accession);
//                int currentSpectraIndex = mgfFilesIndex.getSpectrumIndex(accession);
//                long endIndex = mgfFilesIndex.getIndex(mgfFilesIndex.getSpectrumTitle(currentSpectraIndex + 1));
//                InputStream in = website.openStream();
////                System.out.println("at compin " + in.available()); 
////                LZFCompressingInputStream compIn = new LZFCompressingInputStream(in);
////                  System.out.println("at compin " + compIn.available()); 
//////                compIn.setUseFullReads(true);
//////                 System.out.println("at skip ampunt is  "+compIn.skip(startIndex));
//                int skipper = 0;
//                long remain = startIndex;
////                 while(remain>0){
//////                  remain-=  compIn.skip(remain);
////                 }
////                 LZFInputStream uin = new LZFInputStream(compIn);
//
//                byte[] toread = new byte[100];
////                uin.read(toread);
//                long endTime = System.nanoTime();
//                long duration = (endTime - startTime) / 1000000l;
//                System.out.println("at speed time I " + duration + "   ");
//                System.out.println("at to read I results " + new String(toread));
//
////                startTime = System.nanoTime();
////                InputStream in = website.openStream();
////                in.skip(startIndex);
////                toread = new byte[100];
////                in.read(toread);
////
////                System.out.println("at to read results II  " + new String(toread));
////                System.out.println("at pointer "+pointer+"  start "+startIndex);
////                DataInputStream dis = new DataInputStream(in);
////              in.skip((int)startIndex);
////                System.out.println("at size  " + dis.available());
////                URL location = IOUtils.class.getProtectionDomain().getCodeSource().getLocation();
////                System.out.println(location.getPath());
////                IURLProtocolHandler h = XugglerIO.getFactory().getHandler("http", path, URL_RDONLY_MODE);
////
////                Client client = Client.create();
////                WebResource webResource = client
////                        .resource(path);
////                webResource.header("content-type", "multipart/byteranges");
////                
////                ClientResponse response = webResource.queryParam("bytes", "'"+100+"'-'"+200+"'").queryParam("x-content-type-options", "multipart/byteranges").accept(MediaType.APPLICATION_JSON)
////                        .get(ClientResponse.class);
////
////                if (response.getStatus() != 200) {
////                    throw new RuntimeException("Failed : HTTP error code : "
////                            + response.getStatus());
////                }
////
//////		String output = response.getEntity(String.class).subSequence(100, 300).toString();
////                System.out.println("Output from Server .... "+response.getEntityInputStream().available());
////                MultivaluedMap mvm = response.getHeaders();
////                for (Object str : mvm.keySet()) {
////                    System.out.println(str + "   " + mvm.get(str));
////                }
//// allocate the stream ... only for example
//// get an channel from the stream
////                ReadableByteChannel rbc = Channels.newChannel(in);
////                FileOutputStream fos = new FileOutputStream(file);
////                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
////                fos.close();
////                rbc.close();
////                final ReadableByteChannel inputChannel = Channels.newChannel(in);
////                
////// copy the channels
////                ChannelTools.fastChannelCopy(inputChannel, null);
////// closing the channels
////                inputChannel.close();
////                outputChannel.close() //                  channel = Channels.newChannel(in);
//                //                ByteBuffer bb = ByteBuffer.allocate(100000000);
//                //                int redCounter = 0;
//                //                int ref = 1;
//                //
//                //                while (redCounter < endIndex && ref != -1) {
//                //                    ref = channel.read(bb);
//                //                    redCounter += ref;
//                //                }
//                //                bb.position((int) startIndex);
//                //                byte[] body = new byte[(int) (endIndex - startIndex)];
//                //                bb.get(body);
//                ////                System.out.println("at body from galaxy " + new String(body));
//                //                bb.flip();
//                //                channel.close();
//                //               FileUtils.
//                //                IOUtils.skip(in,  (startIndex));
//                //                        in     .skip(startIndex);
//                byte[] body = new byte[(int) (endIndex - startIndex)];
////                System.out.println("at size  " + h.read(body, body.length));
////              
////                in.read(body);
////                System.out.println("at body from galaxy " + new String(body));
//
////                HttpsURLConnection myURLConnection = (HttpsURLConnection) new URL("https://test-fe.cbu.uib.no/galaxy/datasets/132016f833b57406/display?to_ext=mgf").openConnection();
////                HttpURLConnection myURLConnection = (HttpURLConnection) new URL("http://129.177.123.195/api/histories/f7bb1edd6b95db62/contents/bd2f206dec1cc58f/display;lines=20-30").openConnection();
////                myURLConnection.setRequestMethod("GET");
////                myURLConnection.setRequestProperty("Accept-Ranges", "bytes");
////                myURLConnection.setRequestProperty("If-Range", "A023EF02BD589BC472A2D6774EAE3C58");
////
////                HashMap input = new HashMap();
////                input.put("bytes", ContiguousSet.create(Range.closed(100, 200), DiscreteDomain.integers()));
////                System.out.println("at input " + input);
////                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
////                ObjectOutputStream out = new ObjectOutputStream(byteOut);
////                out.writeObject(input);
////                final String encodedCredentials = javax.xml.bind.DatatypeConverter.(byteOut.toByteArray());
////                String jsonText = "{\"user\":\"doctoravatar@penzance.com\",\"forecast\":7,\"t\":\"vlIj\",\"zip\":94089}";
////                ClientResponse response = GALAXY_INSTANCE.getWebResource().path("histories/f7bb1edd6b95db62/contents/bd2f206dec1cc58f/display").header("RANGE", new String("bytes=10-20")).get(ClientResponse.class);
////.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, object);
////                myURLConnection.setRequestProperty("Range", "bytes=10-20");
////                myURLConnection.setUseCaches(true);
////                myURLConnection.setDoInput(true);
////                myURLConnection.setDoOutput(true);
////                myURLConnection.addRequestProperty("Cookie", "SimpleSAMLAuthToken=_0f439165772aa98b1a7b1591a1fe2e35293c472ea6;PHPSESSID=cb4dbb4f8e070d7bfb52a14f3b06d9b8;AuthMemCookie=_f642c2170182338db4c5979b5a45a499848035a957");
////                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
////                String inputLine;
////                while ((inputLine = in.readLine()) != null) {
////                    System.out.println(inputLine);
////                }
////                in.close();
////                    Dataset dataset = showDataset(historyId, datasetId);
////                    String fileExt = dataset.getDataTypeExt();
////
////                    File downloadedFile = super.getWebResourceContents(historyId).header("Range", "bytes=" + startRange + "-" + endRange + "")
////                            .path(datasetId).path("display").queryParam("to_ext", fileExt)
////                            .get(File.class);
////http://cistrome.org/ap/datasets/53db20a47f357257/display?to_ext=txt
////                   Cookie[] cookies =Page.getCurrent().getWebBrowser().
////                   for(Cookie c:cookies)
////                       System.out.println("at cooki "+c.getName());
////                HttpClient httpclient = new HttpClient();
////                Credentials defaultcreds = new UsernamePasswordCredentials("y.m.farag@gmail.com", "333411");
////                httpclient.getState().setCredentials(new AuthScope("http://cistrome.org/ap/datasets/53db20a47f357257/display?to_ext=txt", 80, AuthScope.ANY_REALM), defaultcreds);
////
////                httpclient.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
//////                httpclient.getParams().setParameter("http.socket.timeout", 6000);
////                httpclient.getParams().setParameter("http.protocol.content-charset", "UTF-8");
////
////                HostConfiguration hostconfig = new HostConfiguration();
////                hostconfig.getParams().setParameter("Cache-Control", "max-age=7200, public");
////                hostconfig.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
//////                hostconfig.setProxy("https://test-fe.cbu.uib.no/galaxy", 80);
////
////                GetMethod httpget = new GetMethod("http://cistrome.org/ap/datasets/53db20a47f357257/display?to_ext=txt");
////
////                HttpMethodParams mb = new HttpMethodParams();
////                mb.setVersion(HttpVersion.HTTP_1_1);
////                httpget.setParams(mb);
////                httpget.setRequestHeader("Accept-Ranges", "bytes");
////                httpget.setRequestHeader("Range", "bytes=10-20");
////                httpget.setRequestHeader("Content-Type", "text/plain");
////                httpget.setRequestHeader("Cache-Control", " max-age=0, s-maxage=7200");
////
//////                httpget.getParams().setParameter("http.socket.timeout", 6000);
////                System.out.println("resp code " + httpclient.executeMethod(hostconfig, httpget));
////
////                System.out.println(httpget.getParams().getParameter("http.protocol.version"));
////
////                System.out.println(httpget.getResponseContentLength());
////                MultivaluedMap<String, String> headers = response.getHeaders();
//////                   Map<String,List<String>>headers=                      myURLConnection.getHeaderFields();
////                for (String h : headers.keySet()) {
////                    System.out.println("at header is " + h + "  " + headers.get(h) + "  " + response.getStatus());
////                }
//////        String key = responseObjects.get("api_key").toString();
////        System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.testRequest() "+responseObjects);
////
////          
////
////                    URLConnection conn = website.openConnection();
////                    conn.setRequestProperty("Range", "bytes=" + (startIndex) + "-" + (endIndex));
////                    conn.setDoInput(true);
////                    conn.setDoOutput(true);
////                    conn.setDefaultUseCaches(false);
////                InputStream in = conn.getInputStream();//
////                byte[] body = new byte[(int) (endIndex - startIndex)];//byteSource.read();
////                ChunkedInputStream cis = new ChunkedInputStream(httpget.getResponseBodyAsStream());
////                cis.read(body);
////                byte[] body = httpget.getResponseBody();
////                in.read(body);
////                System.err.println(" "+conn.getHeaderFields());
//////              
//                System.out.println("at str  " + "  input before " + " " + new String(body));
//            } catch (IOException ex) {
//                Logger.getLogger(LogicLayer.class.getName()).log(Level.SEVERE, null, ex);
//            }
//
//            long endTime = System.nanoTime();
//            long duration = (endTime - startTime) / 1000000l;
//            System.out.println("at speed time  II " + duration + "   ");
//        } catch (MalformedURLException ex) {
//            Logger.getLogger(LogicLayer.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(LogicLayer.class.getName()).log(Level.SEVERE, null, ex);
//        }
//        return proteisnSet;
//    }
//
//    private Set<Object[]> readPsmIndexFile(File proteinsFile) {
//        Set<Object[]> proteisnSet = new LinkedHashSet<>();
//        BufferedReader bufRdr;
//        String line;
//        int row = 0;
//        try {
//            FileReader fr = new FileReader(proteinsFile);
//            bufRdr = new BufferedReader(fr);
////            bufRdr.readLine();
//            while ((line = bufRdr.readLine()) != null && row < 1000) {
//                System.err.println("at peptides found " + line);
//                String[] arr = line.split("\\t");
//                if (arr.length < 8) {
//                    continue;
//                }
//                Object[] obj = new Object[]{arr[0], arr[1], arr[2], arr[5], arr[6], arr[7]};
//                proteisnSet.add(obj);
//            }
//
//        } catch (IOException | NumberFormatException e) {
//            e.printStackTrace();
//            System.out.println(e.getLocalizedMessage());
//        }//e.printStackTrace();}
//        return proteisnSet;
//    }
//
//    /**
//     * Returns the index of all spectra in the given MGF file.
//     *
//     * @return index map of peptides
//     */
//    private LinkedHashMap<String, IndexPoint> getPeptidesIndexMap() {
//        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
//        File fileToRead = new File(basepath + "/VAADIN/Galaxy7-[Peptide_Shaker_on_data_6__Peptide_Report].tabular");
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
//        } catch (IOException ex) {
//            Logger.getLogger(LogicLayer.class
//                    .getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return indexes;
//    }
//
//    /**
//     * Returns the index of all spectra in the given MGF file.
//     *
//     * @return index map of peptides
//     */
//    private HashMap<String, IndexPoint> getPsmIndexMap() {
//        String basepath = VaadinService.getCurrent().getBaseDirectory().getAbsolutePath();
//        File fileToRead = new File(basepath + "/VAADIN/Galaxy86-[Peptide_Shaker_on_data_81__PSM_Report].tabular");
//        HashMap<String, IndexPoint> indexes = new HashMap<>();
//        BufferedRandomAccessFile bufferedRandomAccessFile;
//        try {
//            bufferedRandomAccessFile = new BufferedRandomAccessFile(fileToRead, "r", 1024 * 100);
//
//            long currentIndex = 0;
//            String title;
//            int lineCounter = 0;
//            String line;
//            bufferedRandomAccessFile.getNextLine();
//            while ((line = bufferedRandomAccessFile.getNextLine()) != null) {
////            line = line.trim();
//                title = line.split("\\t")[2] + "__" + lineCounter++;
//                currentIndex = bufferedRandomAccessFile.getFilePointer();
//                IndexPoint point = new IndexPoint();
//                point.setStartPoint(currentIndex - line.toCharArray().length - 1);
//                point.setLength((line.toCharArray().length));
//                indexes.put(title, point);
//
//            }
//
//        } catch (IOException ex) {
//            Logger.getLogger(LogicLayer.class
//                    .getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return indexes;
//    }
//
//    
}
