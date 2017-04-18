package com.uib.onlinepeptideshaker.presenter;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.uib.onlinepeptideshaker.managers.RegistrableView;
import com.uib.onlinepeptideshaker.presenter.view.SmallSideBtn;
import com.vaadin.event.ContextClickEvent;
import com.vaadin.event.LayoutEvents;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbsoluteLayout;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.Link;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import sun.security.provider.certpath.Vertex;

/**
 * This class represents the welcome page for Online PeptideShaker
 *
 * @author Yehia Farag
 */
public abstract class WelcomePage extends VerticalLayout implements RegistrableView {

    /**
     * The body layout panel.
     */
    private final HorizontalLayout bodyPanel;
    /**
     * The header layout panel.
     */
    private final VerticalLayout headerPanel;
    /**
     * The header layout container.
     */
    private final HorizontalLayout headerPanelLayout;

    /**
     * The galaxy server connection panel.
     */
    private GalaxyConnectionPanel galaxyInputPanel;
    /**
     * The galaxy server connection panel.
     */
    private SmallSideBtn homeBtn;

    /**
     * Constructor to initialize the layout.
     */
    public WelcomePage() {
        WelcomePage.this.setSizeFull();
        headerPanel = new VerticalLayout();
        headerPanel.setHeight(50, Unit.PIXELS);
        WelcomePage.this.addComponent(headerPanel);
        WelcomePage.this.setComponentAlignment(headerPanel, Alignment.MIDDLE_LEFT);
        WelcomePage.this.setExpandRatio(headerPanel, 10);
        headerPanelLayout = initializeHeaderPanel();
        headerPanel.addComponent(headerPanelLayout);
        headerPanel.setMargin(new MarginInfo(false, false, false, false));
        
        bodyPanel = new HorizontalLayout();
        bodyPanel.setSizeFull();
        bodyPanel.setMargin(new MarginInfo(true, false, false, true));
        
        VerticalLayout bodyContent = new VerticalLayout();
        bodyContent.setSizeFull();
        bodyContent.setMargin(new MarginInfo(true, true, true, true));
        bodyContent.setSpacing(true);
        bodyContent.setStyleName("centerpanel");
        bodyPanel.addComponent(bodyContent);
        bodyPanel.setComponentAlignment(bodyContent, Alignment.TOP_CENTER);
        
        Label welcomeText = new Label();
        welcomeText.setSizeFull();
        welcomeText.setContentMode(ContentMode.HTML);
        welcomeText.setStyleName(ValoTheme.LABEL_NO_MARGIN);
        welcomeText.setValue("<h2 style='font-weight: bold;'>Welcome to PeptideShaker <font size='2'><i>(online version)</i></font></h2> <br/>To start using the system connect to your Galaxy Server");
        bodyContent.addComponent(welcomeText);
        bodyContent.setComponentAlignment(welcomeText, Alignment.TOP_CENTER);
        
        HorizontalLayout bottomLayout = new HorizontalLayout();
        bottomLayout.setSizeFull();
        bottomLayout.setSpacing(true);
        bodyContent.addComponent(bottomLayout);
        bodyContent.setComponentAlignment(bottomLayout, Alignment.TOP_CENTER);
        
        Label connectionStatuesLabel = new Label("Galaxy is<font color='red'>  not connected </font><font size='3' color='red'> &#128528;</font>");
        connectionStatuesLabel.setContentMode(ContentMode.HTML);
        
        connectionStatuesLabel.setHeight(80, Unit.PERCENTAGE);
        connectionStatuesLabel.setWidth(100, Unit.PERCENTAGE);
        connectionStatuesLabel.setStyleName(ValoTheme.LABEL_SMALL);
        connectionStatuesLabel.addStyleName(ValoTheme.LABEL_BOLD);
        connectionStatuesLabel.addStyleName(ValoTheme.LABEL_TINY);
        connectionStatuesLabel.addStyleName("margintop40");
        bottomLayout.addComponent(connectionStatuesLabel);
        bottomLayout.setComponentAlignment(connectionStatuesLabel, Alignment.MIDDLE_CENTER);
        
        HorizontalLayout galaxyConnectionBtn = new HorizontalLayout();
        galaxyConnectionBtn.setSizeFull();
        galaxyConnectionBtn.setSpacing(false);
        galaxyConnectionBtn.setStyleName("btn");
        bottomLayout.addComponent(galaxyConnectionBtn);
        bottomLayout.setComponentAlignment(galaxyConnectionBtn, Alignment.MIDDLE_CENTER);
        HorizontalLayout icon = new HorizontalLayout();
        icon.setSizeFull();
        icon.setStyleName("galaxybtn");
        galaxyConnectionBtn.addComponent(icon);
        galaxyConnectionBtn.setExpandRatio(icon, 18);
        
        Label connectionLabel = new Label("Connect");
        connectionLabel.setStyleName("btntxt");
        galaxyConnectionBtn.addComponent(connectionLabel);
        galaxyConnectionBtn.setComponentAlignment(connectionLabel, Alignment.MIDDLE_CENTER);
        galaxyConnectionBtn.setExpandRatio(connectionLabel, 64);
        VerticalLayout settingBtn = new VerticalLayout();
        settingBtn.addStyleName("settingbtn");
        settingBtn.setSizeFull();
        galaxyConnectionBtn.addComponent(settingBtn);
        galaxyConnectionBtn.setExpandRatio(settingBtn, 18);
        
        galaxyInputPanel = new GalaxyConnectionPanel() {
            @Override
            public void connectedToGalaxy(GalaxyInstance galaxyInstant) {
                galaxyInputPanel.minimizeView();
                connectionLabel.setValue("Disconnect");
                connectionLabel.addStyleName("disconnect");
                connectionStatuesLabel.setValue("Galaxy is <font color='green'>connected </font><font size='3' color='green'> &#128522;</font>");
                systemConnected(galaxyInstant);
            }
            
        };
        WelcomePage.this.addComponent(bodyPanel);
        WelcomePage.this.setComponentAlignment(bodyPanel, Alignment.TOP_CENTER);
        WelcomePage.this.setExpandRatio(bodyPanel, 70);
        WelcomePage.this.setStyleName("minheight275");
        WelcomePage.this.addStyleName("minwidth435");
        
        galaxyConnectionBtn.addLayoutClickListener((LayoutEvents.LayoutClickEvent event) -> {
            if (event.getClickedComponent() != null && event.getClickedComponent() instanceof Label) {
                if (connectionLabel.getValue().equalsIgnoreCase("Disconnect")) {
                    //disconnect from galaxy
                    connectionLabel.setValue("Connect");
                    connectionLabel.removeStyleName("disconnect");
                    connectionStatuesLabel.setValue("Galaxy is<font color='red'>  not connected </font><font size='3' color='red'> &#128528;</font>");
                    systemConnected(null);
                    
                } else {
                    //connect to galaxy
                    galaxyInputPanel.validateAndConnect();                    
                }
            } else if (event.getClickedComponent() != null && event.getClickedComponent() instanceof VerticalLayout) {
                galaxyInputPanel.maximizeView();
            }
        });
        homeBtn = new SmallSideBtn("img/home-o.png");
        homeBtn.setData(WelcomePage.this.getViewId());
        
    }

