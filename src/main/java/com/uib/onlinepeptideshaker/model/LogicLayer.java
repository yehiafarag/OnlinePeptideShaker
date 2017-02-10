package com.uib.onlinepeptideshaker.model;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.ToolsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Dataset;
import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.HistoryContents;
import com.github.jmchilton.blend4j.galaxy.beans.HistoryDataset;
import com.github.jmchilton.blend4j.galaxy.beans.Tool;
import com.github.jmchilton.blend4j.galaxy.beans.ToolExecution;
import com.github.jmchilton.blend4j.galaxy.beans.ToolInputs;
import com.github.jmchilton.blend4j.galaxy.beans.ToolSection;
import com.github.jmchilton.blend4j.galaxy.beans.Workflow;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowInputs.WorkflowInput;
import com.github.jmchilton.blend4j.galaxy.beans.WorkflowOutputs;
import com.uib.onlinepeptideshaker.model.beans.GalaxyHistory;
import com.uib.onlinepeptideshaker.model.beans.WebTool;
import static com.vaadin.server.Sizeable.Unit.values;
import com.vaadin.server.VaadinService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import static jdk.nashorn.internal.objects.NativeJava.type;
import static jdk.nashorn.internal.objects.NativeJava.type;
import static jdk.nashorn.internal.objects.NativeJava.type;
import static jdk.nashorn.internal.objects.NativeJava.type;

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
    /**
     * Galaxy History bean.
     */
    private GalaxyHistory currentGalaxyHistory;

    /**
     * Initialize the main logic layer
     *
     */
    public LogicLayer() {
        fastaFilesMap = new LinkedHashMap<>();
        mgfFilesMap = new LinkedHashMap<>();
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
     * Initialize the Galaxy user history
     *
     * @param historyId Galaxy history id
     */
    private void initGalaxyHistory(String historyId) {

        fastaFilesMap.clear();
        mgfFilesMap.clear();
        if (historyId == null) {
            currentGalaxyHistory.setUsedHistoryId(GALAXY_INSTANCE.getHistoriesClient().getHistories().get(0).getId());
        } else {
            currentGalaxyHistory.setUsedHistoryId(historyId);
        }
        Map<String, String> historySecMap = new LinkedHashMap<>();
        for (History history : GALAXY_INSTANCE.getHistoriesClient().getHistories()) {
            historySecMap.put(history.getId(), history.getName());
            if (history.getId().equalsIgnoreCase(currentGalaxyHistory.getUsedHistoryId())) {
                currentGalaxyHistory.setUsedHistory(history);
            }
        }

        currentGalaxyHistory.setAvailableGalaxyHistoriesMap(historySecMap);
        Map<String, HistoryContents> galaxyDatasetMap = new LinkedHashMap<>();
        for (HistoryContents content : GALAXY_INSTANCE.getHistoriesClient().showHistoryContents(currentGalaxyHistory.getUsedHistoryId())) {
            if (content.isDeleted()) {
                continue;
            }
            Dataset ds = GALAXY_INSTANCE.getHistoriesClient().showDataset(currentGalaxyHistory.getUsedHistoryId(), content.getId());
            content.setHistoryContentType(GALAXY_INSTANCE.getHistoriesClient().showDataset(currentGalaxyHistory.getUsedHistoryId(), content.getId()).getDataTypeExt());
            if (content.getHistoryContentType().equalsIgnoreCase("fasta")) {
                fastaFilesMap.put(ds.getId(), ds.getName());
            } else if (content.getHistoryContentType().equalsIgnoreCase("mgf")) {
                mgfFilesMap.put(ds.getId(), ds.getName());
            }
            content.setUrl(ds.getFullDownloadUrl());
            galaxyDatasetMap.put(content.getId(), content);
        }
        currentGalaxyHistory.setHistoryDatasetsMap(galaxyDatasetMap);
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

//        HistoryDetails hdt = GALAXY_INSTANCE.getHistoriesClient().createHistoryDataset(historyId, hd);
//        System.out.println("at check content " + GALAXY_INSTANCE.getToolsClient(.showDataset(historyId, datasetId).());
//        hds.setContent(GALAXY_INSTANCE.getHistoriesClient().showProvenance(historyId, datasetId).);
//        GALAXY_INSTANCE.getHistoriesClient().deleteHistory
//        ds.setPurged(true);
//GALAXY_INSTANCE.getHistoriesClient().deleteHistory(historyId).setId(datasetId);
//        System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.deleteGalaxyHistoryDataseyt()" + ds.getName());
//          final String query = "delete *  from hda where id= '" + ds.getId()+ "'";
//          GALAXY_INSTANCE.getSearchClient().search(query);
//        HistoryDetails hdt = GALAXY_INSTANCE.getHistoriesClient()
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
        String json = readFile(new File(basepath + "/VAADIN/Galaxy-Workflow-onlinepeptideshaker.ga"));
        selectedWf = GALAXY_INSTANCE.getWorkflowsClient().importWorkflow(json);

        WorkflowInputs workflowInputs = new WorkflowInputs();
//        final WorkflowDetails workflowDetails =  GALAXY_INSTANCE.getWorkflowsClient().showWorkflow(selectedWf.getId());
//        System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.executeWorkFlow()" + workflowDetails.getInputs().size());
        workflowInputs.setWorkflowId(selectedWf.getId());
        workflowInputs.setDestination(new WorkflowInputs.ExistingHistory(currentGalaxyHistory.getUsedHistoryId()));
//         Map<String, Object> parameters = new HashMap<>();
//        final HashMap inputDict2 = new HashMap();
//        final HashMap values2 = new HashMap();
//        values2.put("src", "hda");
//        values2.put("id", fastaFileId);
////        
//
//
//        inputDict2.put("bach", Boolean.FALSE);
//        inputDict2.put("values2", values2);

//        parameters.put("input_database", inputDict2);
        WorkflowInput input = new WorkflowInputs.WorkflowInput(fastaFileId, WorkflowInputs.InputSourceType.HDA);
        WorkflowInput input2 = new WorkflowInputs.WorkflowInput(mgfIdsList.get(0), WorkflowInputs.InputSourceType.HDA);
        workflowInputs.setInput("0", input);
        workflowInputs.setInput("1", input2);
        final WorkflowOutputs output = GALAXY_INSTANCE.getWorkflowsClient().runWorkflow(workflowInputs);
        while (GALAXY_INSTANCE.getJobsClient().showJob(GALAXY_INSTANCE.getHistoriesClient().showProvenance(currentGalaxyHistory.getUsedHistoryId(), output.getOutputIds().get(1)).getJobId()).getState().equalsIgnoreCase("running")) {
        }
        updateSelectedHistory(currentGalaxyHistory.getUsedHistoryId());
        updateHistoryPresenter(currentGalaxyHistory);

        GALAXY_INSTANCE.getWorkflowsClient().deleteWorkflowRequest(selectedWf.getId());

////        Map<String, Object> parameters = new HashMap<>();
////        parameters.put("create_decoy", String.valueOf(creatDecoyDB.getSelectedButtonValue().equalsIgnoreCase("Yes")));
////        //create gene mapping
////        parameters.put("use_gene_mapping", String.valueOf(geneMappingBtn.getSelectedButtonValue().equalsIgnoreCase("Yes")));
////        database search enginxml data type
////        boolean selectAll = false;
////        if (DBSearchEnginsSelect.getValue().toString().equalsIgnoreCase("[]")) {
////            selectAll = true;
////        }
////
////        parameters.put("X!Tandem", String.valueOf(DBSearchEnginsSelect.isSelected("X!Tandem") || selectAll));
////        parameters.put("MSGF", String.valueOf(DBSearchEnginsSelect.isSelected("MS-GF+") || selectAll));
////        parameters.put("OMSSA", String.valueOf(DBSearchEnginsSelect.isSelected("OMSSA") || selectAll));
////        parameters.put("Comet", String.valueOf(DBSearchEnginsSelect.isSelected("Comet") || selectAll));
////
////        final HashMap inputDict2 = new HashMap();
////        final HashMap values2 = new HashMap();
////        values2.put("src", "hda");
////        values2.put("id", fastaFileId);
////        
////
////        inputDict2.put("bach", Boolean.FALSE);
////        inputDict2.put("values2", values2);
////
////        parameters.put("input_database", inputDict2); //"{'src': 'hda'"+Float.valueOf(',')+"id': '"+input_database_contenet.getId()+"'}";//
////
////        final ToolInputs toolInput = new ToolInputs(Search_GUI_Tool.getId(), parameters);
////        toolInput.setHistoryId(currentGalaxyHistory.getUsedHistoryId());
////
////        String mgfValues = mgfIndexes.toString().replaceFirst(",", "");
////        parameters.put("peak_lists", mgfValues);
////        ToolExecution exc = GALAXY_INSTANCE.getToolsClient().create(currentGalaxyHistory.getUsedHistory(), toolInput);
////        while (GALAXY_INSTANCE.getJobsClient().showJob(GALAXY_INSTANCE.getHistoriesClient().showProvenance(currentGalaxyHistory.getUsedHistoryId(), exc.getOutputs().get(0).getId()).getJobId()).getState().equalsIgnoreCase("running")) {
////        }
        updateSelectedHistory(currentGalaxyHistory.getUsedHistoryId());
        updateHistoryPresenter(currentGalaxyHistory);
    }

    public void executeSearchGUITool(String fastaFileId, List<String> mgfIdsList, List<String> searchEnginesList) {

        Map<String, Object> parameters = new HashMap<>();
//        parameters.put("create_decoy", String.valueOf(creatDecoyDB.getSelectedButtonValue().equalsIgnoreCase("Yes")));
        //create gene mapping
//        parameters.put("use_gene_mapping", String.valueOf(geneMappingBtn.getSelectedButtonValue().equalsIgnoreCase("Yes")));
//        database search enginxml data type
//        boolean selectAll = false;
//        if (DBSearchEnginsSelect.getValue().toString().equalsIgnoreCase("[]")) {
//            selectAll = true;
//        }

        boolean selectAll = searchEnginesList.isEmpty();
        parameters.put("X!Tandem", String.valueOf(searchEnginesList.contains("X!Tandem") || selectAll));
        parameters.put("MSGF", String.valueOf(searchEnginesList.contains("MS-GF+") || selectAll));
        parameters.put("OMSSA", String.valueOf(searchEnginesList.contains("OMSSA") || selectAll));
        parameters.put("Comet", String.valueOf(searchEnginesList.contains("Comet") || selectAll));

        final HashMap inputDict = new HashMap();
        final HashMap values = new HashMap();
        values.put("src", "hda");
        values.put("id", fastaFileId);
        inputDict.put("bach", Boolean.FALSE);
        inputDict.put("values", values);
        parameters.put("input_database", inputDict); //"{'src': 'hda'"+Float.valueOf(',')+"id': '"+input_database_contenet.getId()+"'}";//

       
         final HashMap inputDict2 = new HashMap();
        final HashMap values2 = new HashMap();
        values2.put("src", "hda");
        
        HashMap hdcaid = new HashMap();
        hdcaid.put(currentGalaxyHistory.getUsedHistoryId(), mgfIdsList.get(0));
//      mgfIdsList.get(0)+","+mgfIdsList.get(1)
        
        values2.put("id",hdcaid);
        inputDict2.put("bach", Boolean.TRUE);
        inputDict2.put("values", values2);
        parameters.put("peak_lists", inputDict2); //"{'src': 'hda'"+Float.valueOf(',')+"id': '"+input_database_contenet.getId()+"'}";//
        
        
        
        
        final ToolInputs toolInput = new ToolInputs(Search_GUI_Tool.getId(), parameters);
        toolInput.setHistoryId(currentGalaxyHistory.getUsedHistoryId());

//        String mgfValues = mgfIndexes.toString().replaceFirst(",", "");
//        parameters.put("peak_lists", mgfValues);
        ToolExecution exc = GALAXY_INSTANCE.getToolsClient().create(currentGalaxyHistory.getUsedHistory(), toolInput);
        while (GALAXY_INSTANCE.getJobsClient().showJob(GALAXY_INSTANCE.getHistoriesClient().showProvenance(currentGalaxyHistory.getUsedHistoryId(), exc.getOutputs().get(0).getId()).getJobId()).getState().equalsIgnoreCase("running")) {
        }
        updateSelectedHistory(currentGalaxyHistory.getUsedHistoryId());
        updateHistoryPresenter(currentGalaxyHistory);
    }

    private String readFile(File file) {
        String json = "";
        String line;

        try {
            // FileReader reads text files in the default encoding.
            FileReader fileReader
                    = new FileReader(file);

            try ( // Always wrap FileReader in BufferedReader.
                    BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                while ((line = bufferedReader.readLine()) != null) {
                    json += (line);
                }
                // Always close files.
            }
        } catch (FileNotFoundException ex) {
            System.out.println(
                    "Unable to open file '"
                    + "'");
        } catch (IOException ex) {
            System.out.println(
                    "Error reading file '"
                    + "'");
            // Or we could just do this: 
            // ex.printStackTrace();
        }
        return json;
    }

    public abstract void updateHistoryPresenter(GalaxyHistory currentGalaxyHistory);
}
