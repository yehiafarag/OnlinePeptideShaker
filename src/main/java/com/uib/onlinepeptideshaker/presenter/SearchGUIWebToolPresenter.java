package com.uib.onlinepeptideshaker.presenter;

import com.uib.onlinepeptideshaker.managers.RegistrableView;
import com.uib.onlinepeptideshaker.model.beans.WebTool;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.Sizeable.Unit;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.VerticalLayout;
import java.util.Iterator;

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
     * Top layout (tools button container).
     */
    private HorizontalLayout topPanel;

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
        startWorkFlowIcon.setData(1);
        topPanel.addComponent(startWorkFlowIcon);
        
        VerticalLayout searchGUIIcon = new VerticalLayout();
        searchGUIIcon.setSizeFull();
        searchGUIIcon.setStyleName("searchguiicon");
        searchGUIIcon.setData(2);
        topPanel.addComponent(searchGUIIcon);
        
        VerticalLayout firstprocess = new VerticalLayout();
        firstprocess.setSizeFull();
        firstprocess.setStyleName("processicon");
        firstprocess.setData(-1);
        topPanel.addComponent(firstprocess);
        
        VerticalLayout peptideShakerIcon = new VerticalLayout();
        peptideShakerIcon.setSizeFull();
        peptideShakerIcon.setData(3);
        peptideShakerIcon.setStyleName("peptideshakericon");
        topPanel.addComponent(peptideShakerIcon);
        
        VerticalLayout secondProcess = new VerticalLayout();
        secondProcess.setSizeFull();
        secondProcess.setData(-2);
        secondProcess.setStyleName("processicon");
        secondProcess.setEnabled(false);
        topPanel.addComponent(secondProcess);
        
        VerticalLayout endWorkFlowIcon = new VerticalLayout();
        endWorkFlowIcon.setSizeFull();
        endWorkFlowIcon.setData(-3);
        endWorkFlowIcon.setStyleName("endworkflow");
        topPanel.addComponent(endWorkFlowIcon);
        topPanel.addLayoutClickListener(SearchGUIWebToolPresenter.this);
        
        HorizontalLayout inputPanel = new HorizontalLayout();
        inputPanel.setSizeFull();
        inputPanel.setStyleName("framedpanel");
        this.mainViewPanel.addComponent(inputPanel);
        this.mainViewPanel.setExpandRatio(inputPanel, 90);
        
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
        if (comp instanceof VerticalLayout) {
            Integer index = ((Integer) ((VerticalLayout) comp).getData());
            if (index != null && index > 0) {
                Iterator<Component>itr=topPanel.iterator();
                while (itr.hasNext()) {
                    Component btn = itr.next();
                    btn.addStyleName("deactivate");  
                    btn.removeStyleName("apply");
                }
                comp.removeStyleName("deactivate");
                comp.addStyleName("apply");
                switch (index) {
                    case 1:
                        //initialize workflow and input form
                        //view work flow input form
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
    
}