    /**
     * Initialize the header layout.
     */
    private HorizontalLayout initializeHeaderPanel() {
        HorizontalLayout headerLayoutContainer = new HorizontalLayout();
        headerLayoutContainer.setSpacing(true);
        Image peptideShakerLogoIcon = new Image();
        peptideShakerLogoIcon.setSource(new ThemeResource("favicon.ico"));
        peptideShakerLogoIcon.setHeight(40, Unit.PIXELS);
        peptideShakerLogoIcon.setStyleName("galaxyicon");
        headerLayoutContainer.addComponent(peptideShakerLogoIcon);
        headerLayoutContainer.setComponentAlignment(peptideShakerLogoIcon, Alignment.MIDDLE_LEFT);
        
        Link headerLogoLabel = new Link("PeptideShaker <font>(Online Version)</font>", new ExternalResource(""));
        headerLayoutContainer.addComponent(headerLogoLabel);
        headerLogoLabel.setCaptionAsHtml(true);
        headerLayoutContainer.setComponentAlignment(headerLogoLabel, Alignment.MIDDLE_LEFT);
        headerLogoLabel.setStyleName("headerlogo");
        
        return headerLayoutContainer;
    }
    
    public abstract void systemConnected(GalaxyInstance galaxyInstant);
    
    @Override
    public String getViewId() {
        return WelcomePage.class.getName();
    }
    
    @Override
    public void minimizeView() {
        homeBtn.setSelected(false);
        this.addStyleName("hidepanel");
    }
    
    @Override
    public void maximizeView() {
        homeBtn.setSelected(true);
        this.removeStyleName("hidepanel");
    }
    
    @Override
    public AbsoluteLayout getControlButton() {
        return homeBtn;
    }
    
    @Override
    public VerticalLayout getMainViewComponent() {
        return this;
    }
    
}
