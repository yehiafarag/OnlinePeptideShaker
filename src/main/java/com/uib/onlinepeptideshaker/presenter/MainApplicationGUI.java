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

/**
 * This class represents main GUI container for galaxy web interface (login) and
 * main functions required for SearchGUI and PeptideShaker tools
 *
 * @author Yehia Farag
 */
public class MainApplicationGUI extends VerticalLayout {

    /**
     * The main view controller (VIEW MANAGER).
     */
    private VisualizationManager VISUALIZATION_MANAGER;
    /**
     * The header layout container.
     */
    private final HorizontalLayout headerPanelLayout;
    /**
     * The body layout panel.
     */
    private final VerticalLayout bodyPanel;
    /**
     * The header layout panel.
     */
    private final VerticalLayout headerPanel;
    /**
     * The galaxy server connection panel.
     */
    private final GalaxyConnectionPanel galaxyInputPanel;
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
        MainApplicationGUI.this.setMargin(new MarginInfo(false, true, true, true));

        headerPanel = new VerticalLayout();
        headerPanel.setHeight(50, Unit.PIXELS);
        MainApplicationGUI.this.addComponent(headerPanel);
        MainApplicationGUI.this.setComponentAlignment(headerPanel, Alignment.MIDDLE_LEFT);
        MainApplicationGUI.this.setExpandRatio(headerPanel, 10);
        headerPanelLayout = initializeHeaderPanel();
        headerPanel.addComponent(headerPanelLayout);
        headerPanel.setMargin(new MarginInfo(false, false, false, false));

        bodyPanel = new VerticalLayout();
        bodyPanel.setSizeFull();
        galaxyInputPanel = new GalaxyConnectionPanel() {
            @Override
            public void connectedToGalaxy(GalaxyInstance galaxyInstant) {
                systemConnected(galaxyInstant);
            }

        };

        MainApplicationGUI.this.addComponent(bodyPanel);
        MainApplicationGUI.this.setComponentAlignment(bodyPanel, Alignment.TOP_CENTER);
        MainApplicationGUI.this.setExpandRatio(bodyPanel, 70);
        MainApplicationGUI.this.setStyleName("minheight275");
        MainApplicationGUI.this.addStyleName("minwidth435");

