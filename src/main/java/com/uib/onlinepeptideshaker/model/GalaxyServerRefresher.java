/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.uib.onlinepeptideshaker.model;

import com.github.wolfie.refresher.Refresher;
import com.vaadin.ui.Notification;

/**
 *
 * @author yfa041
 */
public abstract class GalaxyServerRefresher implements Refresher.RefreshListener {

    @Override
    public void refresh(Refresher source) {
        if (process()) {
            end();
        }
    }
    public abstract boolean  process();

    public abstract void end();

}
