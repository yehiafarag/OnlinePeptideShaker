
package com.uib.onlinepeptideshaker.model.beans;

/**
 *This class contain the main data required for visualization and download peptide-shaker results data
 * @author Yehia Farag
 */
public class PeptideShakerViewBean {
   private String viewName;
    private String cps_url;
    private String id;

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
 
    
    
}
