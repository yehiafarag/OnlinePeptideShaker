package com.uib.onlinepeptideshaker.managers;

import com.uib.onlinepeptideshaker.presenter.HistoryManagmentPresenter;
import com.vaadin.event.LayoutEvents;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
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
    private final AbsoluteLayout mainToolViewContainer;
    private final VerticalLayout sideButtonContainer;

    /**
     * Constructor to initialize the main attributes.
     *
     * @param mainViewContainer the main view panel (middle panel )container.
     * @param sideButtonContainer the main side button (left panel) container.
     */
    public VisualizationManager(VerticalLayout sideButtonContainer, AbsoluteLayout mainToolViewContainer) {
        this.sideButtonContainer = sideButtonContainer;
        this.mainToolViewContainer = mainToolViewContainer;
//        this.historyManagmentPresenter = historyManagmentPresenter;
//        historyManagmentPresenter.addLayoutClickListener((LayoutEvents.LayoutClickEvent event) -> {
//            if (historyManagmentPresenter.getMainHistoryPanel().getStyleName().contains("hidepanel")) {
//                historyManagmentPresenter.getMainHistoryPanel().removeStyleName("hidepanel");
//                mainToolViewContainer.removeStyleName("fullsize");
//            } else {
//                historyManagmentPresenter.getMainHistoryPanel().addStyleName("hidepanel");
//                mainToolViewContainer.addStyleName("fullsize");
//            }
//        });
//
//        historyManagmentPresenter.getMainHistoryPanel().addStyleName("hidepanel");
//        mainToolViewContainer.addStyleName("fullsize");

    }

    @Override
    public void layoutClick(LayoutEvents.LayoutClickEvent event) {
       
//        if (event.getComponent().getParent() instanceof RegistrableView) {
        this.viewLayout(((AbsoluteLayout) event.getComponent()).getData().toString());
//        }
    }

    /**
     * Register view into the view management system.
     *
     * @param view visualization layout.
     */
    public void registerView(RegistrableView view) {
        view.getControlButton().addLayoutClickListener(VisualizationManager.this);
        visualizationMap.put(view.getViewId(), view);
        sideButtonContainer.addComponent(view.getControlButton());
         sideButtonContainer.setComponentAlignment(view.getControlButton(),Alignment.TOP_CENTER);
        mainToolViewContainer.addComponent(view.getMainViewComponent());
    }

    /**
     * View only selected view and hide the rest of registered layout
     *
     * @param viewId selected view id
     */
    public void viewLayout(String viewId) {
//         historyManagmentPresenter.getMainHistoryPanel().addStyleName("hidepanel");
//        mainToolViewContainer.addStyleName("fullsize");
        for (RegistrableView view : visualizationMap.values()) {
            view.minimizeView();
        }
        visualizationMap.get(viewId).maximizeView();

    }

}
