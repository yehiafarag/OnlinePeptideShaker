package com.uib.onlinepeptideshaker.presenter.view;

import com.vaadin.event.LayoutEvents;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;

/**
 * This class represents deletable label layout
 *
 * @author Yehia Farag
 */
public abstract class DeletableLabel extends HorizontalLayout implements LayoutEvents.LayoutClickListener {

    private final String id;
    public DeletableLabel(String captionText,String url,String id) {
        this.id=id;
        DeletableLabel.this.setSizeFull();
        DeletableLabel.this.setStyleName("deletablelabel");
        Label caption = new Label("<a target='_blank' href='"+url+"' download='"+captionText+"'>"+captionText+"</a>");
        caption.setContentMode(ContentMode.HTML);
        caption.setSizeFull();
        DeletableLabel.this.addComponent(caption);
        DeletableLabel.this.setExpandRatio(caption, 80);

        VerticalLayout deleteBtn = new VerticalLayout();
        deleteBtn.setStyleName("closebtn");
        deleteBtn.setSizeFull();
        DeletableLabel.this.addComponent(deleteBtn);
        DeletableLabel.this.setExpandRatio(deleteBtn, 20);
        DeletableLabel.this.setComponentAlignment(deleteBtn, Alignment.MIDDLE_RIGHT);
        deleteBtn.addLayoutClickListener(DeletableLabel.this);
        deleteBtn.setEnabled(false);

    }

    @Override
    public void layoutClick(LayoutEvents.LayoutClickEvent event) {
        deleteAction(this.id);
        
    }
    public abstract void deleteAction(String id);

}
