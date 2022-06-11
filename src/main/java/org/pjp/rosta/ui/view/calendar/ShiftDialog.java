package org.pjp.rosta.ui.view.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.pjp.rosta.model.Shift;
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

    static class ShiftEntry {
        private DayOfWeek dayOfWeek;

        private boolean opener;

        private boolean morning;

        private boolean afternoon;

        public ShiftEntry(DayOfWeek dayOfWeek, boolean opener, boolean morning, boolean afternoon) {
            super();
            this.dayOfWeek = dayOfWeek;
            this.opener = opener;
            this.morning = morning;
            this.afternoon = afternoon;
        }

        public ShiftEntry(DayOfWeek dayOfWeek) {
            this(dayOfWeek, true, true, true);
        }

        public DayOfWeek getDayOfWeek() {
            return dayOfWeek;
        }

        public void setDayOfWeek(DayOfWeek dayOfWeek) {
            this.dayOfWeek = dayOfWeek;
        }

        public boolean isOpener() {
            return opener;
        }

        public void setOpener(boolean opener) {
            this.opener = opener;
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
    }

    static class MyCheckbox extends Checkbox {
        private static final long serialVersionUID = -2439486105879875636L;

        public MyCheckbox(boolean initialValue, boolean enabled, ValueChangeListener<ComponentValueChangeEvent<Checkbox, Boolean>> listener) {
            super(null, listener);
            setValue(initialValue);
            setEnabled(enabled);
        }
    }

    private static final long serialVersionUID = -4941020550845994051L;

    static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    private final List<ShiftDialog.ShiftEntry> entries;

    private final LocalDate date;

    private final String shiftUuid;

    public ShiftDialog(LocalDate date) {
        super();
        this.date = date;
        this.entries = Arrays.asList(DayOfWeek.values()).stream().map(dow -> new ShiftEntry(dow)).collect(Collectors.toList());
        shiftUuid = null;

        setContent(getContent(true));
    }

    public ShiftDialog(Shift shift) {
        super();
        this.date = shift.getFromDate();
        this.entries = shift.getShiftDayStream().map(shiftDay -> new ShiftEntry(shiftDay.getDayOfWeek(), shiftDay.isOpener(), shiftDay.isMorning(), shiftDay.isAfternoon())).collect(Collectors.toList());
        shiftUuid = null;

        setContent(getContent(false));
    }

    public ShiftDialog(LocalDate date, Shift shift) {
        super();
        this.date = date;
        this.entries = shift.getShiftDayStream().map(shiftDay -> new ShiftEntry(shiftDay.getDayOfWeek(), shiftDay.isOpener(), shiftDay.isMorning(), shiftDay.isAfternoon())).collect(Collectors.toList());
        this.shiftUuid = date.equals(shift.getFromDate()) ? shift.getUuid() : null;

        setContent(getContent(true));
    }

    public String getShiftUuid() {
        return shiftUuid;
    }

    public boolean isEdit() {
        return shiftUuid != null;
    }

    public LocalDate getDate() {
        return date;
    }

    public List<ShiftDialog.ShiftEntry> getEntries() {
        return entries;
    }

    private Component getContent(boolean enabled) {
        Grid<ShiftDialog.ShiftEntry> grid = new Grid<>(ShiftDialog.ShiftEntry.class, false);
        grid.setColumnReorderingAllowed(false);
        grid.setAllRowsVisible(true);
        grid.setWidthFull();

        grid.addColumn(ShiftEntry::getDayOfWeek).setHeader("Day");
        grid.addColumn(new ComponentRenderer<>(rostaEntry -> new MyCheckbox(rostaEntry.isOpener(), enabled, l -> rostaEntry.setOpener(l.getValue())))).setHeader("Opener");
        grid.addColumn(new ComponentRenderer<>(rostaEntry -> new MyCheckbox(rostaEntry.isMorning(), enabled, l -> rostaEntry.setMorning(l.getValue())))).setHeader("Morning");
        grid.addColumn(new ComponentRenderer<>(rostaEntry -> new MyCheckbox(rostaEntry.isAfternoon(), enabled, l -> rostaEntry.setAfternoon(l.getValue())))).setHeader("Afternoon");

        grid.addThemeVariants(GridVariant.LUMO_COMPACT);
        grid.addThemeVariants(GridVariant.LUMO_COLUMN_BORDERS);

        grid.setItems(entries);

        Label label = new Label("Effective from: " + date.format(FORMATTER));

        VerticalLayout content = new CompactVerticalLayout(label, grid);
        content.setHorizontalComponentAlignment(Alignment.START, label, grid);

        return content;
    }


}