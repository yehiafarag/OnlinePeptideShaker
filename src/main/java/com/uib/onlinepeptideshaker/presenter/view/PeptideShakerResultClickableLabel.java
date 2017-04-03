package com.uib.onlinepeptideshaker.presenter.view;

import com.uib.onlinepeptideshaker.model.beans.PeptideShakerViewBean;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;

/**
 * This class represents the label in the history table under PeptideShaker
 * results section the label allow user to view results or download CPS file or
 * zip File contain all the results
 *
 * @author Yehia Farag
 */
public abstract class PeptideShakerResultClickableLabel extends HorizontalLayout implements LayoutEvents.LayoutClickListener {

    private final PeptideShakerViewBean results;

    /**
     * Constructor to initialize the main variables.
     *
     * @param results peptideShaker results bean that has all required
     * information to view and download files
     */
    public PeptideShakerResultClickableLabel(PeptideShakerViewBean results) {
        this.results = results;
        PeptideShakerResultClickableLabel.this.setSizeFull();
        PeptideShakerResultClickableLabel.this.setStyleName("deletablelabel");
        PeptideShakerResultClickableLabel.this.addLayoutClickListener(PeptideShakerResultClickableLabel.this);
        Label title = new Label(results.getViewName());
        title.setContentMode(ContentMode.HTML);
        title.setSizeFull();
        PeptideShakerResultClickableLabel.this.addComponent(title);
        PeptideShakerResultClickableLabel.this.setExpandRatio(title, 80);
        HorizontalLayout rightSideBtnsContainer = new HorizontalLayout();
        PeptideShakerResultClickableLabel.this.addComponent(rightSideBtnsContainer);
        PeptideShakerResultClickableLabel.this.setExpandRatio(rightSideBtnsContainer, 20);
        rightSideBtnsContainer.setSizeFull();
        rightSideBtnsContainer.setSpacing(true);

        VerticalLayout saveCPSBtn = new VerticalLayout();
        saveCPSBtn.setStyleName("savebtn");
        saveCPSBtn.setSizeFull();
        rightSideBtnsContainer.addComponent(saveCPSBtn);

        VerticalLayout saveZipBtn = new VerticalLayout();
        saveZipBtn.setStyleName("savemultibtn");
        saveZipBtn.setSizeFull();
        rightSideBtnsContainer.addComponent(saveZipBtn);

        VerticalLayout closeBtn = new VerticalLayout();
        closeBtn.setStyleName("closebtn");
        closeBtn.setSizeFull();
        rightSideBtnsContainer.addComponent(closeBtn);

    }

    @Override
    public void layoutClick(LayoutEvents.LayoutClickEvent event) {
        viewResults(results);
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public abstract void viewResults(PeptideShakerViewBean results);

}
