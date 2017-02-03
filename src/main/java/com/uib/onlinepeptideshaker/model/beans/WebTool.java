package com.uib.onlinepeptideshaker.model.beans;

import com.github.jmchilton.blend4j.galaxy.beans.Tool;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represent galaxy tools included in the system
 *
 * @author Yehia Farag
 */
public class WebTool extends Tool {

    /**
     * The tool id generated in galaxy server.
     */
    private String toolId;
    /**
     * The tool name generated in galaxy server.
     */
    private String toolName;
    /**
     * The tool section in galaxy (generated in galaxy server).
     */
    private String toolSection;
    /**
     * The tool section reference list.
     */
    private final List<String> toolIds = new ArrayList<>();
    /**
     * The tool id reference list.
     */
    private final List<String> sectionIds = new ArrayList<>();

    /**
     * add tool reference
     *
     *
     * @param sectionId section id for the tool container
     * @param toolId tool id in the system
     */
    public void addTool(String sectionId, String toolId) {
        this.sectionIds.add(sectionId);
        this.toolIds.add(toolId);
    }

    /**
     * Get the unique tool id
     *
     * @return String The tool id generated in galaxy server
     */
    public String getToolId() {
        return toolId;
    }

    /**
     *Get list of tool ids
     * @return List<String> available tool ids in system
     */
    public List<String> getToolIds() {
        return toolIds;
    }

    /**
     *Get list of section ids
     * @return List<String> available section ids contain tool
     */
    public List<String> getSectionIds() {
        return sectionIds;
    }

    /**
     * Set the unique tool id
     *
     * @param sectionId
     * @param toolId The tool id generated in galaxy server
     */
    public void setActiveTool(String sectionId, String toolId) {
        this.toolId = toolId;
        this.toolSection = sectionId;
    }

    /**
     * Get the tool name
     *
     * @return String The tool name generated in galaxy server.
     */
    public String getToolName() {
        return toolName;
    }

    /**
     * Set the tool name
     *
     * @param toolName The tool name generated in galaxy server.
     */
    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    /**
     * Get the tool section which contain the tool
     *
     * @return String tool section title
     */
    public String getToolSection() {
        return toolSection;
    }

    /**
     **Set the tool section which contain the tool
     *
     * @param toolSection The tool section title generated in galaxy server.
     */
    public void setToolSection(String toolSection) {
        this.toolSection = toolSection;
    }

}
