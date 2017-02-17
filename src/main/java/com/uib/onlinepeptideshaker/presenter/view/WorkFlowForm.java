package com.uib.onlinepeptideshaker.presenter.view;

import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.List;
import java.util.Map;

/**
 * This class represents the data input form required to run the entire
 * work-flow
 *
 * @author Yehia Farag
 */
public abstract class WorkFlowForm extends VerticalLayout {

    private final SearchGuiInputForm searchGUIForm;

    /**
     * Constructor to initialize the main forms.
     */
    public WorkFlowForm() {
        WorkFlowForm.this.setSizeFull();
        WorkFlowForm.this.setSpacing(true);
        HorizontalLayout mainToolInputBody = new HorizontalLayout();
        mainToolInputBody.setSizeFull();
        WorkFlowForm.this.addComponent(mainToolInputBody);
        WorkFlowForm.this.setExpandRatio(mainToolInputBody, 90);

        searchGUIForm = new SearchGuiInputForm(){
            @Override
            public void executeSearchGUITool(String fastaFileId, List<String> mgfIdsList, List<String> searchEnginesList) {
                WorkFlowForm.this.executeWorkFlow(fastaFileId, mgfIdsList, searchEnginesList);
            }
        
        };
        Panel searchGUIInpuPanel = new Panel("<b>SearchGUI inputs</b>", searchGUIForm);
        searchGUIInpuPanel.setCaptionAsHtml(true);
        searchGUIInpuPanel.setStyleName(ValoTheme.PANEL_BORDERLESS);
        searchGUIInpuPanel.setSizeFull();
        mainToolInputBody.addComponent(searchGUIInpuPanel);

        VerticalLayout peptideShakerForm = new VerticalLayout();
        peptideShakerForm.setSizeFull();
        
        Panel peptideShakerInputPanel = new Panel("<b>PeptideShaker output</b>", peptideShakerForm);
        peptideShakerInputPanel.setSizeFull();
        peptideShakerInputPanel.setCaptionAsHtml(true);
        peptideShakerInputPanel.setStyleName(ValoTheme.PANEL_BORDERLESS);
        mainToolInputBody.addComponent(peptideShakerInputPanel);

    }


    /**
     * Execute the work flow in galaxy
     *
     * @param fastaFileId the selected FASTA file id
     * @param mgfIdsList The selected MGFs (spectra)file id list
     * @param searchEnginesList The selected search engine name list
     */
    public abstract void executeWorkFlow(String fastaFileId, List<String> mgfIdsList, List<String> searchEnginesList);

    /**
     * Update the forms based on the history input data
     *
     * @param fastaFilesMap map of available FASTA file in the selected history
     * @param mgfFilesMap map of available MGF files (Spectra) in the selected
     * history
     */
    public void updateInputData(Map<String, String> fastaFilesMap, Map<String, String> mgfFilesMap) {
        this.searchGUIForm.updateForm(fastaFilesMap, mgfFilesMap);

    }

}
