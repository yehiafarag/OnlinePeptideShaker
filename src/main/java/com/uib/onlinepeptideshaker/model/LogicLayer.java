package com.uib.onlinepeptideshaker.model;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.ToolsClient;
import com.github.jmchilton.blend4j.galaxy.beans.Tool;
import com.github.jmchilton.blend4j.galaxy.beans.ToolSection;
import com.uib.onlinepeptideshaker.model.beans.WebTool;

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
     * Get NelsUtility  web tool that has all the required tool information
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

}
