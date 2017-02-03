package com.uib.onlinepeptideshaker.presenter;

import com.uib.onlinepeptideshaker.managers.RegistrableView;
import com.uib.onlinepeptideshaker.model.beans.WebTool;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * This class represent web tool presenter which is responsible for managing the
 * view and interactivity of the tool
 *
 * @author Yehia Farag
 */
public class SearchGUIWebToolPresenter implements RegistrableView, LayoutEvents.LayoutClickListener {

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
     * Initialize web tool.
     */
    private boolean initSearchGUITool = true;

    /**
     * Initialize the web tool main attributes
     *
     * @param searchGUITool SearchGUI web tool
     */
    public SearchGUIWebToolPresenter(WebTool searchGUITool) {
        this.searchGUITool = searchGUITool;

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
//        VerticalLayout container = new VerticalLayout();
//        this.sideButton.addComponent(container);
//        container.setSizeFull();
//         VerticalLayout settingicon = new VerticalLayout();
//        settingicon.setIcon(VaadinIcons.COG);
//        settingicon.setSpacing(true);
////        settingicon.setHeight(100, Unit.PERCENTAGE);
//        settingicon.setWidth(10, Unit.PERCENTAGE);
//        this.sideButton.addComponent(settingicon);
//        this.sideButton.setComponentAlignment(settingicon, Alignment.TOP_RIGHT);
//        this.sideButton.setExpandRatio(settingicon,2);
        Image icon = new Image();
        icon.setSource(new ThemeResource("img/searchgui.ico"));
        icon.setWidth(100, Unit.PERCENTAGE);
        extender.addComponent(icon);
        extender.setComponentAlignment(icon, Alignment.MIDDLE_CENTER);
        extender.setExpandRatio(icon, 8);
        this.sideButton.setData(SearchGUIWebToolPresenter.this.getViewId());
        icon.setData(SearchGUIWebToolPresenter.this.getViewId());
        if (searchGUITool == null) {
            sideButton.setEnabled(false);
            return;
        }
//        this.maximizeView();
//        settingicon.addLayoutClickListener(SearchGUIWebToolPresenter.this);

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
        if (initSearchGUITool && searchGUITool.getToolIds().size() > 1) {
            System.out.println("please select the tool");
            searchGUITool.setActiveTool(searchGUITool.getSectionIds().get(1), searchGUITool.getToolIds().get(1));
            initSearchGUITool = false;
        }
    }

}
