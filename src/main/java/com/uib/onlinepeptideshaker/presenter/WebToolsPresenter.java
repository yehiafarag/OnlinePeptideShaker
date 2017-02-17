package com.uib.onlinepeptideshaker.presenter;

import com.uib.onlinepeptideshaker.managers.RegistrableView;
import com.uib.onlinepeptideshaker.model.LogicLayer;
import com.uib.onlinepeptideshaker.model.beans.WebTool;
import com.uib.onlinepeptideshaker.presenter.view.PeptideShakerInputForm;
import com.uib.onlinepeptideshaker.presenter.view.SearchGuiInputForm;
import com.uib.onlinepeptideshaker.presenter.view.WorkFlowForm;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class represent web tool presenter which is responsible for managing the
 * view and interactivity of the tool
 *
 * @author Yehia Farag
 */
public class WebToolsPresenter implements RegistrableView, LayoutEvents.LayoutClickListener {

    /**
     * Main view layout extender.
     */
    private final VerticalLayout mainViewPanel;
    /**
     * Side button container.
     */
    private final AbsoluteLayout sideButton;
    /**
     * Side button extender.
     */
    private final VerticalLayout extender;
    /**
     * SearchGUI web tool.
     */
    private final WebTool searchGUITool;
    /**
     * PeptideShaker web tool.
     */
    private final WebTool peptideShakerTool;
    /**
     * Initialize web tool.
     */
    private boolean initSearchGUITool = true;
    /**
     * Top layout (tools button container).
     */
    private HorizontalLayout topPanel;
    /**
     * Last selected top button.
     */
    private Component lastSelectedBtn;
    /**
     * Work flow input layout.
     */
    private WorkFlowForm workFlowForm;
    /**
     * SearchGUI input layout.
     */
    private SearchGuiInputForm searchGUIForm;
    /**
     * SearchGUI input container panel.
     */
    private Panel searchGUIInpuPanel;
     /**
     * SearchGUI input layout.
     */
    private PeptideShakerInputForm peptideShakerForm;
    /**
     * SearchGUI input container panel.
     */
    private Panel peptideShakerInpuPanel;
    /**
     * Welcome page for tools.
     */
    private VerticalLayout welcomePage;
    /**
     * The galaxy server logic layer.
     */
    private final LogicLayer LOGIC_LAYER;

    /**
     * Initialize the web tool main attributes
     *
     * @param searchGUITool SearchGUI web tool
     */
    public WebToolsPresenter(LogicLayer LOGIC_LAYER) {
        this.searchGUITool = LOGIC_LAYER.getSearch_GUI_Tool();
        this.peptideShakerTool = LOGIC_LAYER.getPeptide_Shaker_Tool();
        this.LOGIC_LAYER = LOGIC_LAYER;

        this.mainViewPanel = new VerticalLayout();
        initializeMainViewPanel();
        this.sideButton = new AbsoluteLayout();
        this.sideButton.setSizeFull();
        this.sideButton.setStyleName("frame");
        this.sideButton.addStyleName("sidebutton");

        extender = new VerticalLayout();
        this.sideButton.addComponent(extender);
        extender.setSizeFull();
        extender.addStyleName("sidebuttonframe");
        Image icon = new Image();
        icon.setSource(new ThemeResource("img/peptideshaker.png"));
        icon.setWidth(100, Unit.PERCENTAGE);
        extender.addComponent(icon);
        extender.setComponentAlignment(icon, Alignment.MIDDLE_CENTER);
        extender.setExpandRatio(icon, 8);
        this.sideButton.setData(WebToolsPresenter.this.getViewId());
        icon.setData(WebToolsPresenter.this.getViewId());

    }

