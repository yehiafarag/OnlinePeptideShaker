package com.uib.onlinepeptideshaker.managers;

import com.vaadin.event.LayoutEvents;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;

/**
 * Interface for different Vaadin layout that allow the VisualizationManager
 * control the current view.
 *
 * @author Yehia Farag
 */
public interface RegistrableView  {

    /**
     * Get visualization unique id
     *
     * @return string view_id
     */
    public abstract String getViewId();

    /**
     * Minimize the current view.
     */
    public abstract void minimizeView();

    /**
     * Maximize the current view.
     */
    public abstract void maximizeView();
    
    /**
     * Get minimized layout (icon represents the layout).
     */
    public abstract VerticalLayout getMinimizeComponent();

}
