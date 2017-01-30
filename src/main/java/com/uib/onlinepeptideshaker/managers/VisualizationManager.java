package com.uib.onlinepeptideshaker.managers;

import com.vaadin.event.LayoutEvents;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents the view controller, the controller is responsible for
 * handling the requested layout and viewing it.
 *
 * @author Yehia Farag
 *
 */
public class VisualizationManager implements LayoutEvents.LayoutClickListener{

    private final Map<String, RegistrableView> visualizationMap = new LinkedHashMap<>();

    @Override
    public void layoutClick(LayoutEvents.LayoutClickEvent event) {
        if(event.getComponent().getParent() instanceof RegistrableView)
            this.viewLayout(((RegistrableView)event.getComponent().getParent()).getViewId());
    }

    

    /**
     * Register view into the view management system.
     *
     * @param view visualization layout.
     */
    public void registerView(RegistrableView view) {
        view.getMinimizeComponent().addLayoutClickListener(VisualizationManager.this);
        visualizationMap.put(view.getViewId(), view);
    }

    /**
     * View only selected view and hide the rest of registered layout
     *
     * @param viewId selected view id
     */
    public void viewLayout(String viewId) {
        for (RegistrableView view : visualizationMap.values()) {
            view.minimizeView();
        }
        visualizationMap.get(viewId).maximizeView();
    }

}
