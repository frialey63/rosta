package org.pjp.rosta.ui.view.calendar;

import java.time.LocalDate;

import org.pjp.rosta.model.PartOfDay;
import org.pjp.rosta.ui.view.CompactVerticalLayout;

import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;

class CreateDialog extends EnhancedDialog implements PartOfDay {
    private static final long serialVersionUID = -6123213676333349968L;

    private final boolean holiday;

    private final LocalDate date;

    private final String userUuid;

    private final Checkbox morning = new Checkbox("Morning", true);

    private final Checkbox afternoon = new Checkbox("Afternoon", true);

    public CreateDialog(boolean holiday, LocalDate date, String userUuid) {
        super();
        this.holiday = holiday;
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
        VerticalLayout panel = new CompactVerticalLayout(morning, afternoon);
        panel.setHorizontalComponentAlignment(Alignment.START, morning, afternoon);
        panel.setHeightFull();

        return panel;
    }

    public boolean isHoliday() {
        return holiday;
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