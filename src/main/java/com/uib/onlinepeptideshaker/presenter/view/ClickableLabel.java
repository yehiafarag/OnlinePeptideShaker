package com.uib.onlinepeptideshaker.presenter.view;

import com.vaadin.event.LayoutEvents;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * This class represents deletable label layout
 *
 * @author Yehia Farag
 */
public abstract class ClickableLabel extends HorizontalLayout implements LayoutEvents.LayoutClickListener {

    public void setState(boolean busy) {
        if (busy) {
            ClickableLabel.this.addStyleName("inprogress");
        } else {
            ClickableLabel.this.removeStyleName("inprogress");
        }

    }
    private final String id;

    public ClickableLabel(String captionText, String url, String id) {
        this.id = id;
        ClickableLabel.this.setSizeFull();
        ClickableLabel.this.setStyleName("deletablelabel");
        String linkValue;
        if (url != null) {
            linkValue = "<a target='_blank' href='" + url + "' download='" + captionText + "'>" + captionText + "</a>";
        } else {
            linkValue = captionText;
            ClickableLabel.this.addLayoutClickListener(ClickableLabel.this);
        }
        Label caption = new Label(linkValue);
        caption.setContentMode(ContentMode.HTML);
        caption.setSizeFull();
        ClickableLabel.this.addComponent(caption);
        ClickableLabel.this.setExpandRatio(caption, 80);

        VerticalLayout deleteBtn = new VerticalLayout();
        deleteBtn.setStyleName("closebtn");
        deleteBtn.setSizeFull();
//        ClickableLabel.this.addComponent(deleteBtn);
//        ClickableLabel.this.setExpandRatio(deleteBtn, 20);
//        ClickableLabel.this.setComponentAlignment(deleteBtn, Alignment.MIDDLE_RIGHT);
//        
        deleteBtn.setEnabled(false);

    }

    @Override
    public void layoutClick(LayoutEvents.LayoutClickEvent event) {
        performAction(this.id);

    }

    public abstract void performAction(String id);

}
