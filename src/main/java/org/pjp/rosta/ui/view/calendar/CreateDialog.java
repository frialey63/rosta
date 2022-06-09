package org.pjp.rosta.ui.view.calendar;

import java.time.LocalDate;

import org.pjp.rosta.model.DayType;
import org.pjp.rosta.model.PartOfDay;
import org.pjp.rosta.ui.view.CompactVerticalLayout;

import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;

class CreateDialog extends EnhancedDialog implements PartOfDay {
    private static final long serialVersionUID = -6123213676333349968L;

    private final boolean employee;

    private final LocalDate date;

    private final String userUuid;

    private final RadioButtonGroup<DayType> dayType = new RadioButtonGroup<>();

    private final Checkbox morning = new Checkbox("Morning", true);

    private final Checkbox afternoon = new Checkbox("Afternoon", true);

    public CreateDialog(boolean employee, LocalDate date, String userUuid) {
        super();
        this.employee = employee;
        this.date = date;
        this.userUuid = userUuid;

        morning.addValueChangeListener(l -> {
            if (!l.getValue() && !afternoon.getValue()) {
                afternoon.setValue(true);
            }
        });

        afternoon.addValueChangeListener(l -> {
            if (!l.getValue() && !morning.getValue()) {
                morning.setValue(true);
            }
        });

        setContent(getContent());
    }

    public Component getContent() {
        VerticalLayout subPanel = new CompactVerticalLayout(morning, afternoon);
        subPanel.setHorizontalComponentAlignment(Alignment.START, morning, afternoon);
        subPanel.setHeightFull();

        VerticalLayout panel;

        if (employee) {
            dayType.setItems(DayType.Holiday, DayType.Absence);
            dayType.setValue(DayType.Holiday);

            Hr separator = new Hr();
            separator.setWidthFull();

            panel = new CompactVerticalLayout(dayType, separator, subPanel);
        } else {
            panel = subPanel;
        }

        return panel;
    }

    public DayType getDayType() {
        return employee ? dayType.getValue() : DayType.VolunteerDay;
    }

    public boolean isEmployee() {
        return employee;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isMorning() {
        return morning.getValue();
    }

    public boolean isAfternoon() {
        return afternoon.getValue();
    }

    public String getUserUuid() {
        return userUuid;
    }
}