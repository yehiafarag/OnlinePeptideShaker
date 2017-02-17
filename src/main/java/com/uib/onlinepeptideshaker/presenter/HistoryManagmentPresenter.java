package com.uib.onlinepeptideshaker.presenter;

import com.uib.onlinepeptideshaker.model.beans.GalaxyHistory;
import com.uib.onlinepeptideshaker.presenter.view.ClickableLabel;
import com.vaadin.data.Property;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * This class represents the user galaxy history that has all the user datasets
 *
 * @author Yehia Farag
 */
public abstract class HistoryManagmentPresenter extends VerticalLayout implements Property.ValueChangeListener {

    /**
     * Main history panel that has all history contents.
     */
    private final Panel mainHistoryPanel;
    /**
     * Main history panel container.
     */
    private final VerticalLayout mainHistoryPanelContainer;
    /**
     * Galaxy server web address.
     */
    private ComboBox userGalaxyHistories;
    /**
     * Main history panel that has all history contents.
     */
    private VerticalLayout generalHistoryDatasetPanelContent;
    /**
     * Main history panel that has all history contents.
     */
    private VerticalLayout searchGIUHistoryDatasetPanelContent;
    /**
     * Main history panel that has all history contents.
     */
    private VerticalLayout peptideShakerHistoryDatasetPanelContent;
    /**
     * Main history panel that has all history contents.
     */
    private VerticalLayout peptideShakerHistoryDatasetResultsPanelContent;
    /**
     * Galaxy History bean.
     */
    private GalaxyHistory currentGalaxyHistory;

    /**
     * Constructor to initialize the main attributes.
     */
    public HistoryManagmentPresenter() {
        HistoryManagmentPresenter.this.setSizeFull();
        HistoryManagmentPresenter.this.setStyleName("historypopuplabel");
        HistoryManagmentPresenter.this.addStyleName("blink");

        VerticalLayout historyContentLayout = new VerticalLayout();
        historyContentLayout.setWidth(500, Unit.PIXELS);
        historyContentLayout.setHeight(80, Unit.PERCENTAGE);

        mainHistoryPanelContainer = new VerticalLayout();
        mainHistoryPanelContainer.setStyleName("rightmovablepanel");
        mainHistoryPanelContainer.setSizeFull();

        mainHistoryPanel = new Panel();
        mainHistoryPanel.setWidth(100, Unit.PERCENTAGE);
        mainHistoryPanel.setHeight(100, Unit.PERCENTAGE);

        mainHistoryPanel.addStyleName(ValoTheme.PANEL_BORDERLESS);
        mainHistoryPanelContainer.addComponent(mainHistoryPanel);
        HistoryManagmentPresenter.this.initHistoryPanel();;

    }

    /**
     * Initialize history panel layout.
     */
    private void initHistoryPanel() {
        VerticalLayout mainHistoryPanelContent = new VerticalLayout();
        mainHistoryPanelContent.setSizeFull();
        mainHistoryPanel.setContent(mainHistoryPanelContent);
        userGalaxyHistories = new ComboBox();
        userGalaxyHistories.setSizeFull();
        userGalaxyHistories.setInputPrompt("History in use");
        userGalaxyHistories.setDescription("History in use");
        userGalaxyHistories.setNullSelectionAllowed(false);
        userGalaxyHistories.setStyleName("standredcombobox");
        userGalaxyHistories.setTextInputAllowed(true);
        userGalaxyHistories.setNewItemsAllowed(true);
        userGalaxyHistories.setNewItemHandler((final String newItemCaption) -> {
            // Adds new option

            if (!currentGalaxyHistory.getAvailableGalaxyHistoriesMap().containsValue(newItemCaption) && userGalaxyHistories.addItem(newItemCaption) != null) {
                userGalaxyHistories.setValue(newItemCaption);
            }

        });
        mainHistoryPanelContent.addComponent(userGalaxyHistories);
        mainHistoryPanelContent.setExpandRatio(userGalaxyHistories, 4);

        generalHistoryDatasetPanelContent = new VerticalLayout();
        Panel generalHistoryDatasetPanel = new Panel(generalHistoryDatasetPanelContent);
        generalHistoryDatasetPanel.setSizeFull();
        generalHistoryDatasetPanel.setStyleName(ValoTheme.PANEL_WELL);
        mainHistoryPanelContent.addComponent(generalHistoryDatasetPanel);
        mainHistoryPanelContent.setExpandRatio(generalHistoryDatasetPanel, 24);

        searchGIUHistoryDatasetPanelContent = new VerticalLayout();
        Panel searchGIUHistoryDatasetPanel = new Panel("SearchGUI input", searchGIUHistoryDatasetPanelContent);
        searchGIUHistoryDatasetPanel.setSizeFull();
        searchGIUHistoryDatasetPanel.setStyleName(ValoTheme.PANEL_WELL);
        mainHistoryPanelContent.addComponent(searchGIUHistoryDatasetPanel);
        mainHistoryPanelContent.setExpandRatio(searchGIUHistoryDatasetPanel, 24);

        peptideShakerHistoryDatasetPanelContent = new VerticalLayout();
        Panel peptideShakerHistoryDatasetPanel = new Panel("PeptideShaker input", peptideShakerHistoryDatasetPanelContent);
        peptideShakerHistoryDatasetPanel.setSizeFull();
        peptideShakerHistoryDatasetPanel.setStyleName(ValoTheme.PANEL_WELL);
        mainHistoryPanelContent.addComponent(peptideShakerHistoryDatasetPanel);
        mainHistoryPanelContent.setExpandRatio(peptideShakerHistoryDatasetPanel, 24);

        peptideShakerHistoryDatasetResultsPanelContent = new VerticalLayout();
        Panel peptideShakerHistoryDatasetResultsPanel = new Panel("PeptideShaker results", peptideShakerHistoryDatasetResultsPanelContent);
        peptideShakerHistoryDatasetResultsPanel.setSizeFull();
        peptideShakerHistoryDatasetResultsPanel.setStyleName(ValoTheme.PANEL_WELL);
        mainHistoryPanelContent.addComponent(peptideShakerHistoryDatasetResultsPanel);
        mainHistoryPanelContent.setExpandRatio(peptideShakerHistoryDatasetResultsPanel, 24);

        VerticalLayout bottomborder = new VerticalLayout();
        bottomborder.setSizeFull();
        mainHistoryPanelContent.addComponent(bottomborder);
        mainHistoryPanelContent.setExpandRatio(bottomborder, 0.4f);

    }

