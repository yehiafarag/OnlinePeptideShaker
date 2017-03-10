package com.uib.onlinepeptideshaker.model;

import com.compomics.util.experiment.io.massspectrometry.MgfIndex;
//import com.compomics.util.io.FTPClient;
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
import com.ning.compress.lzf.LZFCompressingInputStream;
import com.ning.compress.lzf.LZFInputStream;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.uib.onlinepeptideshaker.model.beans.GalaxyHistory;
import com.uib.onlinepeptideshaker.model.beans.IndexPoint;
import com.uib.onlinepeptideshaker.model.beans.WebTool;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinSession;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

//import org.apache.http.auth.UsernamePasswordCredentials;
//import org.apache.http.client.HttpClient;
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
    private final LinkedHashMap<String, IndexPoint> peptideIndexes;
    /**
     * PSM indexes map.
     */
    private final HashMap<String, IndexPoint> psmIndexes;

    private final MgfIndex mgfFilesIndex;
    /**
     * Data utilities instance.
     */
    private GalaxyDataUtil dataUtil;
    private File mgfSubFile;

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
     * @param GALAXY_INSTANCE instance of the galaxy server
     */
    public void initializeTheLogicLayer(GalaxyInstance GALAXY_INSTANCE) {
        this.GALAXY_INSTANCE = GALAXY_INSTANCE;
        this.TOOLS_CLIENT = GALAXY_INSTANCE.getToolsClient();
        this.initializeToolsModel();
        this.currentGalaxyHistory = new GalaxyHistory();
        this.initGalaxyHistory(null);
        this.dataUtil = new GalaxyDataUtil(GALAXY_INSTANCE, peptideShakerVisualizationMap);
        VaadinSession.getCurrent().getSession().setAttribute("apiKey", GALAXY_INSTANCE.getApiKey());

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
//                GALAXY_INSTANCE.getHistoriesClient().deleteHistory(history.getId());
//                GALAXY_INSTANCE.getHistoriesClient().deleteHistoryRequest(history.getId());
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

            content.setUrl(ds.getFullDownloadUrl().split("display?key")[0]);
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
//        GalaxyServerRefresher gref = new GalaxyServerRefresher() {
//            @Override
//            public boolean process() {
//                System.out.println("at .process()  " + GALAXY_INSTANCE.getHistoriesClient().showDataset(currentGalaxyHistory.getUsedHistoryId(), historyDsId).getState());
//                return (GALAXY_INSTANCE.getHistoriesClient().showDataset(currentGalaxyHistory.getUsedHistoryId(), historyDsId).getState().equalsIgnoreCase("ok"));
//            }
//
//            @Override
//            public void end() {
//                REFRESHER.removeListener(this);
//                updateHistoryPresenter(currentGalaxyHistory);
//                System.err.println("end !");
//            }
//        };
//        REFRESHER.addListener(gref);
//        REFRESHER.setRefreshInterval(10000);
    }

    public Set<Object[]> loadPeptideShakerDataVisulization(String dataId) {
        dataUtil.loadPeptideShakerDataVisulization(dataId);
        return dataUtil.getProteinsTable();

    }

    public Set<Object[]> getPeptides(String accession, String jobId) {
        Set<Object[]> proteisnSet = dataUtil.getPeptides(accession, jobId);
        return proteisnSet;
    }

    public Set<Object[]> getPsm(String accession, String jobId) {
        Set<Object[]> proteisnSet = new LinkedHashSet<>();
        try {
            Set<Long> points = new TreeSet<>();

            for (String key : psmIndexes.keySet()) {

                if (key.split("__")[0].equals(accession)) {
                    points.add(psmIndexes.get(key).getStartPoint());
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

            try {
                InputStream inputStream = website.openStream();
                String line;
                long pointer = 0;
                br = new BufferedReader(new InputStreamReader(inputStream));
                for (long startIndex : points) {

                    br.skip(startIndex - pointer);
                    line = br.readLine();
                    if (line == null) {
                        pointer = startIndex;
                        System.out.println("at error com.uib.onlinepeptideshaker.model.LogicLayer.getPsm()");
                        continue;
                    }
                    pointer = startIndex + line.toCharArray().length + 1;
                    String[] arr = line.split("\\t");
                    Object[] obj = new Object[]{arr[0], arr[1], arr[2], arr[14], arr[10], arr[5], arr[6], arr[19]};
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

//    public void authGoogle() {
//        VaadinRequest request = VaadinService.getCurrentRequest();
//        WrappedSession session = VaadinSession.getCurrent().getSession();
//        final GoogleAuthHelper helper = new GoogleAuthHelper();
//        if (request.getParameter("code") == null
//                || request.getParameter("state") == null) {
//            /*
//	 * initial visit to the page
//             */
//            Label l = new Label("<a href='" + helper.buildLoginUrl() + "'>log in with google</a>");
//            Window w = new Window("login");
//            l.setContentMode(ContentMode.HTML);
//            UI.getCurrent().addWindow(w);
//            w.setSizeFull();
//            w.setContent(l);
//
//
//            /*
//	 * set the secure state token in session to be able to track what we sent to google
//             */
//            session.setAttribute("state", helper.getStateToken());
//
//        } else if (request.getParameter("code") != null && request.getParameter("state") != null && request.getParameter("state").equals(session.getAttribute("state"))) {
//
//            session.removeAttribute("state");
//
//            try {
//                /*
//                * Executes after google redirects to the callback url.
//                * Please note that the state request parameter is for convenience to differentiate
//                * between authentication methods (ex. facebook oauth, google oauth, twitter, in-house).
//                *
//                * GoogleAuthHelper()#getUserInfoJson(String) method returns a String containing
//                * the json representation of the authenticated user's information.
//                * At this point you should parse and persist the info.
//                 */
//                out.println(helper.getUserInfoJson(request.getParameter("code")));
//            } catch (IOException ex) {
//                Logger.getLogger(LogicLayer.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//    }
//
//    public HttpResponse executeGet(
//            HttpTransport transport, JsonFactory jsonFactory, String accessToken, GenericUrl url)
//            throws IOException {
//        Credential credential
//                = new Credential(BearerToken.authorizationHeaderAccessMethod()).setAccessToken(accessToken);
//        HttpRequestFactory requestFactory = transport.createRequestFactory(credential);
//        return requestFactory.buildGetRequest(url).execute();
//    }
    public Set<Object[]> getMGF(String accession, String jobId) {
        Set<Object[]> proteisnSet = new LinkedHashSet<>();
        try {
//               System.err.println("at find folder "+GALAXY_INSTANCE.getJobsClient().getJobs().iterator().next().getUrl());
//            System.out.println("at show " + GALAXY_INSTANCE.getHistoriesClient().showDataset(currentGalaxyHistory.getUsedHistoryId(), "bd2f206dec1cc58f"));
//            final String query = "select * from hda where id= 'bd2f206dec1cc58f' and metadata='accession' ";
//            SearchClient.SearchResponse res = GALAXY_INSTANCE.getSearchClient().search(query);
//            System.out.println("at " + res.getResults());

            String fileID = "bd2f206dec1cc58f";
//            String path = "https://test-fe.cbu.uib.no/galaxy/api/histories/d413a19dec13d11e/contents/132016f833b57406/display";
               String path = "http://129.177.123.195/datasets/059cb1d74e748f00/display?to_ext=mgf";
//            System.out.println("at download line"+GALAXY_INSTANCE.getHistoriesClient().showDataset(currentGalaxyHistory.getUsedHistoryId(), fileID).getDownloadUrl());
//            System.out.println("at full download line"+GALAXY_INSTANCE.getHistoriesClient().showDataset(currentGalaxyHistory.getUsedHistoryId(), fileID).getFullDownloadUrl());
//            System.out.println("at url line"+GALAXY_INSTANCE.getHistoriesClient().showDataset(currentGalaxyHistory.getUsedHistoryId(), fileID).getUrl());
//            System.out.println("at pro line"+GALAXY_INSTANCE.getHistoriesClient().showProvenance(currentGalaxyHistory.getUsedHistoryId(), fileID).getParameters());

//            GalaxyInstance tgalaxyInstance = GalaxyInstanceFactory.get("https://test-fe.cbu.uib.no/galaxy/", "9228c9cd3eccff77b6fc2e8d6f3c7d48");
//            File f = tgalaxyInstance.getHistoriesClient().downloadDatasetRange("d413a19dec13d11e", "132016f833b57406", 100, 200);
//            System.err.println("at path is " + f.length());
//            String path = currentGalaxyHistory.getHistoryDatasetsMap().get(fileID).getUrl() + "";
//https://usegalaxy.org/datasets/bbd44e69cb8906b5734d6a35f28f87b2/display?to_ext=tabular 
//    GALAXY_INSTANCE.getHistoriesClient().downloadDatasetRange(currentGalaxyHistory.getUsedHistoryId(), fileID, 100, 300);
            URL website = new URL(path);
            long startTime = System.nanoTime();
            try {
                long startIndex = mgfFilesIndex.getIndex(accession);
                int currentSpectraIndex = mgfFilesIndex.getSpectrumIndex(accession);
                long endIndex = mgfFilesIndex.getIndex(mgfFilesIndex.getSpectrumTitle(currentSpectraIndex + 1));
               InputStream in = website.openStream();
//                System.out.println("at compin " + in.available()); 
//                LZFCompressingInputStream compIn = new LZFCompressingInputStream(in);
//                  System.out.println("at compin " + compIn.available()); 
////                compIn.setUseFullReads(true);
////                 System.out.println("at skip ampunt is  "+compIn.skip(startIndex));
                 int skipper = 0;
                 long remain = startIndex;
//                 while(remain>0){
////                  remain-=  compIn.skip(remain);
//                 }
//                 LZFInputStream uin = new LZFInputStream(compIn);
               
                byte[] toread = new byte[100];
//                uin.read(toread);
                long endTime = System.nanoTime();
                long duration = (endTime - startTime) / 1000000l;
                System.out.println("at speed time I " + duration + "   ");                
                System.out.println("at to read I results " + new String(toread));

//                startTime = System.nanoTime();
//                InputStream in = website.openStream();
//                in.skip(startIndex);
//                toread = new byte[100];
//                in.read(toread);
//
//                System.out.println("at to read results II  " + new String(toread));

//                System.out.println("at pointer "+pointer+"  start "+startIndex);
//                DataInputStream dis = new DataInputStream(in);
//              in.skip((int)startIndex);
//                System.out.println("at size  " + dis.available());
//                URL location = IOUtils.class.getProtectionDomain().getCodeSource().getLocation();
//                System.out.println(location.getPath());
//                IURLProtocolHandler h = XugglerIO.getFactory().getHandler("http", path, URL_RDONLY_MODE);
//
//                Client client = Client.create();
//                WebResource webResource = client
//                        .resource(path);
//                webResource.header("content-type", "multipart/byteranges");
//                
//                ClientResponse response = webResource.queryParam("bytes", "'"+100+"'-'"+200+"'").queryParam("x-content-type-options", "multipart/byteranges").accept(MediaType.APPLICATION_JSON)
//                        .get(ClientResponse.class);
//
//                if (response.getStatus() != 200) {
//                    throw new RuntimeException("Failed : HTTP error code : "
//                            + response.getStatus());
//                }
//
////		String output = response.getEntity(String.class).subSequence(100, 300).toString();
//                System.out.println("Output from Server .... "+response.getEntityInputStream().available());
//                MultivaluedMap mvm = response.getHeaders();
//                for (Object str : mvm.keySet()) {
//                    System.out.println(str + "   " + mvm.get(str));
//                }
// allocate the stream ... only for example
// get an channel from the stream
//                ReadableByteChannel rbc = Channels.newChannel(in);
//                FileOutputStream fos = new FileOutputStream(file);
//                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
//                fos.close();
//                rbc.close();
//                final ReadableByteChannel inputChannel = Channels.newChannel(in);
//                
//// copy the channels
//                ChannelTools.fastChannelCopy(inputChannel, null);
//// closing the channels
//                inputChannel.close();
//                outputChannel.close() //                  channel = Channels.newChannel(in);
                //                ByteBuffer bb = ByteBuffer.allocate(100000000);
                //                int redCounter = 0;
                //                int ref = 1;
                //
                //                while (redCounter < endIndex && ref != -1) {
                //                    ref = channel.read(bb);
                //                    redCounter += ref;
                //                }
                //                bb.position((int) startIndex);
                //                byte[] body = new byte[(int) (endIndex - startIndex)];
                //                bb.get(body);
                ////                System.out.println("at body from galaxy " + new String(body));
                //                bb.flip();
                //                channel.close();
                //               FileUtils.
                //                IOUtils.skip(in,  (startIndex));
                //                        in     .skip(startIndex);
                byte[] body = new byte[(int) (endIndex - startIndex)];
//                System.out.println("at size  " + h.read(body, body.length));
//              
//                in.read(body);
//                System.out.println("at body from galaxy " + new String(body));

//                HttpsURLConnection myURLConnection = (HttpsURLConnection) new URL("https://test-fe.cbu.uib.no/galaxy/datasets/132016f833b57406/display?to_ext=mgf").openConnection();
//                HttpURLConnection myURLConnection = (HttpURLConnection) new URL("http://129.177.123.195/api/histories/f7bb1edd6b95db62/contents/bd2f206dec1cc58f/display;lines=20-30").openConnection();
//                myURLConnection.setRequestMethod("GET");
//                myURLConnection.setRequestProperty("Accept-Ranges", "bytes");
//                myURLConnection.setRequestProperty("If-Range", "A023EF02BD589BC472A2D6774EAE3C58");
//
//                HashMap input = new HashMap();
//                input.put("bytes", ContiguousSet.create(Range.closed(100, 200), DiscreteDomain.integers()));
//                System.out.println("at input " + input);
//                ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
//                ObjectOutputStream out = new ObjectOutputStream(byteOut);
//                out.writeObject(input);
//                final String encodedCredentials = javax.xml.bind.DatatypeConverter.(byteOut.toByteArray());
//                String jsonText = "{\"user\":\"doctoravatar@penzance.com\",\"forecast\":7,\"t\":\"vlIj\",\"zip\":94089}";
//                ClientResponse response = GALAXY_INSTANCE.getWebResource().path("histories/f7bb1edd6b95db62/contents/bd2f206dec1cc58f/display").header("RANGE", new String("bytes=10-20")).get(ClientResponse.class);
//.type(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, object);
//                myURLConnection.setRequestProperty("Range", "bytes=10-20");
//                myURLConnection.setUseCaches(true);
//                myURLConnection.setDoInput(true);
//                myURLConnection.setDoOutput(true);
//                myURLConnection.addRequestProperty("Cookie", "SimpleSAMLAuthToken=_0f439165772aa98b1a7b1591a1fe2e35293c472ea6;PHPSESSID=cb4dbb4f8e070d7bfb52a14f3b06d9b8;AuthMemCookie=_f642c2170182338db4c5979b5a45a499848035a957");
//                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
//                String inputLine;
//                while ((inputLine = in.readLine()) != null) {
//                    System.out.println(inputLine);
//                }
//                in.close();
//                    Dataset dataset = showDataset(historyId, datasetId);
//                    String fileExt = dataset.getDataTypeExt();
//
//                    File downloadedFile = super.getWebResourceContents(historyId).header("Range", "bytes=" + startRange + "-" + endRange + "")
//                            .path(datasetId).path("display").queryParam("to_ext", fileExt)
//                            .get(File.class);
//http://cistrome.org/ap/datasets/53db20a47f357257/display?to_ext=txt
//                   Cookie[] cookies =Page.getCurrent().getWebBrowser().
//                   for(Cookie c:cookies)
//                       System.out.println("at cooki "+c.getName());
//                HttpClient httpclient = new HttpClient();
//                Credentials defaultcreds = new UsernamePasswordCredentials("y.m.farag@gmail.com", "333411");
//                httpclient.getState().setCredentials(new AuthScope("http://cistrome.org/ap/datasets/53db20a47f357257/display?to_ext=txt", 80, AuthScope.ANY_REALM), defaultcreds);
//
//                httpclient.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
////                httpclient.getParams().setParameter("http.socket.timeout", 6000);
//                httpclient.getParams().setParameter("http.protocol.content-charset", "UTF-8");
//
//                HostConfiguration hostconfig = new HostConfiguration();
//                hostconfig.getParams().setParameter("Cache-Control", "max-age=7200, public");
//                hostconfig.getParams().setParameter("http.protocol.version", HttpVersion.HTTP_1_1);
////                hostconfig.setProxy("https://test-fe.cbu.uib.no/galaxy", 80);
//
//                GetMethod httpget = new GetMethod("http://cistrome.org/ap/datasets/53db20a47f357257/display?to_ext=txt");
//
//                HttpMethodParams mb = new HttpMethodParams();
//                mb.setVersion(HttpVersion.HTTP_1_1);
//                httpget.setParams(mb);
//                httpget.setRequestHeader("Accept-Ranges", "bytes");
//                httpget.setRequestHeader("Range", "bytes=10-20");
//                httpget.setRequestHeader("Content-Type", "text/plain");
//                httpget.setRequestHeader("Cache-Control", " max-age=0, s-maxage=7200");
//
////                httpget.getParams().setParameter("http.socket.timeout", 6000);
//                System.out.println("resp code " + httpclient.executeMethod(hostconfig, httpget));
//
//                System.out.println(httpget.getParams().getParameter("http.protocol.version"));
//
//                System.out.println(httpget.getResponseContentLength());
//                MultivaluedMap<String, String> headers = response.getHeaders();
////                   Map<String,List<String>>headers=                      myURLConnection.getHeaderFields();
//                for (String h : headers.keySet()) {
//                    System.out.println("at header is " + h + "  " + headers.get(h) + "  " + response.getStatus());
//                }
////        String key = responseObjects.get("api_key").toString();
//        System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.testRequest() "+responseObjects);
//
//          
//
//                    URLConnection conn = website.openConnection();
//                    conn.setRequestProperty("Range", "bytes=" + (startIndex) + "-" + (endIndex));
//                    conn.setDoInput(true);
//                    conn.setDoOutput(true);
//                    conn.setDefaultUseCaches(false);
//                InputStream in = conn.getInputStream();//
//                byte[] body = new byte[(int) (endIndex - startIndex)];//byteSource.read();
//                ChunkedInputStream cis = new ChunkedInputStream(httpget.getResponseBodyAsStream());
//                cis.read(body);
//                byte[] body = httpget.getResponseBody();
//                in.read(body);
//                System.err.println(" "+conn.getHeaderFields());
////              
                System.out.println("at str  " + "  input before " + " " + new String(body));
            } catch (IOException ex) {
                Logger.getLogger(LogicLayer.class.getName()).log(Level.SEVERE, null, ex);
            }

            long endTime = System.nanoTime();
            long duration = (endTime - startTime) / 1000000l;
            System.out.println("at speed time  II " + duration + "   ");
        } catch (MalformedURLException ex) {
            Logger.getLogger(LogicLayer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
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
    private LinkedHashMap<String, IndexPoint> getPeptidesIndexMap() {
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
            Logger
                    .getLogger(LogicLayer.class
                            .getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    private InputStream getFile(URL url) throws IOException {
//        InputStream is = url.openStream();
//        DataInputStream dis = new DataInputStream(is);
//        int size = dis.readInt();
//        ByteBuffer tmpBuffer = ByteBuffer.allocateDirect(size);
//        ReadableByteChannel channel = Channels.newChannel(is);
//      
//        channel.read(tmpBuffer);
//        is.close();
//        tmpBuffer.position(size)
//        buffer = tmpBuffer.asReadOnlyBuffer();

//         MappedByteBuffer buffer = new MappedByteBuffer
        //        HttpGet request = new HttpGet(url.toString());
        //        HttpClient client = HttpClientBuilder.create().build();
        //        request.addHeader("Range", "bytes=300-410 ");
        //       
        //        
        //        HttpResponse response = client.execute(request);
        //        response.setHeader("Accept-Ranges", "bytes");
        //        response.setHeader("Content-Range", "bytes 300-400/410");
        //        System.err.println("at resp " + response.getProtocolVersion() + "  " + response.getStatusLine().getReasonPhrase());
        //        ServletOutputStream out = response.getOutputStream();
        //        InputStream is = response.getEntity().getContent();
        //        return is;
        return null;

    }

    MappedByteBuffer map(FileChannel wrapped, URL url, long position, long size) throws IOException {

        return wrapped.map(MapMode.READ_ONLY, position, size);
    }

    private void test(URL url) {

//        javaxt.http.Request request = new javaxt.http.Request(url.getPath());
//        request.addHeader("Range", "bytes=300-410 ");
//
//        javaxt.http.Response response = request.getResponse();
//        java.io.InputStream inputStream = response.getInputStream();
//        System.out.println("at inputstream " + response.getHeaders());
//        ByteArrayOutputStream bas = new ByteArrayOutputStream();
//        String encoding = response.getHeader("Content-Encoding");
//        if (encoding.equalsIgnoreCase("gzip")) {
//
//            GZIPInputStream gzipInputStream = null;
//            byte[] buf = new byte[1024];
//            int len;
//            try {
//                gzipInputStream = new GZIPInputStream(inputStream);
//                while ((len = gzipInputStream.read(buf)) > 0) {
//                    bas.write(buf, 0, len);
//                }
//            } catch (Exception e) {
//            }
//
//            try {
//                gzipInputStream.close();
//            } catch (Exception e) {
//            }
//            try {
//                bas.close();
//            } catch (Exception e) {
//            }
//
//        }
//        try {
//            inputStream.close();
//        } catch (Exception e) {
//        }
    }

    private void testRequest() {
//        final com.sun.jersey.api.client.Client client = getJerseyClient();
//         WebResource resource = client.resource("http://129.177.123.195/").path("api");
//        final String unencodedCredentials = "yfa041@uib.no" + ":" + "333411";
//        final String encodedCredentials = javax.xml.bind.DatatypeConverter.printBase64Binary(unencodedCredentials.getBytes());
//       resource= resource.path("authenticate").path("baseauth");
//        Builder b  = resource
//                .header("Authorization", encodedCredentials).header("HTTP_CONTENT_LENGTH", new Integer("10"));
//        
//        final ClientResponse response = b
//                .get(ClientResponse.class);
//        if (response.getStatus() != 200) {
//            System.out.println("Failed to build Galaxy API key for supplied user e-mail and password.");
//        }
//        MultivaluedMap<String, String> headers = response.getHeaders();
////                   Map<String,List<String>>headers=                      myURLConnection.getHeaderFields();
//        for (String h : headers.keySet()) {
//            System.out.println("at header is " + h + "  " + headers.get(h) + "  " + response.getStatus());
//        }
//
//        final Map<String, Object> responseObjects = response.getEntity(Map.class);
////        String key = responseObjects.get("api_key").toString();
//        System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.testRequest() " + responseObjects);
    }

    private void testxython() {

        Connection connection = null;
//        try {
//            Class.forName("org.sqlite.JDBC");
//           
//            
//            String url = "jdbc:sqlite:sample.db";
//            // create a database connection
//            connection = DriverManager.getConnection(url);
//            Statement statement = connection.createStatement();
//            statement.setQueryTimeout(30);  // set timeout to 30 sec.
//
//            statement.executeUpdate("drop table if exists person");
//            statement.executeUpdate("create table person (id integer, name string)");
//            statement.executeUpdate("insert into person values(1, 'leo')");
//            statement.executeUpdate("insert into person values(2, 'yui')");
//            ResultSet rs = statement.executeQuery("select * from person");
//            while (rs.next()) {
//                // read the result set
//                System.out.println("name = " + rs.getString("name"));
//                System.out.println("id = " + rs.getInt("id"));
//            }
//        } catch (SQLException e) {
//            // if the error message is "out of memory", 
//            // it probably means no database file is found
//            System.err.println(e.getMessage());
//        } catch (ClassNotFoundException ex) {
//            Logger.getLogger(LogicLayer.class.getName()).log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                if (connection != null) {
//                    connection.close();
//                }
//            } catch (SQLException e) {
//                // connection close failed.
//                System.err.println(e);
//            }
//        }

//        FTPClient ftp = new FTPClient();
//        FTPClientConfig config = new FTPClientConfig();
//        ftp.configure(config);
//        boolean error = false;
//        try {
//            int reply;
//            String server = "129.177.123.195/api/histories/f7bb1edd6b95db62/contents/bd2f206dec1cc58f/display";
//            ftp.connect(server,80);
//            System.out.println("Connected to " + server + ".");
//            System.out.print(ftp.getReplyString());
//
//            // After connection attempt, you should check the reply code to verify
//            // success.
//            reply = ftp.getReplyCode();
//
//            if (!FTPReply.isPositiveCompletion(reply)) {
//                ftp.disconnect();
//                System.err.println("FTP server refused connection.");
//            }
//            // transfer files
//            ftp.logout();
//        } catch (IOException e) {
//            error = true;
//            e.printStackTrace();
//        } finally {
//            if (ftp.isConnected()) {
//                try {
//                    ftp.disconnect();
//                } catch (IOException ioe) {
//                    // do nothing
//                }
//            }
//        }
    }

    protected com.sun.jersey.api.client.Client getJerseyClient() {
        final ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        com.sun.jersey.api.client.Client client = com.sun.jersey.api.client.Client.create(clientConfig);

        return client;
    }

//    @GET
//    @Produces("audio/mp3")
//    public Response streamAudio(@HeaderParam("Range") String range) throws Exception {
//        return buildStream(audio, range);
//    }
//    private Response buildStream(final File asset, final String range) throws Exception {
//   // Firefox, Opera, IE do not send range headers
//    if (range == null) {
//        StreamingOutput streamer = new StreamingOutput() {
//            @Override
//            public void write(final OutputStream output) throws IOException, WebApplicationException {
//
//                final FileChannel inputChannel = new FileInputStream(asset).getChannel();
//                final WritableByteChannel outputChannel = Channels.newChannel(output);
//                try {
//                    inputChannel.transferTo(0, inputChannel.size(), outputChannel);
//                } finally {
//                    // closing the channels
//                    inputChannel.close();
//                    outputChannel.close();
//                }
//            }
//        };
//        return Response.ok(streamer).header(HttpHeaders.CONTENT_LENGTH, asset.length()).build();
//    }
//
//    String[] ranges = range.split("=")[1].split("-");
//    final int from = Integer.parseInt(ranges[0]);
//    /**
//     * Chunk media if the range upper bound is unspecified. Chrome sends "bytes=0-"
//     */
//    int to = chunk_size + from;
//    if (to >= asset.length()) {
//        to = (int) (asset.length() - 1);
//    }
//    if (ranges.length == 2) {
//        to = Integer.parseInt(ranges[1]);
//    }
//
//    final String responseRange = String.format("bytes %d-%d/%d", from, to, asset.length());
//    final RandomAccessFile raf = new RandomAccessFile(asset, "r");
//    raf.seek(from);
//
//    final int len = to - from + 1;
//    final MediaStreamer streamer = new MediaStreamer(len, raf);
//    Response.ResponseBuilder res = Response.ok(streamer).status(206)
//            .header("Accept-Ranges", "bytes")
//            .header("Content-Range", responseRange)
//            .header(HttpHeaders.CONTENT_LENGTH, streamer.getLenth())
//            .header(HttpHeaders.LAST_MODIFIED, new Date(asset.lastModified()));
//    }
}
