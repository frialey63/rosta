package org.pjp.rosta.ui.view.calendar;

import java.util.List;
import java.util.stream.Collectors;

import org.pjp.rosta.model.AbsenceDay;
import org.pjp.rosta.model.AbstractDay;
import org.pjp.rosta.model.Holiday;
import org.pjp.rosta.ui.component.CompactHorizontalLayout;

import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;

public class SummaryDialog extends EnhancedDialog {
    private static final long serialVersionUID = -7726616762573407178L;

    private final boolean employee;

    private final List<AbstractDay> days;

    private float dayCounter;

    public SummaryDialog(boolean employee, List<AbstractDay> days) {
        super();
        this.employee = employee;
        this.days = days;

        setContent(getContent());

        setFooter(getFooter());
    }

    private Component getContent() {
        StringBuilder text = new StringBuilder();

        if (employee) {
            List<Holiday> holidays = days.stream().filter(abstractDay -> (abstractDay instanceof Holiday)).map(Holiday.class::cast).sorted().collect(Collectors.toList());
            List<AbsenceDay> absences = days.stream().filter(abstractDay -> (abstractDay instanceof AbsenceDay)).map(AbsenceDay.class::cast).sorted().collect(Collectors.toList());

            dayCounter = 0;

            text.append("Number of holidays is " + holidays.size());
            text.append('\n');
            holidays.forEach(day -> {
                text.append('\t');
                text.append(day.toString());
                text.append('\n');

                dayCounter += day.getPartCount();
            });
            text.append("Total = " + dayCounter + " days");
            text.append('\n');
            text.append('\n');

            dayCounter = 0;

            text.append("Number of absences is " + absences.size());
            text.append('\n');
            absences.forEach(day -> {
                text.append('\t');
                text.append(day.toString());
                text.append('\n');

                dayCounter += day.getPartCount();
            });
            text.append("Total = " + dayCounter + " days");
            text.append('\n');
        } else {
            dayCounter = 0;

            text.append("Number of volunteer days is " + days.size());
            text.append('\n');
            days.forEach(day -> {
                text.append('\t');
                text.append(day.toString());
                text.append('\n');

                dayCounter += day.getPartCount();
            });
            text.append("Total = " + dayCounter + " days");
            text.append('\n');
        }

        TextArea textArea = new TextArea();
        textArea.setValue(text.toString());
        textArea.getStyle().set("font-family", "Courier");

        VerticalLayout content = new VerticalLayout(textArea);
        content.setHorizontalComponentAlignment(Alignment.STRETCH, textArea);

        return content;
    }

    private HorizontalLayout getFooter() {
        Span filler = new Span();

        Button button = new Button("Cancel", e -> SummaryDialog.this.close());

        HorizontalLayout footer = new CompactHorizontalLayout(button);
        footer.setAlignItems(Alignment.STRETCH);
        footer.setVerticalComponentAlignment(Alignment.CENTER, button);
        footer.setFlexGrow(1, filler);

        return footer;
    }


}