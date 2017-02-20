package com.uib.onlinepeptideshaker.presenter;

import com.uib.onlinepeptideshaker.managers.RegistrableView;
import com.uib.onlinepeptideshaker.model.LogicLayer;
import com.uib.onlinepeptideshaker.presenter.view.WorkFlowForm;
import com.vaadin.data.Property;
import com.vaadin.data.Property.ValueChangeListener;
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
     * Bottom left layout (peptide and psm table container).
     */
    private VerticalLayout bottomLeftPanel;
    /**
     * Bottom layout (peptide-psm-mgf tables container).
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

    private final Table proteinsTable;
    private final Table peptidesTable;
    private final Table psmTable;
    private final Table mgfTable;
    private String peptideShakerResultsId;
    private final ValueChangeListener peptideTableListener;
    private final ValueChangeListener psmTableListener;

    /**
     * Initialize the web tool main attributes
     *
     * @param searchGUITool SearchGUI web tool
     */
    public WebVisualizationPresenter(LogicLayer LOGIC_LAYER) {
        this.LOGIC_LAYER = LOGIC_LAYER;
        this.proteinsTable = new Table();
        this.peptidesTable = new Table();
        this.psmTable = new Table();
        mgfTable = new Table();
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
        psmTableListener = (Property.ValueChangeEvent event) -> {
            this.mgfTable.removeAllItems();
            if (event.getProperty().getValue() == null) {
                return;
            }
            String value = "" + event.getProperty().getValue().toString();
            Set<Object[]> proteinsSet = LOGIC_LAYER.getMGF(psmTable.getItem(value).getItemProperty("Spectrum_Title").getValue().toString(), this.peptideShakerResultsId);
            int index = 1;
            for (Object[] proteinBean : proteinsSet) {
                proteinBean[0] = "" + index++;
                this.mgfTable.addItem(proteinBean, proteinBean[0]);
            }
            this.mgfTable.markAsDirty();
        };
        peptideTableListener = (Property.ValueChangeEvent event) -> {
            this.psmTable.removeValueChangeListener(psmTableListener);
            this.psmTable.removeAllItems();

            if (event.getProperty().getValue() == null) {
                return;
            }
            String value = "" + event.getProperty().getValue().toString().split("__")[0];
            Set<Object[]> proteinsSet = LOGIC_LAYER.getPsm(value, this.peptideShakerResultsId);
            int index = 1;
            for (Object[] proteinBean : proteinsSet) {
                proteinBean[0] = "" + index++;
                this.psmTable.addItem(proteinBean, proteinBean[0] + "_" + proteinBean[1]);
            }
            this.psmTable.markAsDirty();
            this.psmTable.addValueChangeListener(psmTableListener);
             this.psmTable.select(this.psmTable.getItemIds().iterator().next());

        };

    }

    /**
     * Initialize the proteins table.
     */
    private void initProteinTable() {
//        this.proteinsTable.setStyleName(ValoTheme.TABLE_SMALL);

        this.proteinsTable.setCaption("<b>Proteins</b>");
        this.proteinsTable.setCaptionAsHtml(true);
        this.proteinsTable.setStyleName("framedpanel");
        this.proteinsTable.addStyleName(ValoTheme.TABLE_BORDERLESS);
        this.proteinsTable.setHeight(100, Unit.PERCENTAGE);
        this.proteinsTable.setWidth(100, Unit.PERCENTAGE);
        this.proteinsTable.setCacheRate(1);

        this.proteinsTable.setSelectable(true);
        this.proteinsTable.setSortEnabled(true);
        this.proteinsTable.setColumnReorderingAllowed(false);

        this.proteinsTable.setColumnCollapsingAllowed(true);
        this.proteinsTable.setImmediate(true);
        this.proteinsTable.setMultiSelect(false);

        this.proteinsTable.addContainerProperty("Index", String.class, null, "", null, Table.Align.RIGHT);
        this.proteinsTable.addContainerProperty("Accession", String.class, null, "Accession", null, Table.Align.CENTER);
        this.proteinsTable.addContainerProperty("Name", String.class, null, "Name", null, Table.Align.LEFT);
        this.proteinsTable.addContainerProperty("geneName", String.class, null, "Gene Name", null, Table.Align.CENTER);
        this.proteinsTable.addContainerProperty("mwkDa", String.class, null, "MW (kDa)", null, Table.Align.RIGHT);
        this.proteinsTable.addContainerProperty("possibleCoverage", String.class, null, "Possible Coverage", null, Table.Align.RIGHT);
        this.proteinsTable.addContainerProperty("peptides_number", String.class, null, "#Peptides", null, Table.Align.RIGHT);

    }

    /**
     * Initialize the proteins table.
     */
    private void initPeptidesTable() {
//        this.proteinsTable.setStyleName(ValoTheme.TABLE_SMALL);
        this.peptidesTable.setCaption("<b>Peptides</b>");
        this.peptidesTable.setCaptionAsHtml(true);
        this.peptidesTable.setStyleName("framedpanel");
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
        this.peptidesTable.addContainerProperty("pi", String.class, null, "PI", null, Table.Align.LEFT);
        this.peptidesTable.addContainerProperty("Sequence", String.class, null, "Sequence", null, Table.Align.CENTER);
        this.peptidesTable.addContainerProperty("Modified Sequence", String.class, null, "Modified Sequence", null, Table.Align.RIGHT);
        this.peptidesTable.addContainerProperty("PSM_number", String.class, null, "#PSM", null, Table.Align.RIGHT);
        this.peptidesTable.addContainerProperty("Validation", String.class, null, "Validation", null, Table.Align.RIGHT);

    }

    /**
     * Initialize the proteins table.
     */
    private void initPsmTable() {
//        this.proteinsTable.setStyleName(ValoTheme.TABLE_SMALL);
        this.psmTable.setCaption("<b>Peptide Spectrum Matches</b>");
        this.psmTable.setCaptionAsHtml(true);
        this.psmTable.setStyleName("framedpanel");
        this.psmTable.addStyleName(ValoTheme.TABLE_BORDERLESS);
        this.psmTable.setHeight(100, Unit.PERCENTAGE);
        this.psmTable.setWidth(100, Unit.PERCENTAGE);
        this.psmTable.setCacheRate(1);

        this.psmTable.setSelectable(true);
        this.psmTable.setSortEnabled(true);
        this.psmTable.setColumnReorderingAllowed(false);

        this.psmTable.setColumnCollapsingAllowed(true);
        this.psmTable.setImmediate(true);
        this.psmTable.setMultiSelect(false);

        this.psmTable.addContainerProperty("Index", String.class, null, "", null, Table.Align.RIGHT);
        this.psmTable.addContainerProperty("Protein(s)", String.class, null, "Protein(s)", null, Table.Align.CENTER);
        this.psmTable.addContainerProperty("Sequence", String.class, null, "Sequence", null, Table.Align.CENTER);
        this.psmTable.addContainerProperty("m/z_Error", String.class, null, "m/z Error", null, Table.Align.RIGHT);
        this.psmTable.addContainerProperty("Charge", String.class, null, "Charge", null, Table.Align.RIGHT);
        this.psmTable.addContainerProperty("Spectrum_File", String.class, null, "Spectrum File", null, Table.Align.RIGHT);
        this.psmTable.addContainerProperty("Spectrum_Title", String.class, null, "Spectrum Title", null, Table.Align.RIGHT);
        this.psmTable.addContainerProperty("Validation", String.class, null, "Validation", null, Table.Align.RIGHT);

    }

    /**
     * Initialize the proteins table.
     */
    private void initMgfTable() {
//        this.proteinsTable.setStyleName(ValoTheme.TABLE_SMALL);
        this.mgfTable.setCaption("<b>Spectrum & Fragment IonsMatches</b>");
        this.mgfTable.setCaptionAsHtml(true);
        this.mgfTable.setStyleName("framedpanel");
        this.mgfTable.addStyleName(ValoTheme.TABLE_BORDERLESS);
        this.mgfTable.setHeight(100, Unit.PERCENTAGE);
        this.mgfTable.setWidth(100, Unit.PERCENTAGE);
        this.mgfTable.setCacheRate(1);

        this.mgfTable.setSelectable(true);
        this.mgfTable.setSortEnabled(true);
        this.mgfTable.setColumnReorderingAllowed(false);

        this.mgfTable.setColumnCollapsingAllowed(true);
        this.mgfTable.setImmediate(true);
        this.mgfTable.setMultiSelect(false);

        this.mgfTable.addContainerProperty("Index", String.class, null, "", null, Table.Align.CENTER);
        this.mgfTable.addContainerProperty("x", String.class, null, "x", null, Table.Align.CENTER);
        this.mgfTable.addContainerProperty("y", String.class, null, "y", null, Table.Align.CENTER);

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
//       0
        this.mainViewPanel.addComponent(this.topPanel);
        topPanel.setMargin(false);
        this.topPanel.addComponent(this.proteinsTable);
        this.initProteinTable();
        this.bottomPanel = new HorizontalLayout();
        this.bottomPanel.setSizeFull();
        this.bottomPanel.setSpacing(true);

        this.mainViewPanel.addComponent(this.bottomPanel);

        this.bottomLeftPanel = new VerticalLayout();
        this.bottomLeftPanel.setSizeFull();
        this.bottomLeftPanel.setSpacing(true);

        this.bottomPanel.addComponent(this.bottomLeftPanel);
        this.bottomLeftPanel.addComponent(this.peptidesTable);
        this.bottomLeftPanel.addComponent(this.psmTable);
        this.bottomPanel.addComponent(this.mgfTable);
        bottomLeftPanel.setMargin(false);
        this.initPeptidesTable();
        this.initPsmTable();
        initMgfTable();
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
        proteinsTable.removeValueChangeListener(WebVisualizationPresenter.this);
        this.proteinsTable.removeAllItems();

        Set<Object[]> proteinsSet = LOGIC_LAYER.loadPeptideShakerResults(peptideShakerResultsId);

        for (Object[] proteinBean : proteinsSet) {
            this.proteinsTable.addItem(proteinBean, proteinBean[1]);

        }
        this.proteinsTable.markAsDirty();
        proteinsTable.addValueChangeListener(WebVisualizationPresenter.this);
        proteinsTable.select(proteinsTable.getItemIds().iterator().next());

    }

    public void updateTableItems() {

    }

    @Override
    public void valueChange(Property.ValueChangeEvent event) {

        this.peptidesTable.removeValueChangeListener(peptideTableListener);
        this.peptidesTable.removeAllItems();
        if (event.getProperty().getValue() == null) {
            return;
        }
        Set<Object[]> proteinsSet = LOGIC_LAYER.getPeptides(event.getProperty().getValue().toString(), this.peptideShakerResultsId);
        int index = 1;
        for (Object[] proteinBean : proteinsSet) {
            proteinBean[0] = "" + index++;
            this.peptidesTable.addItem(proteinBean, proteinBean[3] + "__" + proteinBean[0]);
        }
        this.peptidesTable.markAsDirty();
        this.peptidesTable.addValueChangeListener(peptideTableListener);
        this.peptidesTable.select(peptidesTable.getItemIds().iterator().next());
    }

}
