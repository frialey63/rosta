package org.pjp.rosta.ui.view.event;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.applayout.DrawerToggle;

public class DrawerToggleEvent extends ComponentEvent<DrawerToggle> {

    private static final long serialVersionUID = 3313686966195067409L;

    public DrawerToggleEvent(DrawerToggle source, boolean fromClient) {
        super(source, fromClient);
    }

}
