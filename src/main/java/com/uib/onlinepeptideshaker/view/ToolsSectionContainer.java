package com.uib.onlinepeptideshaker.view;

import com.vaadin.ui.VerticalLayout;

/**
 * This class represents the left section that contains the tool buttons
 *
 * @author Yehia Farag
 */
public class ToolsSectionContainer extends VerticalLayout {

    public ToolsSectionContainer() {
//        ToolsSectionContainer.this.setWidth(100,Unit.PERCENTAGE);
//        ToolsSectionContainer.this.setStyleName("frame");
        ToolsSectionContainer.this.setSpacing(true);

        ToolsSectionContainer.this.addComponent(generateBtn());
        ToolsSectionContainer.this.addComponent(generateBtn());
        ToolsSectionContainer.this.addComponent(generateBtn());
        ToolsSectionContainer.this.addComponent(generateBtn());

    }

    private VerticalLayout generateBtn() {
        VerticalLayout vlo = new VerticalLayout();
        vlo.setWidth(100, Unit.PERCENTAGE);
        vlo.setHeight(100, Unit.PIXELS);
        vlo.setStyleName("frame");
        return vlo;
    }

}
