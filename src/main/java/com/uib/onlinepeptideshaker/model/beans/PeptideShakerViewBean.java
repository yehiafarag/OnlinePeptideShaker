package com.uib.onlinepeptideshaker.model.beans;

import java.util.List;

/**
 * This class contain the main data required for visualization and download
 * peptide-shaker results data
 *
 * @author Yehia Farag
 */
public class PeptideShakerViewBean {

    private String viewName;
    private String cps_url;
    private String id;
    private String proteinFileURL;
    private String peptideFileId;
    private String peptideFileIndex;
    private String PSMFileId;
    private String fastaFileId;
    private List<String> mgfIds;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public String getCps_url() {
        return cps_url;
    }

    public void setCps_url(String cps_url) {
        this.cps_url = cps_url;
    }

    public String getProteinFileURL() {
        return proteinFileURL;
    }

    public void setProteinFileURL(String proteinFileURL) {
        this.proteinFileURL = proteinFileURL;
    }

    public String getPeptideFileId() {
        return peptideFileId;
    }

    public void setPeptideFileId(String peptideFileId) {
        this.peptideFileId = peptideFileId;
    }

    public String getPeptideFileIndex() {
        return peptideFileIndex;
    }

    public void setPeptideFileIndex(String peptideFileIndex) {
        this.peptideFileIndex = peptideFileIndex;
    }

    public String getPSMFileId() {
        return PSMFileId;
    }

    public void setPSMFileId(String PSMFileId) {
        this.PSMFileId = PSMFileId;
    }

    public String getFastaFileId() {
        return fastaFileId;
    }

    public void setFastaFileId(String fastaFileId) {
        this.fastaFileId = fastaFileId;
    }

    public List<String> getMgfIds() {
        return mgfIds;
    }

    public void setMgfIds(List<String> mgfIds) {
        this.mgfIds = mgfIds;
    }

}
