package com.uib.onlinepeptideshaker;

import com.github.wolfie.refresher.Refresher;
import com.github.wolfie.refresher.RefresherUI.DatabaseListener;
import com.uib.onlinepeptideshaker.model.GalaxyServerRefresher;
import com.uib.onlinepeptideshaker.presenter.MainApplicationGUI;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

/**
 * This UI is the application entry point. A UI may either represent a browser
 * window (or tab) or some part of a html page where a Vaadin application is
 * embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is
 * intended to be overridden to add component to the user interface and
 * initialize non-component functionality.
 */
@Theme("customisedvaadintheme")
public class ApplicationUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        this.setSizeFull();
        
         // the Refresher polls automatically
        final Refresher REFRESHER = new Refresher();
        addExtension(REFRESHER);

        Panel applicationContainer = new Panel();
        applicationContainer.setSizeFull();
        setContent(applicationContainer);
        applicationContainer.setContent(new MainApplicationGUI(REFRESHER));

        Page.getCurrent().addBrowserWindowResizeListener((Page.BrowserWindowResizeEvent event) -> {
            for (Window w : UI.getCurrent().getWindows()) {
                w.center();
            }
        });
       
        
        
       

    }

    @WebServlet(urlPatterns = "/*", name = "ApplicationUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = ApplicationUI.class, productionMode = false)
    public static class ApplicationUIServlet extends VaadinServlet {
    }
}
