package org.pjp.rosta.ui.util;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class CompactVerticalLayout extends VerticalLayout {
    private static final long serialVersionUID = 3679921726248121365L;

    public CompactVerticalLayout() {
        super();
        setMargin(false);
        setPadding(false);
    }

    public CompactVerticalLayout(Component... children) {
        super(children);
        setMargin(false);
        setPadding(false);
    }
}