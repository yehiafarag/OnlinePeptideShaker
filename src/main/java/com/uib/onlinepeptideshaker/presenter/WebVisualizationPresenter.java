package com.uib.onlinepeptideshaker.presenter;

import com.uib.onlinepeptideshaker.managers.RegistrableView;
import com.uib.onlinepeptideshaker.model.LogicLayer;
import com.uib.onlinepeptideshaker.model.beans.ProteinBean;
import com.uib.onlinepeptideshaker.model.beans.WebTool;
import com.uib.onlinepeptideshaker.presenter.view.WorkFlowForm;
import com.vaadin.data.Property;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.Iterator;
import java.util.Set;

/**
 * This class represent web tool presenter which is responsible for managing the
 * view and interactivity of the tool
 *
 * @author Yehia Farag
 */
public class WebVisualizationPresenter implements RegistrableView, LayoutEvents.LayoutClickListener, Property.ValueChangeListener {

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
     * Top layout (proteins table container).
     */
    private HorizontalLayout topPanel;
    /**
     * Bottom layout (peptide table container).
     */
    private HorizontalLayout bottomPanel;
    /**
     * Last selected top button.
     */
    private Component lastSelectedBtn;
    /**
     * Work flow input layout.
     */
    private WorkFlowForm workFlowForm;
    /**
     * Welcome page for tools.
     */
    private VerticalLayout welcomePage;
    /**
     * The galaxy server logic layer.
     */
    private final LogicLayer LOGIC_LAYER;

    private final Table protiensTable;
    private final Table peptidesTable;
    private String peptideShakerResultsId;

    /**
     * Initialize the web tool main attributes
     *
     * @param searchGUITool SearchGUI web tool
     */
    public WebVisualizationPresenter(LogicLayer LOGIC_LAYER) {
        this.LOGIC_LAYER = LOGIC_LAYER;
        this.protiensTable = new Table();
        this.peptidesTable = new Table();
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
        icon.setSource(new ThemeResource("img/graph.png"));
        icon.setWidth(100, Unit.PERCENTAGE);
        extender.addComponent(icon);
        extender.setComponentAlignment(icon, Alignment.MIDDLE_CENTER);
        extender.setExpandRatio(icon, 8);
        this.sideButton.setData(WebVisualizationPresenter.this.getViewId());
        icon.setData(WebVisualizationPresenter.this.getViewId());

    }

    /**
     * Initialize the proteins table.
     */
    private void initProteinTable() {
//        this.protiensTable.setStyleName(ValoTheme.TABLE_SMALL);
        this.protiensTable.addStyleName(ValoTheme.TABLE_BORDERLESS);
        this.protiensTable.setHeight(100, Unit.PERCENTAGE);
        this.protiensTable.setWidth(100, Unit.PERCENTAGE);
        this.protiensTable.setCacheRate(1);

        this.protiensTable.setSelectable(true);
        this.protiensTable.setSortEnabled(true);
        this.protiensTable.setColumnReorderingAllowed(false);

        this.protiensTable.setColumnCollapsingAllowed(true);
        this.protiensTable.setImmediate(true);
        this.protiensTable.setMultiSelect(false);

        this.protiensTable.addContainerProperty("Index", String.class, null, "", null, Table.Align.RIGHT);
        this.protiensTable.addContainerProperty("Accession", String.class, null, "Accession", null, Table.Align.CENTER);
        this.protiensTable.addContainerProperty("Name", String.class, null, "Name", null, Table.Align.LEFT);
        this.protiensTable.addContainerProperty("geneName", String.class, null, "Gene Name", null, Table.Align.CENTER);
        this.protiensTable.addContainerProperty("mwkDa", String.class, null, "MW (kDa)", null, Table.Align.RIGHT);
        this.protiensTable.addContainerProperty("possibleCoverage", String.class, null, "Possible Coverage", null, Table.Align.RIGHT);

    }

