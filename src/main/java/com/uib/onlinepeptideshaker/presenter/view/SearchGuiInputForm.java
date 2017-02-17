package com.uib.onlinepeptideshaker.presenter.view;

import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents SearchGUI input form that has all the required
 * information to run SearchGUI tool in Galaxy
 *
 * @author Yehia Farag
 */
public abstract class SearchGuiInputForm extends VerticalLayout implements Button.ClickListener {

    /**
     * Drop down list for available FASTA files
     */
    private final ComboBox proteinDatabaseFastaFileSelect;
    /**
     * Option group for available MGF files (Spectrum)
     */
    private final OptionGroup mgfFileSelect;
    /**
     * Available supported search engine
     */
    private final OptionGroup DBSearchEnginesSelect;
    /**
     * Execute tool button
     */
    private final Button executeBtn;

    /**
     * Constructor to initialize the main attributes
     */
    public SearchGuiInputForm() {
        SearchGuiInputForm.this.setSizeFull();
        SearchGuiInputForm.this.setHeightUndefined();
        SearchGuiInputForm.this.setMargin(true);
        SearchGuiInputForm.this.setSpacing(true);
        proteinDatabaseFastaFileSelect = new ComboBox("Protein Database");
        proteinDatabaseFastaFileSelect.addStyleName(ValoTheme.COMBOBOX_SMALL);
        proteinDatabaseFastaFileSelect.addStyleName(ValoTheme.COMBOBOX_TINY);
        proteinDatabaseFastaFileSelect.setWidth(100, Unit.PERCENTAGE);
        proteinDatabaseFastaFileSelect.setHeight(100, Unit.PERCENTAGE);
        proteinDatabaseFastaFileSelect.setStyleName("standredcomboboxwithcaption");
        proteinDatabaseFastaFileSelect.setNullSelectionAllowed(false);

        SearchGuiInputForm.this.addComponent(proteinDatabaseFastaFileSelect);
        SearchGuiInputForm.this.setExpandRatio(proteinDatabaseFastaFileSelect, 5);

        mgfFileSelect = new OptionGroup("Spectrum File(s)");
        mgfFileSelect.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
        mgfFileSelect.setWidth(100, Unit.PERCENTAGE);
        mgfFileSelect.setHeight(100, Unit.PERCENTAGE);

        mgfFileSelect.setMultiSelect(true);
        mgfFileSelect.setNullSelectionAllowed(false);

        SearchGuiInputForm.this.addComponent(mgfFileSelect);
        SearchGuiInputForm.this.setExpandRatio(mgfFileSelect, 45);

        DBSearchEnginesSelect = new OptionGroup("DataBase-Search Engines");
        DBSearchEnginesSelect.addStyleName(ValoTheme.OPTIONGROUP_SMALL);
        DBSearchEnginesSelect.setWidth(100, Unit.PERCENTAGE);
        DBSearchEnginesSelect.setHeight(100, Unit.PERCENTAGE);
        DBSearchEnginesSelect.setMultiSelect(true);
        DBSearchEnginesSelect.addItem("X!Tandem");
        DBSearchEnginesSelect.select("X!Tandem");
        DBSearchEnginesSelect.addItem("MS-GF+");
        DBSearchEnginesSelect.select("MS-GF+");
        DBSearchEnginesSelect.addItem("OMSSA");
        DBSearchEnginesSelect.select("OMSSA");
        DBSearchEnginesSelect.addItem("Comet");

        DBSearchEnginesSelect.setNullSelectionAllowed(false);
        SearchGuiInputForm.this.addComponent(DBSearchEnginesSelect);
        SearchGuiInputForm.this.setExpandRatio(DBSearchEnginesSelect, 45);
//        inputForm.setExpandRatio(DBSearchEnginesSelect, 5);

        executeBtn = new Button("Execute");
        executeBtn.setStyleName(ValoTheme.BUTTON_SMALL);
        executeBtn.addStyleName(ValoTheme.BUTTON_TINY);
        executeBtn.addStyleName("maxwidth100");
        executeBtn.addStyleName("maxheight25");
        SearchGuiInputForm.this.addComponent(executeBtn);
        SearchGuiInputForm.this.setExpandRatio(executeBtn, 10);
        executeBtn.addClickListener(SearchGuiInputForm.this);
    }

    /**
     * Update the forms based on the history input data.
     *
     * @param fastaFilesMap map of available FASTA file in the selected history
     * @param mgfFilesMap map of available MGF files (Spectra) in the selected
     * history
     *
     */
    public void updateForm(Map<String, String> fastaFilesMap, Map<String, String> mgfFilesMap) {
        proteinDatabaseFastaFileSelect.removeAllItems();
        mgfFileSelect.removeAllItems();
        for (String id : fastaFilesMap.keySet()) {
            proteinDatabaseFastaFileSelect.addItem(id);
            proteinDatabaseFastaFileSelect.setItemCaption(id, fastaFilesMap.get(id));
            proteinDatabaseFastaFileSelect.setValue(id);
        }
        for (String id : mgfFilesMap.keySet()) {
            mgfFileSelect.addItem(id);
            mgfFileSelect.setItemCaption(id, mgfFilesMap.get(id));

        }
        mgfFileSelect.setValue(mgfFileSelect.getItemIds());

    }

 
    /**
     * Execute the work flow in galaxy
     *
     * @param fastaFileId the selected FASTA file id
     * @param mgfIdsList The selected MGFs (spectra)file id list
     * @param searchEnginesList The selected search engine name list
     */
    public abstract void executeSearchGUITool(String fastaFileId, List<String> mgfIdsList, List<String> searchEnginesList);

    /**
     * Get the selected FASTA file id
     *
     * @return String FASTA file id (History dataset id on galaxy server)
     */
    private String getSelectedProteinDatabaseFastaFileId() {
        return proteinDatabaseFastaFileSelect.getValue() + "";
    }

    /**
     * Get the selected MGF (Spectra) files ids
     *
     * @return List of MGF file ids (History dataset id on galaxy server)
     */
    private List<String> getSelectedMgfFileIds() {
        List<String> ids = new ArrayList<>();
        List<Object> objects = new ArrayList<>((Set<Object>) mgfFileSelect.getValue());
        for (Object o : objects) {
            ids.add(o + "");
        }
        return ids;
    }

    /**
     * Get the selected search engines parameters
     *
     * @return List of search engines names
     */
    private List<String> getDBSearchEnginesSelect() {
        List<String> ids = new ArrayList<>();
        List<Object> objects = new ArrayList<>((Set<Object>) DBSearchEnginesSelect.getValue());
        for (Object o : objects) {
            ids.add(o + "");
        }
        return ids;
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        executeSearchGUITool(getSelectedProteinDatabaseFastaFileId(), getSelectedMgfFileIds(), getDBSearchEnginesSelect());
    }

}