    /**
     * Initialize the web tool main view panel that has the main forms and view.
     * components
     */
    private void initializeMainViewPanel() {
        this.mainViewPanel.setSizeFull();
        this.mainViewPanel.setStyleName("toolviewframe");
        this.mainViewPanel.setSpacing(true);

        topPanel = new HorizontalLayout();
        topPanel.setSizeFull();
        topPanel.setStyleName("framedpanel");
        this.mainViewPanel.addComponent(topPanel);
        this.mainViewPanel.setExpandRatio(topPanel, 10);

        VerticalLayout startWorkFlowIcon = new VerticalLayout();
        startWorkFlowIcon.setSizeFull();
        startWorkFlowIcon.setStyleName("startworkflow");
        if (this.peptideShakerTool != null && this.searchGUITool != null) {
            startWorkFlowIcon.setData(1);
        } else {
            startWorkFlowIcon.setEnabled(false);
            startWorkFlowIcon.addStyleName("deactivatepermanent");
        }

        topPanel.addComponent(startWorkFlowIcon);

        VerticalLayout searchGUIIcon = new VerticalLayout();
        searchGUIIcon.setSizeFull();
        searchGUIIcon.setStyleName("searchguiicon");
        if (this.searchGUITool != null) {
            searchGUIIcon.setData(2);
        } else {
            searchGUIIcon.setEnabled(false);
            searchGUIIcon.addStyleName("deactivatepermanent");
        }

        topPanel.addComponent(searchGUIIcon);

        VerticalLayout peptideShakerIcon = new VerticalLayout();
        peptideShakerIcon.setSizeFull();
        peptideShakerIcon.setStyleName("peptideshakericon");
        if (this.peptideShakerTool != null) {
            peptideShakerIcon.setData(3);
        } else {
            peptideShakerIcon.setEnabled(false);
            peptideShakerIcon.addStyleName("deactivatepermanent");
        }
        topPanel.addComponent(peptideShakerIcon);
        topPanel.addLayoutClickListener(WebToolsPresenter.this);

        AbsoluteLayout inputPanel = new AbsoluteLayout();
        inputPanel.setSizeFull();
        inputPanel.setStyleName("framedpanel");
        this.mainViewPanel.addComponent(inputPanel);
        this.mainViewPanel.setExpandRatio(inputPanel, 90);

        welcomePage = new VerticalLayout();
        welcomePage.setSizeFull();
        Label welcomeLabel = new Label("Please select from available tools");
        welcomeLabel.setWidth(50, Unit.PERCENTAGE);
        welcomeLabel.setHeight(50, Unit.PERCENTAGE);
        welcomeLabel.setStyleName(ValoTheme.LABEL_HUGE);
        welcomePage.addComponent(welcomeLabel);
        welcomePage.setComponentAlignment(welcomeLabel, Alignment.MIDDLE_CENTER);

        inputPanel.addComponent(welcomePage);

        workFlowForm = new WorkFlowForm() {
            @Override
            public void executeWorkFlow(String fastaFileId, List<String> mgfIdsList, List<String> searchEnginesList) {
                LOGIC_LAYER.executeWorkFlow(fastaFileId, mgfIdsList, searchEnginesList);
            }

        };
        inputPanel.addComponent(workFlowForm);
        workFlowForm.setVisible(false);

        searchGUIForm = new SearchGuiInputForm() {
            @Override
            public void executeSearchGUITool(String fastaFileId, List<String> mgfIdsList, List<String> searchEnginesList) {
                LOGIC_LAYER.executeSearchGUITool(fastaFileId, mgfIdsList, searchEnginesList);
            }

        };

        searchGUIInpuPanel = new Panel("<b>SearchGUI inputs</b>", searchGUIForm);
        searchGUIInpuPanel.setCaptionAsHtml(true);
        searchGUIInpuPanel.setStyleName(ValoTheme.PANEL_BORDERLESS);
        searchGUIInpuPanel.setSizeFull();

        inputPanel.addComponent(searchGUIInpuPanel);
        searchGUIInpuPanel.setVisible(false);
        
          peptideShakerForm = new PeptideShakerInputForm() {
            @Override
            public void executePeptideShakerTool(String searchGUIResultsFileId) {
                LOGIC_LAYER.executePeptideShakerTool(searchGUIResultsFileId);
            }   
        };

        peptideShakerInpuPanel = new Panel("<b>PeptideShaker inputs</b>", peptideShakerForm);
        peptideShakerInpuPanel.setCaptionAsHtml(true);
        peptideShakerInpuPanel.setStyleName(ValoTheme.PANEL_BORDERLESS);
        searchGUIInpuPanel.setSizeFull();

        inputPanel.addComponent(peptideShakerInpuPanel);
        peptideShakerInpuPanel.setVisible(false);
        

    }

