package com.uib.onlinepeptideshaker;

import com.github.wolfie.refresher.Refresher;
import com.uib.onlinepeptideshaker.presenter.MainApplicationGUI;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.event.ContextClickEvent;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.Cookie;

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

    /**
     * Entry point - This is the initial Vaadin request method initialize
     * components and non-component functionality.
     *
     * @param vaadinRequest Main request object.
     */
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
        mimicNelsLogin();
    }
//

    @Override
    public void detach() {
        super.detach(); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void close() {
        super.close(); //To change body of generated methods, choose Tools | Templates.
    }

    @WebServlet(urlPatterns = "/*", name = "ApplicationUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = ApplicationUI.class, productionMode = false)
    public static class ApplicationUIServlet extends VaadinServlet {
    }

    private void mimicNelsLogin() {
        // Create a new cookie
        initCookie("SimpleSAMLAuthToken", "_681b5c369f6eee2e70c46b56c01f54a6eecfba2e76");
        initCookie("PHPSESSID", "4c2b70025413d424b0de7b5dcabbee3a");
        initCookie("AuthMemCookie", "_640a4dd2577495291e6e8477227d2a275feb4a755f");
    }

    private void initCookie(String name, String value) {
        // Create a new cookie
        Cookie myCookie = new Cookie(name, value);
// Make cookie expire in 2 minutes
        myCookie.setMaxAge(120);
// Set the cookie path.
        myCookie.setPath(VaadinService.getCurrentRequest().getContextPath());
// Save cookie
        VaadinService.getCurrentResponse().addCookie(myCookie);
    }

}
