package org.pjp.rosta.ui.view.calendar;

import org.pjp.rosta.model.AbstractDay;
import org.vaadin.stefan.fullcalendar.Entry;

import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Span;

class DeleteDialog extends EnhancedDialog {
    private static final long serialVersionUID = 5109473128007800278L;

    private final Entry entry;

    public DeleteDialog(Entry entry) {
        super();
        this.entry = entry;

        String className = entry.getCustomProperty(CalendarView.KEY_DAY_CLASS);

        String question = String.format("Do you want to delete this %s?", AbstractDay.getDayType(className));
        setContent(getContent(question));
    }

    // TODO deletion of volunteer days with repeat

    private Component getContent(String question) {
        return new Span(question);
    }

    public Entry getEntry() {
        return entry;
    }
}