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
import java.io.IOException;
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
//        embeddingLoginPage();

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
        initCookie("SimpleSAMLAuthToken", "_db04fc0bafe87beeb844163a166faac52f833d4b9f");
        initCookie("PHPSESSID", "8cf622a3c6230dc488b3741abeddb6a4");
        initCookie("AuthMemCookie", "_1a6b736f80b4103ab70d78ca324e6981d61a282b5a");

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

    private void embeddingLoginPage() {

        VaadinServletRequest vRequset = ((VaadinServletRequest) VaadinService.getCurrentRequest());

//        Cookie[] cookies = ((VaadinServletRequest) VaadinService.()).;
//        for (Cookie cookie : cookies) {
//            System.err.println("at cookies " + cookie.getName() + "=" + cookie.getValue());
//        }
        ExternalResource prueba = new ExternalResource("https://galaxy-uib.bioinfo.no/");
        BrowserFrame browser = new BrowserFrame("Login Page", prueba);
        browser.setSizeFull();

        Window w = new Window();
        w.setSizeFull();
        w.setModal(true);
        w.addCloseListener(new Window.CloseListener() {
            @Override
            public void windowClose(Window.CloseEvent e) {
                System.out.println("widows are closed");
                Cookie[] cookies = ((VaadinServletRequest) VaadinService.getCurrentRequest()).getCookies();
                for (Cookie cookie : cookies) {
                    System.err.println("at cookies after " + cookie.getName() + "=" + cookie.getValue());
                }
            }

        });
        w.addContextClickListener(new ContextClickEvent.ContextClickListener() {
            @Override
            public void contextClick(ContextClickEvent event) {
                System.out.println("context is clicked");

            }
        });

        this.addWindow(w);
        w.setContent(browser);
        w.setVisible(true);
        try {
            browser.handleConnectorRequest(vRequset, VaadinService.getCurrentResponse(), VaadinServlet.getCurrent().getServletContext().getContextPath());

//         browser.handleConnectorRequest(request, response, DESIGN_ATTR_PLAIN_TEXT)
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(ApplicationUI.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