    /**
     * Update the user galaxy history interface with the updated data from
     * galaxy server
     *
     * @param galaxyHistory galaxy history bean that has all required
     * information from the server
     */
    public void updateHistoryPanels(GalaxyHistory galaxyHistory) {
        this.currentGalaxyHistory = galaxyHistory;
        userGalaxyHistories.removeValueChangeListener(HistoryManagmentPresenter.this);
        userGalaxyHistories.removeAllItems();
        for (String historyId : galaxyHistory.getAvailableGalaxyHistoriesMap().keySet()) {
            userGalaxyHistories.addItem(historyId);
            userGalaxyHistories.setItemCaption(historyId, galaxyHistory.getAvailableGalaxyHistoriesMap().get(historyId));
        }
        userGalaxyHistories.setValue(galaxyHistory.getUsedHistoryId());
        userGalaxyHistories.commit();
        userGalaxyHistories.addValueChangeListener(HistoryManagmentPresenter.this);

        generalHistoryDatasetPanelContent.removeAllComponents();
        searchGIUHistoryDatasetPanelContent.removeAllComponents();
        peptideShakerHistoryDatasetPanelContent.removeAllComponents();
        peptideShakerHistoryDatasetResultsPanelContent.removeAllComponents();
        for (String key : galaxyHistory.getHistoryDatasetsMap().keySet()) {
            if (galaxyHistory.getHistoryDatasetsMap().get(key).getState() == null) {
                continue;
            }
            ClickableLabel label = new ClickableLabel(galaxyHistory.getHistoryDatasetsMap().get(key).getName(), galaxyHistory.getHistoryDatasetsMap().get(key).getUrl(), galaxyHistory.getHistoryDatasetsMap().get(key).getId()) {
                @Override
                public void performAction(String id) {
                    deleteHistoryDataset(currentGalaxyHistory.getUsedHistoryId(), id);
                }

            };
            label.setData(key);
            label.setState(!(galaxyHistory.getHistoryDatasetsMap().get(key).getState() + "").replace("new", "ok").equals("ok"));

            String type = galaxyHistory.getHistoryDatasetsMap().get(key).getHistoryContentType();
            if (type.equalsIgnoreCase("fasta") || type.equalsIgnoreCase("mgf")) {
                searchGIUHistoryDatasetPanelContent.addComponent(label);
            } else if (type.equalsIgnoreCase("searchgui_archive")) {
                peptideShakerHistoryDatasetPanelContent.addComponent(label);
            } else if (type.equalsIgnoreCase("peptideshaker_archive") || type.equalsIgnoreCase("mzid")) {
               
            } else {
                generalHistoryDatasetPanelContent.addComponent(label);
            }
        }
        for (String jobId : galaxyHistory.getPeptideShakerVisualizationMap().keySet()) {
            ClickableLabel label = new ClickableLabel("view data",null,jobId) {
                @Override
                public void performAction(String id) {
                    viewPeptideshakerResults(id);
                    }
            }; 
            peptideShakerHistoryDatasetResultsPanelContent.addComponent(label);
        }

        //create clickable label for view peptideShaker
        this.blink();
    }

    private void blink() {
        if (HistoryManagmentPresenter.this.getStyleName().contains("blinkII")) {
            HistoryManagmentPresenter.this.removeStyleName("blinkII");
            HistoryManagmentPresenter.this.addStyleName("blink");
        } else {
            HistoryManagmentPresenter.this.removeStyleName("blink");
            HistoryManagmentPresenter.this.addStyleName("blinkII");
        }
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {
        if (this.currentGalaxyHistory.getAvailableGalaxyHistoriesMap().containsKey(userGalaxyHistories.getValue().toString())) {
            updateSelectedHistory(userGalaxyHistories.getValue().toString());
        } else {
            createNewHistory(userGalaxyHistories.getValue().toString());
        }
    }

    /**
     * Get the main history panel.
     *
     * @return Panel Main history panel that has all history contents.
     */
    public VerticalLayout getMainHistoryPanel() {
        return mainHistoryPanelContainer;
    }

    /**
     * Update the selected history in galaxy.
     *
     * @param historyId selected history id could be from exist history or new.
     */
    public abstract void updateSelectedHistory(String historyId);

    /**
     * Create new history in galaxy.
     *
     * @param historyName new history name.
     */
    public abstract void createNewHistory(String historyName);

    /**
     * Delete history dataset in galaxy.
     *
     * @param datasetid history dataset id.
     */
    public abstract void deleteHistoryDataset(String historyId, String historyDatasetId);
    
    /**
     * View peptideShakerResults 
     *
     * @param jobId job id.
     */
    public abstract void viewPeptideshakerResults(String jobId);

}
