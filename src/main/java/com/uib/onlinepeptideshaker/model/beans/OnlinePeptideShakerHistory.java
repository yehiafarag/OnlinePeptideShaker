package com.uib.onlinepeptideshaker.model.beans;

import com.github.jmchilton.blend4j.galaxy.beans.Dataset;
import com.github.jmchilton.blend4j.galaxy.beans.History;
import com.github.jmchilton.blend4j.galaxy.beans.JobDetails;
import com.github.jmchilton.blend4j.galaxy.beans.JobInputOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents the required information for Galaxy user history
 *
 * @author Yehia Farag
 */
public class OnlinePeptideShakerHistory {

    /**
     * Current selected history on galaxy.
     */
    private String current_galaxy_history;
    /**
     * current Online-PeptideShaker job history.
     */
    private String online_peptideShaker_job_history;
    /**
     * The entire user galaxy history contents map.
     */
    private Map<String, List<Dataset>> historyMap;
    /**
     * The MGF-Fasta-Re-indexed map.
     */
    private Map<String, String> mgf_fasta_reindex_map;
    /**
     * Available user history sections.
     */
    private Map<String, String> availableGalaxyHistoriesNameMap;
    /**
     * PeptideShaker results to view map.
     */
    private final Map<String, PeptideShakerViewBean> peptideShakerVisualizationResultsSet;
    /**
     * Available history sections.
     */
    private Map<String, History> availableGalaxyHistoriesMap;
    private Map<String, JobDetails> jobInputIds;

    public void setJobInputIds(Map<String, JobDetails> jobInputIds) {
        this.jobInputIds = jobInputIds;
    }

    /**
     * Constructor to initialize the main variables.
     */
    public OnlinePeptideShakerHistory() {
        peptideShakerVisualizationResultsSet = new LinkedHashMap<>();
    }

    public Map<String, History> getAvailableGalaxyHistoriesMap() {
        return availableGalaxyHistoriesMap;
    }

    public void setAvailableGalaxyHistoriesMap(Map<String, History> availableGalaxyHistoriesMap) {
        this.availableGalaxyHistoriesMap = availableGalaxyHistoriesMap;
    }

    /**
     * Get the current selected history on galaxy.
     *
     * @return current_galaxy_history
     */
    public String getCurrent_galaxy_history() {
        return current_galaxy_history;
    }

    /**
     * Set the current selected history on galaxy.
     *
     * @param current_galaxy_history current active galaxy history
     */
    public void setCurrent_galaxy_history(String current_galaxy_history) {
        this.current_galaxy_history = current_galaxy_history;
    }

    /**
     * Get the current Online-PeptideShaker job history.
     *
     * @return online_peptideShaker_job_history get the history that contain the
     * Re-indexed MGF and Fasta-files
     */
    public String getOnline_peptideShaker_job_history() {
        return online_peptideShaker_job_history;
    }

    /**
     * Set the current Online-PeptideShaker job history.
     *
     * @param online_peptideShaker_job_history the history that contain the
     * Re-indexed MGF and Fasta-files
     *
     */
    public void setOnline_peptideShaker_job_history(String online_peptideShaker_job_history) {
        this.online_peptideShaker_job_history = online_peptideShaker_job_history;

    }

    /**
     * Set the entire user galaxy history contents map .
     *
     * @param historyMap Map the history id to history contents.
     * @return set of MGF, and Fasta-file datasets need to re-index on galaxy
     * server
     */
    public Set<String> setHistoryMap(Map<String, List<Dataset>> historyMap) {
        this.historyMap = historyMap;
        return checkDatasets();
    }

    /**
     * Check if all MGF files mapped to indexed files in job history .
     *
     * @return set of MGF, and Fasta-file datasets need to re-index on galaxy
     * server
     */
    private Set<String> checkDatasets() {
        Set<String> datasetsIds = new LinkedHashSet<>();
        Set<String> filteredDatasetIds = new LinkedHashSet<>();
        mgf_fasta_reindex_map = new HashMap<>();
        for (String historyId : historyMap.keySet()) {
            if (historyId.equalsIgnoreCase(online_peptideShaker_job_history)) {
                continue;
            }

            datasetsIds.addAll(getMGF_FastaFilesIds(historyId));
        }
        for (Dataset ds : historyMap.get(online_peptideShaker_job_history)) {
            mgf_fasta_reindex_map.put(ds.getName(), ds.getId());

        }

        for (String ds : datasetsIds) {
            if (!mgf_fasta_reindex_map.containsKey(ds)) {
                filteredDatasetIds.add(ds);
            }
        }
        return filteredDatasetIds;
    }

