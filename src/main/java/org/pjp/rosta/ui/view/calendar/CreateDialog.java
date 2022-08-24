package org.pjp.rosta.ui.view.calendar;

import java.time.LocalDate;

import org.pjp.rosta.bean.Repeater;
import org.pjp.rosta.bean.Repeater.RepeatType;
import org.pjp.rosta.model.DayType;
import org.pjp.rosta.model.PartOfDay;
import org.pjp.rosta.model.User;
import org.pjp.rosta.ui.component.CompactHorizontalLayout;
import org.pjp.rosta.ui.component.CompactVerticalLayout;

import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;

class CreateDialog extends EnhancedDialog implements PartOfDay {

    private static final long serialVersionUID = -6123213676333349968L;

    private final LocalDate date;

    private boolean employee;

    private User user;

    private final RadioButtonGroup<DayType> dayType = new RadioButtonGroup<>();

    private final Checkbox morning = new Checkbox("Morning", true);
    private final Checkbox afternoon = new Checkbox("Afternoon", true);
    private final Checkbox evening = new Checkbox("Evening", false);

    private final Select<RepeatType> repType = new Select<>();
    private final IntegerField repCount = new IntegerField();

    public CreateDialog(LocalDate date, User user) {
        this(date, user.isEmployee());
        this.user = user;
    }

    public CreateDialog(LocalDate date, boolean employee) {
        super();
        this.date = date;
        this.employee = employee;

        reconfigure(employee);
    }

    public void reconfigure(boolean employee) {
        this.employee = employee;

        setContent(getContent());

        addCheckboxListeners(employee);
    }

    private void addCheckboxListeners(boolean employee) {
        if (employee) {
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
        } else {
            morning.addValueChangeListener(l -> {
                if (!l.getValue() && !afternoon.getValue() && !evening.getValue()) {
                    afternoon.setValue(true);
                    evening.setValue(true);
                }
            });

            afternoon.addValueChangeListener(l -> {
                if (!l.getValue() && !morning.getValue() && !evening.getValue()) {
                    morning.setValue(true);
                    evening.setValue(true);
                }
            });

            evening.addValueChangeListener(l -> {
                if (!l.getValue() && !morning.getValue() && !afternoon.getValue()) {
                    morning.setValue(true);
                    afternoon.setValue(true);
                }
            });
        }
    }

    private Component getContent() {
        morning.setValue(true);
        afternoon.setValue(true);
        evening.setValue(false);

        VerticalLayout subPanel = new CompactVerticalLayout(morning, afternoon);
        subPanel.setHorizontalComponentAlignment(Alignment.START, morning, afternoon);
        subPanel.setHeightFull();

        VerticalLayout content;

        if (employee) {
            dayType.setItems(DayType.HOLIDAY, DayType.ABSENCE);
            dayType.setValue(DayType.HOLIDAY);

            Hr separator = new Hr();
            separator.setWidthFull();

            content = new CompactVerticalLayout(dayType, separator, subPanel);
        } else {
            subPanel.add(evening);
            subPanel.setHorizontalComponentAlignment(Alignment.START, evening);

            Label label = new Label("Repeat:");

            repType.setItems(RepeatType.values());
            repType.setValue(RepeatType.NONE);

            repCount.setValue(0);
            repCount.setHasControls(true);
            repCount.setMin(0);
            repCount.setEnabled(false);

            repType.addValueChangeListener(l -> {
                repCount.setValue(0);

                switch (l.getValue()) {
                case NONE:
                    repCount.setMax(0);
                    repCount.setEnabled(false);
                    break;
                case DAILY:
                    repCount.setMax(14);
                    repCount.setEnabled(true);
                    break;
                case WEEKLY:
                    repCount.setMax(26);
                    repCount.setEnabled(true);
                    break;
                case MONTHLY:
                    repCount.setMax(12);
                    repCount.setEnabled(true);
                    break;
                }
            });

            HorizontalLayout repeatPanel = new CompactHorizontalLayout(label, repType, repCount);
            repeatPanel.setVerticalComponentAlignment(Alignment.CENTER, label, repType, repCount);
            subPanel.add(repeatPanel);

            content = subPanel;
        }

        return content;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isEmployee() {
        return employee;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public DayType getDayType() {
        return employee ? dayType.getValue() : DayType.VOLUNTEER;
    }

    public Repeater getRepeater() {
        if (employee) {
            return new Repeater(RepeatType.NONE, 0);
        }

        return new Repeater(repType.getValue(), repCount.getValue());
    }

    @Override
    public boolean isMorning() {
        return morning.getValue();
    }

    @Override
    public boolean isAfternoon() {
        return afternoon.getValue();
    }

    @Override
    public Boolean isEvening() {
        return employee ? null : evening.getValue();
    }

    @Override
    public boolean isAllDay() {
        boolean allDay = isMorning() && isAfternoon();

        if (!employee) {
            allDay = allDay && isEvening();
        }

        return allDay;
    }

}