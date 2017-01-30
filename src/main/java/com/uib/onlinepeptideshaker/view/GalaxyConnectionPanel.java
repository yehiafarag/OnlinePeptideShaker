package com.uib.onlinepeptideshaker.view;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.github.jmchilton.blend4j.galaxy.GalaxyInstanceFactory;
import com.uib.onlinepeptideshaker.managers.RegistrableView;
import com.uib.onlinepeptideshaker.presenter.MainApplicationGUI;
import com.vaadin.server.ThemeResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Image;
import com.vaadin.ui.Label;
import com.vaadin.ui.PasswordField;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class represents the galaxy server connection panel that contains
 * user-input data and link to galaxy
 *
 * @author Yehia Farag
 */
public abstract class GalaxyConnectionPanel extends VerticalLayout implements RegistrableView, Button.ClickListener {

    /**
     * Container for galaxy connection input fields.
     */
    private HorizontalLayout galaxyLinkContainer;
    /**
     * Tab-sheet for galaxy connection user input fields.
     */
    private TabSheet inputTabSheet;
    /**
     * Galaxy server is connected.
     */
    private boolean galaxyConnected;
    /**
     * Galaxy connection label.
     */
    private Label connectionLabel;
    /**
     * Galaxy server user email.
     */
    private TextField userEmail;
    /**
     * Galaxy server user password.
     */
    private PasswordField password;
    /**
     * Galaxy server web address.
     */
    private TextField galaxyLink;
    /**
     * Galaxy server user API key.
     */
    private TextField APIKey;
    /**
     * Minimized layout view.
     */
    private final VerticalLayout minimizedLayout;
    /**
     * Maximized layout view.
     */
    private final VerticalLayout maximizedLayout;
    /**
     * Email format validation regex.
     */
    private final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    /**
     * Main galaxy server instance.
     */
    private GalaxyInstance galaxyInstance;

    @Override
    public String getViewId() {
        return this.getClass().getName();
    }

    @Override
    public void minimizeView() {
        this.addStyleName("minimizegalaxypanel");
        this.removeStyleName("maxmizegalaxypanel");
        this.minimizedLayout.setVisible(true);
        this.maximizedLayout.setVisible(false);
    }

    @Override
    public void maximizeView() {
        this.addStyleName("maxmizegalaxypanel");
        this.removeStyleName("minimizegalaxypanel");
        this.minimizedLayout.setVisible(false);
        this.maximizedLayout.setVisible(true);

    }

    @Override
    public VerticalLayout getMinimizeComponent() {
        return minimizedLayout;
    }

    /**
     * Constructor to initialize the main variable.
     */
    public GalaxyConnectionPanel() {
        GalaxyConnectionPanel.this.setStyleName("frame");
        GalaxyConnectionPanel.this.addStyleName("maxmizegalaxypanel");
        GalaxyConnectionPanel.this.setWidth(100, Unit.PERCENTAGE);
        GalaxyConnectionPanel.this.setHeight(300, Unit.PIXELS);
        maximizedLayout = this.initializeUserInputPanel();
        GalaxyConnectionPanel.this.addComponent(maximizedLayout);
        minimizedLayout = new VerticalLayout();

        Image galaxyIcon = new Image();
        galaxyIcon.setSource(new ThemeResource("img/galaxyLogo.png"));
        galaxyIcon.setHeight(25, Unit.PIXELS);
        galaxyIcon.setStyleName("galaxyicon");
        minimizedLayout.addComponent(galaxyIcon);

        GalaxyConnectionPanel.this.addComponent(minimizedLayout);
        this.minimizedLayout.setVisible(false);
        this.maximizedLayout.setVisible(true);
    }

