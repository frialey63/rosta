package org.pjp.rosta.ui.view.calendar;

import java.time.LocalDate;

import org.pjp.rosta.model.DayType;
import org.pjp.rosta.model.PartOfDay;
import org.pjp.rosta.model.Repeat;
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

    private Checkbox morning;

    private Checkbox afternoon;

    private Checkbox evening;

    public CreateDialog(LocalDate date, boolean employee, User user) {
        super();
        this.date = date;
        this.employee = employee;
        this.user = user;

        reconfigure(employee);
    }

    public CreateDialog(LocalDate date, boolean employee) {
        this(date, employee, null);
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
        morning = new Checkbox("Morning", true);
        afternoon = new Checkbox("Afternoon", true);
        evening = new Checkbox("Evening", false);

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

            Select<Repeat> selector = new Select<>();
            selector.setItems(Repeat.values());
            selector.setValue(Repeat.NONE);

            IntegerField count = new IntegerField();
            count.setValue(0);
            count.setHasControls(true);
            count.setMin(0);
            count.setEnabled(false);

            selector.addValueChangeListener(l -> {
                count.setValue(0);

                switch (l.getValue()) {
                case NONE:
                    count.setMax(0);
                    count.setEnabled(false);
                    break;
                case WEEK:
                    count.setMax(52);
                    count.setEnabled(true);
                    break;
                case MONTH:
                    count.setMax(12);
                    count.setEnabled(true);
                    break;
                }
            });

            HorizontalLayout repeatPanel = new CompactHorizontalLayout(label, selector, count);
            repeatPanel.setVerticalComponentAlignment(Alignment.CENTER, label, selector, count);
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
        return employee ? dayType.getValue() : DayType.VOLUNTARY;
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