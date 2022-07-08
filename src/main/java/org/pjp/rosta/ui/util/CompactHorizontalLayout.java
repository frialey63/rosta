package org.pjp.rosta.ui.util;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

public class CompactHorizontalLayout extends HorizontalLayout {
    private static final long serialVersionUID = -1659293782603113030L;

    public CompactHorizontalLayout() {
        super();
        setMargin(false);
        setPadding(false);
    }

    public CompactHorizontalLayout(Component... children) {
        super(children);
        setMargin(false);
        setPadding(false);
    }
}