    /**
     * Initialize galaxy input data layout.
     */
    private VerticalLayout initializeUserInputPanel() {
        VerticalLayout userInputPanelLayout = new VerticalLayout();
        userInputPanelLayout.setSizeFull();
        userInputPanelLayout.setMargin(new MarginInfo(true, false, false, true));

        galaxyLinkContainer = new HorizontalLayout();
        galaxyLinkContainer.setSpacing(true);

        Image galaxyIcon = new Image();
        galaxyIcon.setSource(new ThemeResource("img/galaxyLogo.png"));
        galaxyIcon.setHeight(25, Unit.PIXELS);
        galaxyIcon.setStyleName("galaxyicon");
        galaxyLinkContainer.addComponent(galaxyIcon);

        Label galaxyLinkCaption = new Label("Link to galaxy server:");
        galaxyLinkCaption.setStyleName(ValoTheme.LABEL_BOLD);
        galaxyLinkCaption.addStyleName("v-textfield-textfileldborder");
        galaxyLinkContainer.addComponent(galaxyLinkCaption);

        galaxyLink = new TextField();
        galaxyLink.setHeight(100, Unit.PERCENTAGE);
        galaxyLink.setWidth(100, Unit.PERCENTAGE);
        galaxyLink.addStyleName(ValoTheme.TEXTFIELD_SMALL);
        galaxyLink.addStyleName(ValoTheme.TEXTFIELD_TINY);
        galaxyLinkContainer.addComponent(galaxyLink);

        userInputPanelLayout.addComponent(galaxyLinkContainer);
        userInputPanelLayout.setExpandRatio(galaxyLinkContainer, 20);

        galaxyLink.setValue("http://129.177.123.195/");//https://usegalaxyp.org

        inputTabSheet = new TabSheet();
        inputTabSheet.setWidth(400, Unit.PIXELS);
        userInputPanelLayout.addComponent(inputTabSheet);
        userInputPanelLayout.setExpandRatio(inputTabSheet, 60);

        HorizontalLayout userInputPanel = new HorizontalLayout();
        userInputPanel.setWidth(100, Unit.PERCENTAGE);
        userInputPanel.setHeight(100, Unit.PERCENTAGE);
        inputTabSheet.addTab(userInputPanel, "Email & Password");

        userEmail = new TextField();
        userEmail.setImmediate(true);

        userEmail.setRequiredError("Not vaild e-mail address");
        userEmail.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        userEmail.addStyleName(ValoTheme.TEXTFIELD_TINY);
        userEmail.addStyleName("nomargin");
        userEmail.setCaption("E-mail");
        userEmail.setInputPrompt("Galaxy e-mail");
        userInputPanel.addComponent(userEmail);
        userEmail.setWidth(90, Unit.PERCENTAGE);
        userEmail.setHeight(80, Unit.PERCENTAGE);
        userInputPanel.setComponentAlignment(userEmail, Alignment.MIDDLE_CENTER);

        password = new PasswordField();
        password.setImmediate(true);

        password.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        password.addStyleName(ValoTheme.TEXTFIELD_TINY);
        password.addStyleName("nomargin");
        password.setCaption("Password");
        password.setRequiredError("Password can not be empty");
        password.setWidth(90, Unit.PERCENTAGE);
        password.setHeight(80, Unit.PERCENTAGE);
        userInputPanel.addComponent(password);
        userInputPanel.setComponentAlignment(password, Alignment.MIDDLE_CENTER);

        HorizontalLayout userAPIKeyPanel = new HorizontalLayout();
        userAPIKeyPanel.setWidth(100, Unit.PERCENTAGE);
        userAPIKeyPanel.setHeight(100, Unit.PERCENTAGE);
        inputTabSheet.addTab(userAPIKeyPanel, "API Key");
        inputTabSheet.setSelectedTab(userAPIKeyPanel);

        APIKey = new TextField("");//71821f0c14cf63a2609f59d821bc1df3       //  6abed6a0b5021096631350a0b89c5155----61062cd3acb2433c1e1ed66d6560357f
        APIKey.setImmediate(true);
        APIKey.setRequiredError("Not vaild API Key");
        APIKey.setStyleName(ValoTheme.TEXTFIELD_SMALL);
        APIKey.addStyleName(ValoTheme.TEXTFIELD_TINY);
        APIKey.addStyleName("nomargin");
        APIKey.setInputPrompt("Galaxy API Key");
        APIKey.setValue("6abed6a0b5021096631350a0b89c5155");//6abed6a0b5021096631350a0b89c5155   ----61062cd3acb2433c1e1ed66d6560357f
        userAPIKeyPanel.addComponent(APIKey);
        APIKey.setWidth(100, Unit.PERCENTAGE);
        APIKey.setHeight(80, Unit.PERCENTAGE);
        userAPIKeyPanel.setComponentAlignment(APIKey, Alignment.MIDDLE_CENTER);

        HorizontalLayout connectionPanel = new HorizontalLayout();
        connectionPanel.setWidth(400, Unit.PIXELS);
        connectionPanel.setHeight(100, Unit.PERCENTAGE);
        userInputPanelLayout.addComponent(connectionPanel);
        userInputPanelLayout.setExpandRatio(connectionPanel, 20);

        connectionLabel = new Label("Galaxy is not connected <font size=\"3\" color=\"red\"> &#128528;</font>");
        connectionLabel.setContentMode(ContentMode.HTML);
        connectionLabel.setStyleName(ValoTheme.LABEL_SMALL);
        connectionLabel.addStyleName(ValoTheme.LABEL_BOLD);
        connectionLabel.addStyleName(ValoTheme.LABEL_TINY);
        connectionPanel.addComponent(connectionLabel);
        connectionPanel.setComponentAlignment(connectionLabel, Alignment.TOP_LEFT);

        Button connectBtn = new Button("Connect");
        connectBtn.setStyleName(ValoTheme.BUTTON_TINY);
        connectBtn.addStyleName(ValoTheme.BUTTON_SMALL);
        connectBtn.setWidth(100, Unit.PIXELS);
        connectBtn.setHeight(30, Unit.PIXELS);
        connectionPanel.addComponent(connectBtn);
        connectBtn.addClickListener(GalaxyConnectionPanel.this);
        connectionPanel.setComponentAlignment(connectBtn, Alignment.TOP_RIGHT);

        return userInputPanelLayout;
    }

