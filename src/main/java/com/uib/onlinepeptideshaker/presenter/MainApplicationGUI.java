package com.uib.onlinepeptideshaker.presenter;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.wolfie.refresher.Refresher;
import com.uib.onlinepeptideshaker.managers.VisualizationManager;
import com.uib.onlinepeptideshaker.model.LogicLayer;
import com.uib.onlinepeptideshaker.model.beans.OnlinePeptideShakerHistory;
import com.uib.onlinepeptideshaker.model.beans.PeptideShakerViewBean;
import com.uib.onlinepeptideshaker.presenter.view.ToolsSectionContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * This class represents main GUI container for galaxy web interface (login) and
 * main functions required for SearchGUI and PeptideShaker tools
 *
 * @author Yehia Farag
 */
public class MainApplicationGUI extends HorizontalLayout {

    /**
     * The main view controller (VIEW MANAGER).
     */
    private VisualizationManager VISUALIZATION_MANAGER;
    /**
     * The galaxy server logic layer.
     */
    private final LogicLayer LOGIC_LAYER;
    /**
     * The galaxy server history manger.
     */
    private HistoryManagmentPresenter historyManagmentPresenter;
    /**
     * The galaxy tools presenter.
     */
    private WebToolsPresenter webGalaxyTools;
    /**
     * The galaxy web visualization presenter.
     */
    private WebVisualizationPresenter webVisualization;

    /**
     * Constructor to initialize the main variable.
     */
    public MainApplicationGUI(Refresher REFRESHER) {
        MainApplicationGUI.this.setSizeFull();
        MainApplicationGUI.this.setSpacing(true);
        MainApplicationGUI.this.setMargin(new MarginInfo(false, false, true, true));

        AbsoluteLayout mainViewContainer = new AbsoluteLayout();
        mainViewContainer.setSizeFull();
        MainApplicationGUI.this.addComponent(mainViewContainer);
        MainApplicationGUI.this.setExpandRatio(mainViewContainer, 95);

        AbsoluteLayout rightLayoutContainer = new AbsoluteLayout();
        rightLayoutContainer.setSizeFull();
        rightLayoutContainer.setVisible(true);
        rightLayoutContainer.setStyleName("rightbtnscontainer");
        MainApplicationGUI.this.addComponent(rightLayoutContainer);
        MainApplicationGUI.this.setExpandRatio(rightLayoutContainer, 5);

        VerticalLayout marker = new VerticalLayout();
        marker.setWidth(2, Unit.PIXELS);
        marker.setHeight(80, Unit.PERCENTAGE);
        marker.setStyleName("lightgraylayout");
        rightLayoutContainer.addComponent(marker, "left: 50%; top: 16px;");

        VerticalLayout rightControlBtnsContainer = new VerticalLayout() {
            @Override
            public void setEnabled(boolean enabled) {
                if (enabled) {
                    this.removeStyleName("slowinvisible");
                } else {
                    this.addStyleName("slowinvisible");
                }
            }

        };
        rightControlBtnsContainer.setSizeFull();
        rightControlBtnsContainer.setStyleName("rightbtnscontainer");
        rightControlBtnsContainer.setEnabled(false);
        rightLayoutContainer.addComponent(rightControlBtnsContainer);

        VISUALIZATION_MANAGER = new VisualizationManager(rightControlBtnsContainer, mainViewContainer);
        WelcomePage landedPage = new WelcomePage() {
            @Override
            public void systemConnected(GalaxyInstance galaxyInstant) {
                if (galaxyInstant != null) {
                    rightControlBtnsContainer.setEnabled(true);
                    MainApplicationGUI.this.systemConnected(galaxyInstant);
                } else {
                    rightControlBtnsContainer.setEnabled(false);
                }
            }
        };
        VISUALIZATION_MANAGER.registerView(landedPage);

//       
//
        this.LOGIC_LAYER = new LogicLayer(REFRESHER) {
            @Override
            public void updateHistoryPresenter(OnlinePeptideShakerHistory systemHistory) {
                if (historyManagmentPresenter != null) {
                    historyManagmentPresenter.updateHistoryPanels(systemHistory);
                } else {
                    System.out.println("null hmpresenter");
                }
                if (webGalaxyTools != null) {
                    webGalaxyTools.updateForm();
                }
            }

        };

        WebToolsPresenter toolPresenter = new WebToolsPresenter(LOGIC_LAYER);
        VISUALIZATION_MANAGER.registerView(toolPresenter);
        VISUALIZATION_MANAGER.viewLayout(landedPage.getViewId());
        
        
            WebToolsPresenter test = new WebToolsPresenter(LOGIC_LAYER);
        VISUALIZATION_MANAGER.registerView(test);
        VISUALIZATION_MANAGER.viewLayout(landedPage.getViewId());
        
        
        

//        initalizeBodyPanel();
    }

