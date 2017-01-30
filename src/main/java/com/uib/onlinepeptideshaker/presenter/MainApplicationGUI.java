package com.uib.onlinepeptideshaker.presenter;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.beans.ToolSection;
import com.uib.onlinepeptideshaker.managers.VisualizationManager;
import com.uib.onlinepeptideshaker.view.GalaxyConnectionPanel;
import com.uib.onlinepeptideshaker.view.ToolsSectionContainer;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.MarginInfo;
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
    private final VisualizationManager VISUALIZATION_MANAGER;
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
     * Constructor to initialize the main variable.
     */
    public MainApplicationGUI() {
        MainApplicationGUI.this.setSizeFull();
        MainApplicationGUI.this.setSpacing(true);
        MainApplicationGUI.this.setMargin(new MarginInfo(false, true, true, true));

        VISUALIZATION_MANAGER = new VisualizationManager();

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
        VISUALIZATION_MANAGER.registerView(galaxyInputPanel);
        bodyPanel.addComponent(galaxyInputPanel);
        bodyPanel.setComponentAlignment(galaxyInputPanel, Alignment.TOP_CENTER);

        MainApplicationGUI.this.addComponent(bodyPanel);
        MainApplicationGUI.this.setComponentAlignment(bodyPanel, Alignment.TOP_CENTER);
        MainApplicationGUI.this.setExpandRatio(bodyPanel, 70);
        MainApplicationGUI.this.setStyleName("minheight400");
        MainApplicationGUI.this.addStyleName("minwidth500");

    }

    /**
     * Initialize the header layout.
     */
    private HorizontalLayout initializeHeaderPanel() {
        HorizontalLayout headerLayoutContainer = new HorizontalLayout();
        Image peptideShakerLogoIcon = new Image();
        peptideShakerLogoIcon.setSource(new ThemeResource("favicon.ico"));
        peptideShakerLogoIcon.setHeight(40, Unit.PIXELS);
        peptideShakerLogoIcon.setStyleName("galaxyicon");
        headerLayoutContainer.addComponent(peptideShakerLogoIcon);
        headerLayoutContainer.setComponentAlignment(peptideShakerLogoIcon, Alignment.MIDDLE_LEFT);

        Link headerLogoLabel = new Link("Peptide Shaker <font>(Online Version)</font>", new ExternalResource(""));
        headerLayoutContainer.addComponent(headerLogoLabel);
        headerLogoLabel.setCaptionAsHtml(true);
        headerLayoutContainer.setComponentAlignment(headerLogoLabel, Alignment.MIDDLE_LEFT);
        headerLogoLabel.setStyleName("headerlogo");

        return headerLayoutContainer;
    }

    private HorizontalLayout initalizeBodyPanel() {
        HorizontalLayout bodyPanelLayout = new HorizontalLayout();
        bodyPanelLayout.setSpacing(true);
        bodyPanelLayout.setSizeFull();
//        bodyPanelLayout.setMargin(new MarginInfo(true, true, true, true));
        
        ToolsSectionContainer toolsControlSection = new ToolsSectionContainer();
        bodyPanelLayout.addComponent(toolsControlSection);
       bodyPanelLayout.setExpandRatio(toolsControlSection, 10);
        
           VerticalLayout mainViewSection = new VerticalLayout();
        mainViewSection.setWidth(100, Unit.PERCENTAGE);
        mainViewSection.setHeight(100, Unit.PERCENTAGE);
        mainViewSection.setStyleName("frame");
        bodyPanelLayout.addComponent(mainViewSection);
        bodyPanelLayout.setExpandRatio(mainViewSection, 85);
        
          VerticalLayout historySection = new VerticalLayout();
           historySection.setWidth(100, Unit.PERCENTAGE);
        historySection.setHeight(100, Unit.PERCENTAGE);
        historySection.setStyleName("frame");
        bodyPanelLayout.addComponent(historySection);
        bodyPanelLayout.setExpandRatio(historySection, 5);
        
        return bodyPanelLayout;
    }

    private void systemConnected(GalaxyInstance galaxyInstant) {
        galaxyInputPanel.minimizeView();
        headerPanelLayout.setHeight(40, Unit.PIXELS);
        if (headerPanelLayout.getComponentCount() < 3) {
            bodyPanel.removeAllComponents();
            headerPanelLayout.addComponent(galaxyInputPanel);
            headerPanelLayout.setComponentAlignment(galaxyInputPanel, Alignment.MIDDLE_LEFT);
            bodyPanel.addComponent(initalizeBodyPanel());
        }

    }

}
