package com.uib.onlinepeptideshaker.model.beans;

import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.HistoryContents;
import java.util.Map;

/**
 * This class represents the required information for Galaxy user history
 *
 * @author Yehia Farag
 */
public class GalaxyHistory {

    /**
     * Available user history sections.
     */
    private Map<String, String> availableGalaxyHistoriesMap;

    /**
     * SearchGUI results map files map.
     */
    private Map<String, HistoryContents> historyDatasetsMap;
    /**
     * Current used history id.
     */
    private String usedHistoryId;
      /**
     * Current used history on galaxy.
     */
    private History usedHistory;
     /**
     * PeptideShaker results to view map.
     */
    private Map<String, String[]> peptideShakerVisualizationMap;

    public History getUsedHistory() {
        return usedHistory;
    }

    public void setUsedHistory(History usedHistory) {
        this.usedHistory = usedHistory;
    }


    /**
     * Get available user history sections.
     *
     * @return map of history id and history name
     */
    public Map<String, String> getAvailableGalaxyHistoriesMap() {
        return availableGalaxyHistoriesMap;
    }

    /**
     * Set available user history sections map.
     *
     * @param availableGalaxyHistoriesMap map of history id and history name
     */
    public void setAvailableGalaxyHistoriesMap(Map<String, String> availableGalaxyHistoriesMap) {
        this.availableGalaxyHistoriesMap = availableGalaxyHistoriesMap;
    }

    /**
     * Get history datasets map
     *
     * @return map of histories datasets
     */
    public Map<String, HistoryContents> getHistoryDatasetsMap() {
        return historyDatasetsMap;
    }

    /**
     * Set history datasets map
     *
     * @param historyDatasetsMap map of histories datasets
     */
    public void setHistoryDatasetsMap(Map<String, HistoryContents> historyDatasetsMap) {
        this.historyDatasetsMap = historyDatasetsMap;
    }

    /**
     * Get current used history id
     *
     * @return String history id
     */
    public String getUsedHistoryId() {
        return usedHistoryId;
    }

    /**
     * Set current used history id
     *
     * @param usedHistoryId history id
     */
    public void setUsedHistoryId(String usedHistoryId) {
        this.usedHistoryId = usedHistoryId;
    }

    public Map<String, String[]> getPeptideShakerVisualizationMap() {
        return peptideShakerVisualizationMap;
    }

    public void setPeptideShakerVisualizationMap(Map<String, String[]> peptideShakerVisualizationMap) {
        this.peptideShakerVisualizationMap = peptideShakerVisualizationMap;
    }
}
