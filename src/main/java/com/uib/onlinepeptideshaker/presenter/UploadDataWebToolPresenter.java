package com.uib.onlinepeptideshaker.presenter;

import com.uib.onlinepeptideshaker.managers.RegistrableView;
import com.uib.onlinepeptideshaker.model.beans.WebTool;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.Sizeable;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Image;
import com.vaadin.ui.VerticalLayout;

/**
 * This class represents Upload data tool in Galaxy server where user can upload
 * files to his history
 *
 * @author Yehia Farag
 */
public class UploadDataWebToolPresenter implements RegistrableView, LayoutEvents.LayoutClickListener {

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
     * Initialize the web tool main attributes
     *
     * @param uploadToGalaxyTool SearchGUI web tool
     */
    public UploadDataWebToolPresenter() {
        this.mainViewPanel = new VerticalLayout();
        this.initializeMainViewPanel();
        this.sideButton = new AbsoluteLayout();
        this.sideButton.setSizeFull();
        this.sideButton.setStyleName("frame");
        this.sideButton.addStyleName("sidebutton");

        extender = new VerticalLayout();
        this.sideButton.addComponent(extender);
        extender.setSizeFull();

        extender.addStyleName("sidebuttonframe");
        Image icon = new Image();
        icon.setSource(new ThemeResource("img/upload.png"));
        icon.setWidth(100, Sizeable.Unit.PERCENTAGE);
        extender.addComponent(icon);
        extender.setComponentAlignment(icon, Alignment.MIDDLE_CENTER);
        extender.setExpandRatio(icon, 8);
        this.sideButton.setData(UploadDataWebToolPresenter.this.getViewId());
        icon.setData(UploadDataWebToolPresenter.this.getViewId());

    }

    /**
     * Initialize the web tool main view panel that has the main forms and view.
     * components
     */
    private void initializeMainViewPanel() {
        this.mainViewPanel.setSizeFull();
        this.mainViewPanel.setStyleName("toolviewframe");
        VerticalLayout toolViewContainer = new VerticalLayout();

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
//        if (initSearchGUITool && uploadToGalaxyTool.getToolIds().size() > 1) {
//            System.out.println("please select the tool");
//            uploadToGalaxyTool.setActiveTool(uploadToGalaxyTool.getSectionIds().get(1), uploadToGalaxyTool.getToolIds().get(1));
//            initSearchGUITool = false;
//        }
    }
}