    @Override
    public void buttonClick(Button.ClickEvent event) {
        if (galaxyConnected) {
            event.getButton().setCaption("Connect");
            galaxyConnected = false;
            connectionLabel.setValue("Galaxy is not connected <font size=\"3\" color=\"red\"> &#128528;</font>");
            return;

        } else {
            galaxyConnected = connectToGalaxy();
            if (galaxyConnected) {
                connectionLabel.setValue("Galaxy is connected <font size=\"3\" color=\"green\"> &#128522;</font>");
                event.getButton().setCaption("Disconnect");

            }

        }
        galaxyLinkContainer.setEnabled(!galaxyConnected);
        inputTabSheet.setEnabled(!galaxyConnected);
        this.connectedToGalaxy(galaxyInstance);

    }

    private boolean connectToGalaxy() {

        if (inputTabSheet.getTabPosition(inputTabSheet.getTab(inputTabSheet.getSelectedTab())) == 0) {
            userEmail.setRequired(true);
            password.setRequired(true);
            galaxyLink.setRequired(true);

            try {
                userEmail.validate();
                password.validate();
                galaxyLink.validate();
                if (!galaxyLink.isValid() || !userEmail.isValid() || !password.isValid()) {
                    userEmail.setRequired(!userEmail.isValid());
                    password.setRequired(!password.isValid());
                    galaxyLink.setRequired(!galaxyLink.isValid());
                    return false;
                } else {
                    Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(userEmail.getValue());
                    if (!matcher.find()) {
                        userEmail.clear();
                        userEmail.setInputPrompt("Invalid email address");
                        userEmail.validate();
                        return false;
                    }

                }
            } catch (Exception e) {
                userEmail.setRequired(!userEmail.isValid());
                password.setRequired(!password.isValid());
                galaxyLink.setRequired(!galaxyLink.isValid());
                return false;
            }
            galaxyInstance = GalaxyInstanceFactory.getFromCredentials(galaxyLink.getValue(), userEmail.getValue(), password.getValue());
        } else {
            galaxyLink.setRequired(true);
            APIKey.setRequired(true);

            try {
                APIKey.validate();
                galaxyLink.validate();
                if (!galaxyLink.isValid() || !APIKey.isValid()) {
                    APIKey.setRequired(!APIKey.isValid());
                    galaxyLink.setRequired(!galaxyLink.isValid());
                    return false;
                }
            } catch (Exception e) {
                APIKey.setRequired(!APIKey.isValid());
                galaxyLink.setRequired(!galaxyLink.isValid());
                return false;

            }

            galaxyInstance = GalaxyInstanceFactory.get(galaxyLink.getValue(), APIKey.getValue());

        }

        try {
            galaxyInstance.getConfigurationClient().getRawConfiguration();
        } catch (Exception e) {
            connectionLabel.setValue("<font color='red'>Galaxy is not connected, check input data <font size='3' color='red'>&#128530;</font></font>");
            return false;
        }
        APIKey.setRequired(false);
        userEmail.setRequired(false);
        password.setRequired(false);
        galaxyLink.setRequired(false);
        return true;
    }

    /**
     * The server connected to galaxy
     *
     * @param galaxyInstant galaxy server instance
     */
    public abstract void connectedToGalaxy(GalaxyInstance galaxyInstant);

}
