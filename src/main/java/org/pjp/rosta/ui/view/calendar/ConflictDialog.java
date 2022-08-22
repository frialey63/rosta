package org.pjp.rosta.ui.view.calendar;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.pjp.rosta.model.DayType;

import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;

class ConflictDialog extends EnhancedDialog {
    private static final long serialVersionUID = -5670166565727831285L;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("EEEE dd MMMM");

    public ConflictDialog(DayType dayType, LocalDate conflictDate) {
        super();

        String existingStr = (dayType == DayType.VOLUNTEER) ? "volunteer day" : "holiday / absence";

        String message = String.format("Unable to add %s because it overlaps an existing %s on %s", dayType, existingStr, FORMATTER.format(conflictDate));
        setContent(getContent(message));
    }

    private Component getContent(String message) {
        return new Span(message);
    }

}