    /**
     * Get all MGF files for the selected history .
     *
     * @param historyID selected history id
     * @param fileType fasta / mgf data type
     * @return map of fasta/MGF name to id server
     */
    private Map<String, String> getFastaMGFFiles(String historyID, String fileType) {
        Map<String, String> datasets = new LinkedHashMap<>();
        List<Dataset> fullDs = historyMap.get(historyID);
        for (Dataset ds : fullDs) {
            if (ds.getDataTypeExt().equalsIgnoreCase(fileType)) {
                datasets.put(ds.getId(), ds.getName());
            }
        }
        return datasets;

    }

    /**
     * Get all MGF files for the selected history .
     *
     * @param historyID selected history id
     * @return set of MGF and fasta files datasets ids available in this history
     * on galaxy server
     */
    private Set<String> getMGF_FastaFilesIds(String historyID) {
        Set<String> datasets = new LinkedHashSet<>();
        List<Dataset> fullDs = historyMap.get(historyID);
        for (Dataset ds : fullDs) {
            if (ds.getDataTypeExt().equalsIgnoreCase("mgf") || ds.getDataTypeExt().equalsIgnoreCase("fasta")) {
                datasets.add(ds.getId());
            }
        }
        return datasets;

    }

    /**
     * Add new output datasets
     *
     * @param datasetmap new datasets
     */
    public void addNewDatasets(List<Dataset> datasetmap) {
        historyMap.get(current_galaxy_history).addAll(datasetmap);
    }

    /**
     * Update the history with full datasets
     *
     * @param datasetmap new datasets
     */
    public void updateAllDatasetsReady() {
        List<Dataset> datasetmap = historyMap.get(current_galaxy_history);
        for (Dataset ds : datasetmap) {
            ds.setState("ok");
        }
        initPeptideShakerViewResultsSet();

    }

    /**
     * Add new re-indexed datasets
     *
     * @param new_mgf_fasta_reindex_map updated indexes
     */
    public void addNewReIndexedDatasets(Map<String, String> new_mgf_fasta_reindex_map) {
        if (mgf_fasta_reindex_map == null) {
            this.mgf_fasta_reindex_map = new_mgf_fasta_reindex_map;
        } else {
            this.mgf_fasta_reindex_map.putAll(new_mgf_fasta_reindex_map);
        }
        initPeptideShakerViewResultsSet();
    }

    public String getReIndexedFile(String fasta_mgf_file_id) {
        if (mgf_fasta_reindex_map.containsKey(fasta_mgf_file_id)) {
            return mgf_fasta_reindex_map.get(fasta_mgf_file_id);
        } else {
            System.out.println("at not exist fasta/mgf file need to reIndex");
            return fasta_mgf_file_id;
        }

    }

    /**
     * Check and create PeptideShakerViewBean that has all information required
     * to view data
     */
    private void initPeptideShakerViewResultsSet() {
        peptideShakerVisualizationResultsSet.clear();
        List<Dataset> datasetmap = historyMap.get(current_galaxy_history);
        Map<String, String> SearchGUIMap = new HashMap<>();
        PeptideShakerViewBean psv;
        for (Dataset ds : datasetmap) {

            if (ds.getDataTypeExt().equalsIgnoreCase("tabular") || ds.getDataTypeExt().equalsIgnoreCase("peptideshaker_archive")) {
                if (peptideShakerVisualizationResultsSet.containsKey(ds.getInfo())) {
                    psv = peptideShakerVisualizationResultsSet.get(ds.getInfo());
                } else {
                    psv = new PeptideShakerViewBean();
                    psv.setId(ds.getInfo());
                    psv.setViewName(ds.getName().split(":")[0]);
                    peptideShakerVisualizationResultsSet.put(psv.getId(), psv);
                }

                if (ds.getName().endsWith("Protein Report")) {
                    psv.setProteinFileURL(ds.getFullDownloadUrl());
                    ds.setVisible(false);
                } else if (ds.getName().endsWith("Peptide Report")) {
                    psv.setPeptideFileId(ds.getId());
                    ds.setVisible(false);
                } else if (ds.getName().endsWith("PSM Report")) {
                    psv.setPSMFileId(ds.getId());
                    ds.setVisible(false);
                } else if (ds.getDataTypeExt().equalsIgnoreCase("peptideshaker_archive")) {
                    psv.setCps_url(ds.getFullDownloadUrl());
                }

            } else if (ds.getDataTypeExt().equalsIgnoreCase("searchgui_archive")) {
                SearchGUIMap.put(ds.getId(), ds.getInfo());

            }

        }
        for (String jobs : peptideShakerVisualizationResultsSet.keySet()) {
            psv = peptideShakerVisualizationResultsSet.get(jobs);
            if (jobInputIds.containsKey(psv.getId())) {
                JobDetails jobDetail = jobInputIds.get(psv.getId());
                JobInputOutput searchGUIInputId = jobDetail.getInputs().get("searchgui_input");
                if (searchGUIInputId != null) {
                    String initialJobId = SearchGUIMap.get(searchGUIInputId.getId());
                    if (jobInputIds.containsKey(initialJobId)) {
                        jobDetail = jobInputIds.get(initialJobId);
                        psv.setFastaFileId(jobDetail.getInputs().get("input_database").getId());
                        List<String> MGFList = new ArrayList<>();
                        MGFList.add(jobDetail.getInputs().get("peak_lists").getId());
                        psv.setMgfIds(MGFList);

                    }

                }

            }

        }

    }

