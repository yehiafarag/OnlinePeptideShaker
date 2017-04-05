package com.uib.onlinepeptideshaker;

import com.github.wolfie.refresher.Refresher;
import com.uib.onlinepeptideshaker.presenter.MainApplicationGUI;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinService;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Panel;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;
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

    @WebServlet(urlPatterns = "/*", name = "ApplicationUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = ApplicationUI.class, productionMode = false)
    public static class ApplicationUIServlet extends VaadinServlet {
    }

    private void mimicNelsLogin() {
        // Create a new cookie
        initCookie("SimpleSAMLAuthToken", "_7411f1e6cada8618309cb1e35bf6ddd28b77f8e7ea");
        initCookie("PHPSESSID", "89219cd1f1082162547a7bbfc68b5290");
        initCookie("AuthMemCookie", "_2929cdb96770acd4f7fe1be5b3cb0d81a9e553960d");
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
