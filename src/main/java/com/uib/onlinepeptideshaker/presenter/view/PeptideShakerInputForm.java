package com.uib.onlinepeptideshaker.presenter.view;

import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.List;
import java.util.Map;

/**
 * This class represents input form for PeptideShaker tool in galaxy server
 *
 * @author yfa041
 */
public abstract class PeptideShakerInputForm extends VerticalLayout implements Button.ClickListener {

    /**
     * Drop down list for available searchGUI results files
     */
    private final ComboBox searchGUIResultsSelect;
    /**
     * Execute tool button
     */
    private final Button executeBtn;

    /**
     * Constructor to initialize the main attributes
     */
    public PeptideShakerInputForm() {
        PeptideShakerInputForm.this.setSizeFull();
        PeptideShakerInputForm.this.setHeightUndefined();
        PeptideShakerInputForm.this.setMargin(true);
        PeptideShakerInputForm.this.setSpacing(true);
        searchGUIResultsSelect = new ComboBox("SearchGUI results");
        searchGUIResultsSelect.addStyleName(ValoTheme.COMBOBOX_SMALL);
        searchGUIResultsSelect.addStyleName(ValoTheme.COMBOBOX_TINY);
        searchGUIResultsSelect.setWidth(100, Unit.PERCENTAGE);
        searchGUIResultsSelect.setHeight(100, Unit.PERCENTAGE);
        searchGUIResultsSelect.setStyleName("standredcomboboxwithcaption");
        searchGUIResultsSelect.setNullSelectionAllowed(false);

        PeptideShakerInputForm.this.addComponent(searchGUIResultsSelect);
        PeptideShakerInputForm.this.setExpandRatio(searchGUIResultsSelect, 5);

        executeBtn = new Button("Execute");
        executeBtn.setStyleName(ValoTheme.BUTTON_SMALL);
        executeBtn.addStyleName(ValoTheme.BUTTON_TINY);
        executeBtn.addStyleName("maxwidth100");
        executeBtn.addStyleName("maxheight25");
        PeptideShakerInputForm.this.addComponent(executeBtn);
        PeptideShakerInputForm.this.setExpandRatio(executeBtn, 10);
        executeBtn.addClickListener(PeptideShakerInputForm.this);
    }

    /**
     * Update the forms based on the history input data.
     *
     * @param fastaFilesMap map of available FASTA file in the selected history
     * @param mgfFilesMap map of available MGF files (Spectra) in the selected
     * history
     *
     */
    public void updateForm(Map<String, String> searchGUIResultsFileMap) {
        searchGUIResultsSelect.removeAllItems();
        for (String id : searchGUIResultsFileMap.keySet()) {
            searchGUIResultsSelect.addItem(id);
            searchGUIResultsSelect.setItemCaption(id, searchGUIResultsFileMap.get(id));
            searchGUIResultsSelect.setValue(id);
        }

    }

    /**
     * Get the selected SearchGUI results file id
     *
     * @return String SearchGUI results file id (History dataset id on galaxy
     * server)
     */
    private String getSelectedSearchGUIResultsFileId() {
        return searchGUIResultsSelect.getValue() + "";
    }

    /**
     * Execute the work flow in galaxy
     *
     * @param searchGUIFileId the selected searchGUI results file id
     */
    public abstract void executePeptideShakerTool(String searchGUIFileId);

    @Override
    public void buttonClick(Button.ClickEvent event) {
        executePeptideShakerTool(getSelectedSearchGUIResultsFileId());
    }
}