    @Override
    public String getViewId() {
        return this.getClass().getName();
    }

    @Override
    public void minimizeView() {
        mainViewPanel.addStyleName("hidepanel");
        sideButton.removeStyleName("mergewithmainview");
    }

    @Override
    public void maximizeView() {
        mainViewPanel.removeStyleName("hidepanel");
        sideButton.addStyleName("mergewithmainview");
    }

    @Override
    public AbsoluteLayout getMinimizeComponent() {
        return sideButton;
    }

    /**
     * Get main layout for the tool (input form)
     *
     * @return VerticalLayout main view panel that have data tool layout
     */
    @Override
    public VerticalLayout getMainViewComponent() {
        return mainViewPanel;
    }

    @Override
    public void layoutClick(LayoutEvents.LayoutClickEvent event) {
        Component comp = event.getClickedComponent();
        if (comp == lastSelectedBtn) {
            lastSelectedBtn = null;
            Iterator<Component> itr = topPanel.iterator();
            while (itr.hasNext()) {
                Component btn = itr.next();
                btn.removeStyleName("deactivate");
                btn.removeStyleName("apply");
            }
            welcomePage.setVisible(true);
            workFlowForm.setVisible(false);
            searchGUIInpuPanel.setVisible(false);
            peptideShakerInpuPanel.setVisible(false);
            return;
        }
        if (comp instanceof VerticalLayout) {
            lastSelectedBtn = comp;
            Integer index = ((Integer) ((VerticalLayout) comp).getData());
            if (index != null && index > 0) {
                welcomePage.setVisible(false);
                Iterator<Component> itr = topPanel.iterator();
                while (itr.hasNext()) {
                    Component btn = itr.next();
                    btn.addStyleName("deactivate");
                    btn.removeStyleName("apply");
                }
                comp.removeStyleName("deactivate");
                comp.addStyleName("apply");
                workFlowForm.setVisible(false);
                searchGUIInpuPanel.setVisible(false);
                peptideShakerInpuPanel.setVisible(false);

                switch (index) {
                    case 1:
                        //initialize workflow and input form
                        workFlowForm.setVisible(true);
                        //view work flow input form
                        workFlowForm.updateInputData(LOGIC_LAYER.getFastaFilesMap(), LOGIC_LAYER.getMgfFilesMap());
                        break;
                    case 2:
                        searchGUIInpuPanel.setVisible(true);
                        //view searchGUI input form
                        searchGUIForm.updateForm(LOGIC_LAYER.getFastaFilesMap(), LOGIC_LAYER.getMgfFilesMap());
                        break;
                    case 3:
                        //view PeptideShaker input form
                         peptideShakerInpuPanel.setVisible(true);
                        //view searchGUI input form
                        peptideShakerForm.updateForm(LOGIC_LAYER.getSearchGUIResultsFilesMap());
                        break;
                    default:
                        break;
                }
            }

        }
    }

    /**
     * Update the forms based on the history input data.
     *
     * @param fastaFilesMap map of available FASTA file in the selected history
     * @param mgfFilesMap map of available MGF files (Spectra) in the selected
     * history
     *
     */
    public void updateForm() {
        if (workFlowForm.isVisible()) {
            workFlowForm.updateInputData(LOGIC_LAYER.getFastaFilesMap(), LOGIC_LAYER.getMgfFilesMap());
        } else if (searchGUIForm.isVisible()) {
            searchGUIForm.updateForm(LOGIC_LAYER.getFastaFilesMap(), LOGIC_LAYER.getMgfFilesMap());
        }
    }

}
