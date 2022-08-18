package org.pjp.rosta.ui.view.calendar;

import org.pjp.rosta.model.AbstractDay;

import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;

class ConflictDialog extends EnhancedDialog {
    private static final long serialVersionUID = -5670166565727831285L;

    public ConflictDialog(AbstractDay day) {
        super();

        String className = day.getClass().getName();
        String existingStr = className.contains("Volunteer") ? "voluntary" : "holiday / absence";

        String message = String.format("Unable to add %s because it overlaps an existing %s on this day!", AbstractDay.getDayType(className), existingStr);
        setContent(getContent(message));
    }

    private Component getContent(String message) {
        return new Span(message);
    }

}