    /**
     * Initialize the main body panel that has all tools and history contents.
     */
    private HorizontalLayout initalizeBodyPanel() {
        HorizontalLayout bodyPanelLayout = new HorizontalLayout();
        bodyPanelLayout.setSpacing(false);
        bodyPanelLayout.setSizeFull();

        ToolsSectionContainer toolsControlSection = new ToolsSectionContainer();
        bodyPanelLayout.addComponent(toolsControlSection);
        bodyPanelLayout.setExpandRatio(toolsControlSection, 10);

        HorizontalLayout toolViewHistoryContainer = new HorizontalLayout();
        toolViewHistoryContainer.setSizeFull();
        toolViewHistoryContainer.setStyleName("mainviewframe");
        toolViewHistoryContainer.addStyleName("hide");
//        bodyPanelLayout.addComponent(toolViewHistoryContainer);
//        bodyPanelLayout.setExpandRatio(toolViewHistoryContainer, 87);
        toolViewHistoryContainer.setSpacing(true);

        VerticalLayout rightSidePanel = new VerticalLayout();
        rightSidePanel.setWidth(100, Unit.PERCENTAGE);
        rightSidePanel.setHeight(100, Unit.PERCENTAGE);
        rightSidePanel.setStyleName("historyframe");
//        bodyPanelLayout.addComponent(rightSidePanel);
//        bodyPanelLayout.setExpandRatio(rightSidePanel, 3);

//        AbsoluteLayout mainViewSection = new AbsoluteLayout();
//        mainViewSection.setWidth(100, Unit.PERCENTAGE);
//        mainViewSection.setHeight(100, Unit.PERCENTAGE);
//        mainViewSection.setStyleName("slowmove");
//        toolViewHistoryContainer.addComponent(mainViewSection);
//        toolViewHistoryContainer.setExpandRatio(mainViewSection, 0.7f);
//
//        historyManagmentPresenter = new HistoryManagmentPresenter() {
//            @Override
//            public void updateSelectedHistory(String historyId) {
////                LOGIC_LAYER.updateSelectedHistory(historyId);
////                this.updateHistoryPanels(LOGIC_LAYER.getCurrentGalaxyHistory());
//            }
//
//            @Override
//            public void createNewHistory(String historyName) {
////                LOGIC_LAYER.createNewHistory(historyName);
////                this.updateHistoryPanels(LOGIC_LAYER.getCurrentGalaxyHistory());
//            }
//
//            @Override
//            public void deleteHistoryDataset(String historyId, String historyDatasetId) {
////                LOGIC_LAYER.deleteGalaxyHistoryDataseyt(historyDatasetId, historyDatasetId);
////                this.updateHistoryPanels(LOGIC_LAYER.getCurrentGalaxyHistory());
//
//            }
//
//            @Override
//            public void viewPeptideshakerResults(PeptideShakerViewBean results) {
//                VISUALIZATION_MANAGER.viewLayout(webVisualization.getViewId());
//                webVisualization.updateProteinTable(results);
//                
//
//            }
//
//        };
//        toolViewHistoryContainer.addComponent(historyManagmentPresenter.getMainHistoryPanel());
//        toolViewHistoryContainer.setComponentAlignment(historyManagmentPresenter.getMainHistoryPanel(), Alignment.BOTTOM_RIGHT);
//        toolViewHistoryContainer.setExpandRatio(historyManagmentPresenter.getMainHistoryPanel(), 0.3f);
//
//        VerticalLayout topRightLayoutContainer = new VerticalLayout();
//        topRightLayoutContainer.setSizeFull();
//        topRightLayoutContainer.setSpacing(true);
//        topRightLayoutContainer.setStyleName("toprightframe");
//        rightSidePanel.addComponent(topRightLayoutContainer);
//
//        VerticalLayout topBtnContainer = new VerticalLayout();
//        topRightLayoutContainer.addComponent(topBtnContainer);
//        topBtnContainer.setSizeFull();
//        topBtnContainer.setSpacing(true);
//        topBtnContainer.addStyleName("maxheight80");
//        VerticalLayout homeLayoutBtn = new VerticalLayout();
//        homeLayoutBtn.setSizeFull();
//        homeLayoutBtn.setStyleName("homestyle");
//        topBtnContainer.addComponent(homeLayoutBtn);
//
//        VerticalLayout galaxyControlPanelBtn = new VerticalLayout();
//        galaxyControlPanelBtn.setSizeFull();
//        galaxyControlPanelBtn.setStyleName("galaxybtnstyle");
//        galaxyControlPanelBtn.addComponent(galaxyInputPanel.getControlButton());
//        galaxyControlPanelBtn.addLayoutClickListener(galaxyInputPanel);
//
//        topBtnContainer.addComponent(galaxyControlPanelBtn);
//
//        VerticalLayout bottomRightLayoutContainer = new VerticalLayout();
//        bottomRightLayoutContainer.setSizeFull();
//        bottomRightLayoutContainer.setStyleName("bottomrightframe");
//        rightSidePanel.addComponent(bottomRightLayoutContainer);
//
//        UploadDataWebToolPresenter uploadToGalaxyTool = new UploadDataWebToolPresenter();
//        VISUALIZATION_MANAGER.registerView(uploadToGalaxyTool);
//
//        bottomRightLayoutContainer.addComponent(historyManagmentPresenter);
//
//        webGalaxyTools = new WebToolsPresenter(LOGIC_LAYER);
//        VISUALIZATION_MANAGER.registerView(webGalaxyTools);
//       
//
//        webVisualization = new WebVisualizationPresenter(LOGIC_LAYER);
//        VISUALIZATION_MANAGER.registerView(webVisualization); 
//        VISUALIZATION_MANAGER.viewLayout(webVisualization.getViewId());
        return bodyPanelLayout;

    }

    private void systemConnected(GalaxyInstance galaxyInstant) {
//       bodyPanel.addComponent(initalizeBodyPanel());
        LOGIC_LAYER.connectToGalaxyServer(galaxyInstant);
        LOGIC_LAYER.loadGalaxyHistory(null);

    }

}