        this.LOGIC_LAYER = new LogicLayer(REFRESHER) {
            @Override
            public void updateHistoryPresenter(OnlinePeptideShakerHistory systemHistory) {
                if (historyManagmentPresenter != null) {
                    historyManagmentPresenter.updateHistoryPanels(systemHistory);
                }else
                    System.out.println("null hmpresenter");
                if (webGalaxyTools != null) {
                    webGalaxyTools.updateForm();
                }
            }

        };


    }

    /**
     * Initialize the header layout.
     */
    private HorizontalLayout initializeHeaderPanel() {
        HorizontalLayout headerLayoutContainer = new HorizontalLayout();
        headerLayoutContainer.setSpacing(true);
        Image peptideShakerLogoIcon = new Image();
        peptideShakerLogoIcon.setSource(new ThemeResource("favicon.ico"));
        peptideShakerLogoIcon.setHeight(40, Unit.PIXELS);
        peptideShakerLogoIcon.setStyleName("galaxyicon");
        headerLayoutContainer.addComponent(peptideShakerLogoIcon);
        headerLayoutContainer.setComponentAlignment(peptideShakerLogoIcon, Alignment.MIDDLE_LEFT);

        Link headerLogoLabel = new Link("PeptideShaker <font>(Online Version)</font>", new ExternalResource(""));
        headerLayoutContainer.addComponent(headerLogoLabel);
        headerLogoLabel.setCaptionAsHtml(true);
        headerLayoutContainer.setComponentAlignment(headerLogoLabel, Alignment.MIDDLE_LEFT);
        headerLogoLabel.setStyleName("headerlogo");

        return headerLayoutContainer;
    }

    /**
     * Initialize the main body panel that has all tools and history contents
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
        bodyPanelLayout.addComponent(toolViewHistoryContainer);
        bodyPanelLayout.setExpandRatio(toolViewHistoryContainer, 87);
        toolViewHistoryContainer.setSpacing(true);

        VerticalLayout historySection = new VerticalLayout();
        historySection.setWidth(100, Unit.PERCENTAGE);
        historySection.setHeight(100, Unit.PERCENTAGE);
        historySection.setStyleName("historyframe");
        bodyPanelLayout.addComponent(historySection);
        bodyPanelLayout.setExpandRatio(historySection, 3);

        AbsoluteLayout mainViewSection = new AbsoluteLayout();
        mainViewSection.setWidth(100, Unit.PERCENTAGE);
        mainViewSection.setHeight(100, Unit.PERCENTAGE);
        mainViewSection.setStyleName("slowmove");
        toolViewHistoryContainer.addComponent(mainViewSection);
        toolViewHistoryContainer.setExpandRatio(mainViewSection, 0.7f);

        historyManagmentPresenter = new HistoryManagmentPresenter() {
            @Override
            public void updateSelectedHistory(String historyId) {
//                LOGIC_LAYER.updateSelectedHistory(historyId);
//                this.updateHistoryPanels(LOGIC_LAYER.getCurrentGalaxyHistory());
            }

            @Override
            public void createNewHistory(String historyName) {
//                LOGIC_LAYER.createNewHistory(historyName);
//                this.updateHistoryPanels(LOGIC_LAYER.getCurrentGalaxyHistory());
            }

            @Override
            public void deleteHistoryDataset(String historyId, String historyDatasetId) {
//                LOGIC_LAYER.deleteGalaxyHistoryDataseyt(historyDatasetId, historyDatasetId);
//                this.updateHistoryPanels(LOGIC_LAYER.getCurrentGalaxyHistory());

            }

            @Override
            public void viewPeptideshakerResults(PeptideShakerViewBean results) {
                VISUALIZATION_MANAGER.viewLayout(webVisualization.getViewId());
                webVisualization.updateProteinTable(results);
                

            }

        };
        toolViewHistoryContainer.addComponent(historyManagmentPresenter.getMainHistoryPanel());
        toolViewHistoryContainer.setComponentAlignment(historyManagmentPresenter.getMainHistoryPanel(), Alignment.BOTTOM_RIGHT);
        toolViewHistoryContainer.setExpandRatio(historyManagmentPresenter.getMainHistoryPanel(), 0.3f);

        VISUALIZATION_MANAGER = new VisualizationManager(toolsControlSection, mainViewSection, historyManagmentPresenter);

        VerticalLayout topRightLayoutContainer = new VerticalLayout();
        topRightLayoutContainer.setSizeFull();
        topRightLayoutContainer.setSpacing(true);
        topRightLayoutContainer.setStyleName("toprightframe");
        historySection.addComponent(topRightLayoutContainer);

        VerticalLayout topBtnContainer = new VerticalLayout();
        topRightLayoutContainer.addComponent(topBtnContainer);
        topBtnContainer.setSizeFull();
        topBtnContainer.setSpacing(true);
        topBtnContainer.addStyleName("maxheight80");
        VerticalLayout homeLayoutBtn = new VerticalLayout();
        homeLayoutBtn.setSizeFull();
        homeLayoutBtn.setStyleName("homestyle");
        topBtnContainer.addComponent(homeLayoutBtn);

        VerticalLayout galaxyControlPanelBtn = new VerticalLayout();
        galaxyControlPanelBtn.setSizeFull();
        galaxyControlPanelBtn.setStyleName("galaxybtnstyle");
        galaxyControlPanelBtn.addComponent(galaxyInputPanel.getMinimizeComponent());
        galaxyControlPanelBtn.addLayoutClickListener(galaxyInputPanel);

        topBtnContainer.addComponent(galaxyControlPanelBtn);

        VerticalLayout bottomRightLayoutContainer = new VerticalLayout();
        bottomRightLayoutContainer.setSizeFull();
        bottomRightLayoutContainer.setStyleName("bottomrightframe");
        historySection.addComponent(bottomRightLayoutContainer);

        UploadDataWebToolPresenter uploadToGalaxyTool = new UploadDataWebToolPresenter();
        VISUALIZATION_MANAGER.registerView(uploadToGalaxyTool);

        bottomRightLayoutContainer.addComponent(historyManagmentPresenter);

        webGalaxyTools = new WebToolsPresenter(LOGIC_LAYER);
        VISUALIZATION_MANAGER.registerView(webGalaxyTools);
       

        webVisualization = new WebVisualizationPresenter(LOGIC_LAYER);
        VISUALIZATION_MANAGER.registerView(webVisualization); 
        VISUALIZATION_MANAGER.viewLayout(webVisualization.getViewId());
        return bodyPanelLayout;

    }

    private void systemConnected(GalaxyInstance galaxyInstant) {
        galaxyInputPanel.minimizeView();
        headerPanel.setVisible(false);
        bodyPanel.addStyleName("margintop10");
        headerPanelLayout.setHeight(40, Unit.PIXELS);
        LOGIC_LAYER.connectToGalaxyServer(galaxyInstant);
        bodyPanel.removeAllComponents();
        bodyPanel.addComponent(initalizeBodyPanel());
        LOGIC_LAYER.loadGalaxyHistory(null);
//        historyManagmentPresenter.updateHistoryPanels(LOGIC_LAYER.g());

    }
    
 

}
