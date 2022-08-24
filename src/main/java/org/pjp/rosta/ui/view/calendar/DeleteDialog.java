package org.pjp.rosta.ui.view.calendar;

import org.pjp.rosta.model.AbstractDay;
import org.pjp.rosta.ui.component.CompactVerticalLayout;
import org.vaadin.stefan.fullcalendar.Entry;

import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

class DeleteDialog extends EnhancedDialog {
    private static final long serialVersionUID = 5109473128007800278L;

    private final Entry entry;

    private final String className;

    private final boolean repeated;

    private final Checkbox checkBox = new Checkbox("Also delete all repeat occurrences?");

    public DeleteDialog(Entry entry) {
        super();
        this.entry = entry;
        this.repeated = (entry.getCustomProperty(CalendarView.KEY_REPEAT_UUID) != null);
        this.className = entry.getCustomProperty(CalendarView.KEY_DAY_CLASS);

        String question = String.format("Do you want to delete this %s?", AbstractDay.getDayType(className));

        setContent(getContent(question));
    }

    private Component getContent(String question) {
        Span span = new Span(question);

        checkBox.setEnabled(repeated);

        VerticalLayout vl = new CompactVerticalLayout(span, checkBox);

        return vl;
    }

    public Entry getEntry() {
        return entry;
    }

    public boolean isRepeated() {
        return checkBox.getValue();
    }
}