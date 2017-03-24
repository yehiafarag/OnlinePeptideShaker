package com.uib.onlinepeptideshaker.listeners;

import com.github.jmchilton.blend4j.galaxy.GalaxyInstance;
import com.vaadin.server.VaadinSession;
import java.io.File;
import java.util.Enumeration;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * This class responsible for cleaning folders after user session expire
 *
 * @author Yehia Farag
 */
public class VaadinSessionListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent hse) {
        System.out.println("at welcome to new session");
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent hse) {
        GalaxyInstance galaxy = (GalaxyInstance)hse.getSession().getAttribute("currentGalaxy");

        File userFolder = new File(galaxy.getApiKey()+"");
        if (userFolder.exists()) {
            for (File tFile : userFolder.listFiles()) {
                tFile.delete();
            }
        }
        boolean cleaned = userFolder.delete(); 
//       galaxy.getHistoriesClient().deleteHistoryRequest(hse.getSession().getAttribute("tempHistoryID")+"");
        System.err.println("at session is ready to distroy ..Good bye...folder ("+userFolder.getName()+" are cleaned ("+cleaned+") and folder exist ("+userFolder.exists()+")");

    }

}