    /**
     * Initialize the proteins table.
     */
    private void initPeptidesTable() {
//        this.protiensTable.setStyleName(ValoTheme.TABLE_SMALL);
        this.peptidesTable.addStyleName(ValoTheme.TABLE_BORDERLESS);
        this.peptidesTable.setHeight(100, Unit.PERCENTAGE);
        this.peptidesTable.setWidth(100, Unit.PERCENTAGE);
        this.peptidesTable.setCacheRate(1);

        this.peptidesTable.setSelectable(true);
        this.peptidesTable.setSortEnabled(true);
        this.peptidesTable.setColumnReorderingAllowed(false);

        this.peptidesTable.setColumnCollapsingAllowed(true);
        this.peptidesTable.setImmediate(true);
        this.peptidesTable.setMultiSelect(false);

        this.peptidesTable.addContainerProperty("Index", String.class, null, "", null, Table.Align.RIGHT);
        this.peptidesTable.addContainerProperty("Protein(s)", String.class, null, "Protein(s)", null, Table.Align.CENTER);
        this.peptidesTable.addContainerProperty("Protein Group(s)", String.class, null, "Name", null, Table.Align.LEFT);
        this.peptidesTable.addContainerProperty("Sequence", String.class, null, "Sequence", null, Table.Align.CENTER);
        this.peptidesTable.addContainerProperty("Modified Sequence", String.class, null, "Modified Sequence", null, Table.Align.RIGHT);
        this.peptidesTable.addContainerProperty("Validation", String.class, null, "Validation", null, Table.Align.RIGHT);

    }

    /**
     * Initialize the web tool main view panel that has the main forms and view.
     * components
     */
    private void initializeMainViewPanel() {
        this.mainViewPanel.setSizeFull();
        this.mainViewPanel.setStyleName("toolviewframe");
        this.mainViewPanel.setSpacing(true);

        this.topPanel = new HorizontalLayout();
        this.topPanel.setSizeFull();
        this.topPanel.setStyleName("framedpanel");
        this.topPanel.setCaption("<b>Proteins</b>");
        this.topPanel.setCaptionAsHtml(true);
        this.mainViewPanel.addComponent(this.topPanel);
        topPanel.setMargin(false);
        this.topPanel.addComponent(this.protiensTable);
        this.initProteinTable();

        this.bottomPanel = new HorizontalLayout();
        this.bottomPanel.setSizeFull();
        this.bottomPanel.setCaption("<b>Peptides</b>");
        this.bottomPanel.setCaptionAsHtml(true);
        this.bottomPanel.setStyleName("framedpanel");
        this.mainViewPanel.addComponent(this.bottomPanel);
        this.bottomPanel.addComponent(this.peptidesTable);
        bottomPanel.setMargin(false);
        this.initPeptidesTable();
        this.mainViewPanel.addStyleName("hidepanel");

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
                switch (index) {
                    case 1:
                        //initialize workflow and input form
                        workFlowForm.setVisible(true);
                        //view work flow input form
                        workFlowForm.updateInputData(LOGIC_LAYER.getFastaFilesMap(), LOGIC_LAYER.getMgfFilesMap());
                        break;
                    case 2:
                        //view searchGUI input form

                        break;
                    case 3:
                        //view PeptideShaker input form
                        break;
                    default:
                        break;
                }
            }

        }
    }

    public void updateProteinTable(String peptideShakerResultsId) {
        this.peptideShakerResultsId = peptideShakerResultsId;
        protiensTable.removeValueChangeListener(WebVisualizationPresenter.this);
        this.protiensTable.removeAllItems();
        
        Set<Object[]> proteinsSet = LOGIC_LAYER.loadPeptideShakerResults(peptideShakerResultsId);
       
        for (Object[] proteinBean : proteinsSet) {
            this.protiensTable.addItem(proteinBean, proteinBean[1]);

        }
        this.protiensTable.markAsDirty();
        protiensTable.addValueChangeListener(WebVisualizationPresenter.this);

    }
    public void updateTableItems(){
    
    
    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {

        this.peptidesTable.removeAllItems();
        if (event.getProperty().getValue() == null) {
            return;
        }
        Set<Object[]> proteinsSet = LOGIC_LAYER.getPeptides(event.getProperty().getValue().toString(), this.peptideShakerResultsId);
        int index = 1;
        for (Object[] proteinBean : proteinsSet) {
            proteinBean[0] = "" + index++;
            this.peptidesTable.addItem(proteinBean, proteinBean[1]);
        }
        this.peptidesTable.markAsDirty();
    }

}