    /**
     * Get available user history sections.
     *
     * @return map of history id and history name
     */
    public Map<String, String> getAvailableGalaxyHistoriesNameMap() {
        return availableGalaxyHistoriesNameMap;
    }

    /**
     * Set available user history sections map.
     *
     * @param availableGalaxyHistoriesNameMap map of history id and history name
     */
    public void setAvailableGalaxyHistoriesNameMap(Map<String, String> availableGalaxyHistoriesNameMap) {
        this.availableGalaxyHistoriesNameMap = availableGalaxyHistoriesNameMap;
    }

    /**
     * Get history datasets list
     *
     * @return List of datasets for current history
     */
    public List<Dataset> getHistoryDatasetsList() {
        return historyMap.get(current_galaxy_history);
    }

    /**
     * Get peptide-shaker ready to view data set
     *
     * @return set of peptide-shaker data results
     */
    public Map<String, PeptideShakerViewBean> getPeptideShakerVisualizationResultsSet() {
        return peptideShakerVisualizationResultsSet;
    }

    /**
     * Get FASTA files map.
     *
     * @return Map dataset id and and dataset name
     */
    public Map<String, String> getFastaFilesMap() {
        return getFastaMGFFiles(current_galaxy_history, "fasta");
    }

    /**
     * Get MGF (spectra) files map.
     *
     * @return Map dataset id and and dataset name
     */
    public Map<String, String> getMgfFilesMap() {
        return getFastaMGFFiles(current_galaxy_history, "mgf");
    }

    /**
     * Get SearchGUIResults files map.
     *
     * @return Map dataset id and and dataset name
     */
    public Map<String, String> getSearchGUIResultsFilesMap() {
        return getFastaMGFFiles(current_galaxy_history, "searchgui_archive");
    }

    /**
     * SearchGUI results map files map.
     */
//    private Map<String, HistoryContents> historyDatasetsMap;
//    /**
//     * Current used history id.
//     */
//    private String usedHistoryId;
//    /**
//     * Current used history on galaxy.
//     */
//    private History usedHistory;
//    /**
//     * Current temp working history on galaxy.
//     */
//    private History tempHistory;
//    /**
//     *
//     * @return
//     */
//    public History getUsedHistory() {
//        return usedHistory;
//    }
//
//    /**
//     *
//     * @param usedHistory
//     */
//    public void setUsedHistory(History usedHistory) {
//        this.usedHistory = usedHistory;
//    }
//    /**
//     * Set history datasets map
//     *
//     * @param historyDatasetsMap map of histories datasets
//     */
//    public void setHistoryDatasetsMap(Map<String, HistoryContents> historyDatasetsMap) {
//        this.historyDatasetsMap = historyDatasetsMap;
//    }
//    /**
//     * Get current used history id
//     *
//     * @return String history id
//     */
//    public String getUsedHistoryId() {
//        return usedHistoryId;
//    }
//
//    /**
//     * Set current used history id
//     *
//     * @param usedHistoryId history id
//     */
//    public void setUsedHistoryId(String usedHistoryId) {
//        this.usedHistoryId = usedHistoryId;
//    }
//    /**
//     *
//     * @return
//     */
//    public History getTempHistory() {
//        return tempHistory;
//    }
//
//    /**
//     *
//     * @param tempHistory
//     */
//    public void setTempHistory(History tempHistory) {
//        this.tempHistory = tempHistory;
//    }
}
