package org.pjp.rosta.ui.view.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.pjp.rosta.ui.view.CompactVerticalLayout;

import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;

class ShiftDialog extends EnhancedDialog {

    public static class ShiftEntry {
        private DayOfWeek dayOfWeek;

        private boolean morning;

        private boolean afternoon;

        public ShiftEntry(DayOfWeek dayOfWeek, boolean morning, boolean afternoon) {
            super();
            this.dayOfWeek = dayOfWeek;
            this.morning = morning;
            this.afternoon = afternoon;
        }

        public DayOfWeek getDayOfWeek() {
            return dayOfWeek;
        }

        public void setDayOfWeek(DayOfWeek dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
        }

        public boolean isMorning() {
            return morning;
        }

        public void setMorning(boolean morning) {
            this.morning = morning;
        }

        public boolean isAfternoon() {
            return afternoon;
        }

        public void setAfternoon(boolean afternoon) {
            this.afternoon = afternoon;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ShiftEntry [dayOfWeek=");
            builder.append(dayOfWeek);
            builder.append(", morning=");
            builder.append(morning);
            builder.append(", afternoon=");
            builder.append(afternoon);
            builder.append("]");
            return builder.toString();
        }
    }

    private static class MyCheckbox extends Checkbox {
        private static final long serialVersionUID = -2439486105879875636L;

        public MyCheckbox(boolean initialValue, ValueChangeListener<ComponentValueChangeEvent<Checkbox, Boolean>> listener) {
            super(null, listener);
            this.setValue(initialValue);
        }

    }

    private static final long serialVersionUID = -4941020550845994051L;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    private final List<ShiftDialog.ShiftEntry> entries = Arrays.asList(DayOfWeek.values()).stream().map(dow -> new ShiftEntry(dow, true, true)).collect(Collectors.toList());

    private final LocalDate date;

    public ShiftDialog(LocalDate date) {
        super();
        this.date = date;

        setContent(getContent());
    }

    public LocalDate getDate() {
        return date;
    }

    public List<ShiftDialog.ShiftEntry> getEntries() {
        return entries;
    }

    private Component getContent() {
        Grid<ShiftDialog.ShiftEntry> grid = new Grid<>(ShiftDialog.ShiftEntry.class, false);
        grid.setColumnReorderingAllowed(false);
        grid.setAllRowsVisible(true);
        grid.setWidth("30em");

        grid.addColumn(ShiftEntry::getDayOfWeek).setHeader("Day");
        grid.addColumn(new ComponentRenderer<>(rostaEntry -> new MyCheckbox(rostaEntry.isMorning(), l -> rostaEntry.setMorning(l.getValue())))).setHeader("Morning");
        grid.addColumn(new ComponentRenderer<>(rostaEntry -> new MyCheckbox(rostaEntry.isAfternoon(), l -> rostaEntry.setAfternoon(l.getValue())))).setHeader("Afternoon");

        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);

        grid.setItems(entries);

        Label label = new Label("Effective from: " + date.format(FORMATTER));

        VerticalLayout vl = new CompactVerticalLayout(label, grid);
        vl.setHorizontalComponentAlignment(Alignment.START, label, grid);

        return vl;
    }


}