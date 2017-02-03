package com.uib.onlinepeptideshaker.managers;

import com.vaadin.event.LayoutEvents;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.VerticalLayout;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class represents the view controller, the controller is responsible for
 * handling the requested layout and viewing it.
 *
 * @author Yehia Farag
 *
 */
public class VisualizationManager implements LayoutEvents.LayoutClickListener {

    private final Map<String, RegistrableView> visualizationMap = new LinkedHashMap<>();
    private AbsoluteLayout mainViewContainer;
    private VerticalLayout sideButtonContainer;

    /**
     * Constructor to initialize the main attributes.
     *
     * @param mainViewContainer the main view panel (middle panel )container.
     * @param sideButtonContainer the main side button (left panel) container.
     */
    public VisualizationManager(AbsoluteLayout mainViewContainer,VerticalLayout sideButtonContainer) {
        this.mainViewContainer = mainViewContainer;    
        this.sideButtonContainer=sideButtonContainer;
//        this.mainViewContainer.addStyleName("topleftroundedborder");
    }

    @Override
    public void layoutClick(LayoutEvents.LayoutClickEvent event) {
//        if (event.getComponent().getParent() instanceof RegistrableView) {
            this.viewLayout(((AbsoluteLayout)event.getComponent()).getData().toString());
//        }
    }

    /**
     * Register view into the view management system.
     *
     * @param view visualization layout.
     */
    public void registerView(RegistrableView view) {
        view.getMinimizeComponent().addLayoutClickListener(VisualizationManager.this);
        visualizationMap.put(view.getViewId(), view);
        sideButtonContainer.addComponent(view.getMinimizeComponent());
        mainViewContainer.addComponent(view.getMainViewComponent());
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
        System.err.println("at max "+ viewId);
        visualizationMap.get(viewId).maximizeView();
        if (visualizationMap.keySet().toArray()[0].toString().equalsIgnoreCase(viewId)) {
            System.err.println("at first button selected");
//            mainViewContainer.removeStyleName("topleftroundedborder");
        }else{
//        mainViewContainer.addStyleName("topleftroundedborder");
        }
    }

}
