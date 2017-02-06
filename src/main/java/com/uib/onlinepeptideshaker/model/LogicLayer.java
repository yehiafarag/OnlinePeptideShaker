package com.uib.onlinepeptideshaker.model;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.ToolsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Dataset;
import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.HistoryContents;
import com.github.jmchilton.blend4j.galaxy.beans.HistoryDetails;
import com.github.jmchilton.blend4j.galaxy.beans.Tool;
import com.github.jmchilton.blend4j.galaxy.beans.ToolSection;
import com.uib.onlinepeptideshaker.model.beans.GalaxyHistory;
import com.uib.onlinepeptideshaker.model.beans.WebTool;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents the business logic layer (BLL) where the main
 * computations functions will be located
 *
 * @author Yehia Farag
 */
public class LogicLayer {

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

        if (historyId == null) {
            currentGalaxyHistory.setUsedHistoryId(GALAXY_INSTANCE.getHistoriesClient().getHistories().get(2).getId());
        } else {
            currentGalaxyHistory.setUsedHistoryId(historyId);
        }
        Map<String, String> historySecMap = new LinkedHashMap<>();
        for (History history : GALAXY_INSTANCE.getHistoriesClient().getHistories()) {
            historySecMap.put(history.getId(), history.getName());
        }

        currentGalaxyHistory.setAvailableGalaxyHistoriesMap(historySecMap);
        Map<String, HistoryContents> galaxyDatasetMap = new LinkedHashMap<>();
        for (HistoryContents content : GALAXY_INSTANCE.getHistoriesClient().showHistoryContents(currentGalaxyHistory.getUsedHistoryId())) {
            if (content.isDeleted()) {
                continue;
            }
            Dataset ds = GALAXY_INSTANCE.getHistoriesClient().showDataset(currentGalaxyHistory.getUsedHistoryId(), content.getId());
            content.setHistoryContentType(GALAXY_INSTANCE.getHistoriesClient().showDataset(currentGalaxyHistory.getUsedHistoryId(), content.getId()).getDataTypeExt());
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
//        Dataset ds = GALAXY_INSTANCE.getHistoriesClient().showDataset(historyId, datasetId);        
//        ds.setDeleted(true);
//GALAXY_INSTANCE.getHistoriesClient().deleteHistory(historyId).setId(datasetId);
        
//        System.out.println("com.uib.onlinepeptideshaker.model.LogicLayer.deleteGalaxyHistoryDataseyt()" + ds.getName());
//          final String query = "delete *  from hda where id= '" + ds.getId()+ "'";
//          GALAXY_INSTANCE.getSearchClient().search(query);

//        HistoryDetails hdt = GALAXY_INSTANCE.getHistoriesClient().createHistoryDataset(historyId, ds);
    }

    /**
     * Update the selected history in galaxy.
     *
     * @param historyId selected history id could be from exist history or new.
     */
    public void updateSelectedHistory(String historyId) {
        initGalaxyHistory(historyId);

    }

}
