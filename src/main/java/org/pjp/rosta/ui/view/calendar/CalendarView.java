package org.pjp.rosta.ui.view.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.pjp.rosta.model.Holiday;
import org.pjp.rosta.security.Session;
import org.pjp.rosta.service.RostaService;
import org.pjp.rosta.service.UserService;
import org.pjp.rosta.ui.view.CompactHorizontalLayout;
import org.pjp.rosta.ui.view.CompactVerticalLayout;
import org.pjp.rosta.ui.view.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.stefan.fullcalendar.DatesRenderedEvent;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.EntryClickedEvent;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.fullcalendar.TimeslotsSelectedEvent;

import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.ClickEvent;
//.import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Calendar of Volunteer Days & Holidays")
@Route(value = "calendar", layout = MainLayout.class)
public class CalendarView extends VerticalLayout implements ComponentEventListener<DatesRenderedEvent> {

    private static class DeleteDialog extends EnhancedDialog {
        private static final long serialVersionUID = 5109473128007800278L;

        private final Entry entry;

        public DeleteDialog(boolean holiday, Entry entry) {
            super();
            this.entry = entry;

            String question = String.format("Do you want to delete this %s?", (holiday ? "Holiday" : "Volunteer Day"));
            setContent(new Span(question));
        }

        public Entry getEntry() {
            return entry;
        }
    }

    private static class CreateDialog extends EnhancedDialog {
        private static final long serialVersionUID = -6123213676333349968L;

        private final boolean holiday;

        private final LocalDate date;

        private final Checkbox morning = new Checkbox("Morning", true);

        private final Checkbox afternoon = new Checkbox("Afternoon", true);

        public CreateDialog(boolean holiday, LocalDate date) {
            super();
            this.holiday = holiday;
            this.date = date;

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

        public VerticalLayout getContent() {
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

        public boolean isAllDay() {
            return morning.getValue() && afternoon.getValue();
        }

        public boolean isMorning() {
            return morning.getValue();
        }

        public boolean isAfternoon() {
            return afternoon.getValue();
        }
    }

    private static final long serialVersionUID = -4423320972580039035L;

    private static final String HOLIDAY_COLOUR = "#ff3333";

    private static final String VOLUNTEER_DAY_COLOUR = "#33ff33";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

    private final FullCalendar calendar = FullCalendarBuilder.create().build();

    private final Button buttonDatePicker = new Button("", VaadinIcon.CALENDAR.create());

    private EnhancedDialog dialog;

    @Autowired
    private UserService userService;

    @Autowired
    private RostaService rostaService;

    public CalendarView() {
        MenuBar menuBar = createMenuBar();

        // Create a new calendar instance and attach it to our layout
        calendar.setFirstDay(DayOfWeek.MONDAY);
        calendar.addTimeslotsSelectedListener(this::onTimeslotsSelectedEvent);
        calendar.addEntryClickedListener(this::onEntryClickedEvent);
        calendar.addDatesRenderedListener(this);

        setSizeFull();

        calendar.setHeightByParent();
        setFlexGrow(1, calendar);

        setHorizontalComponentAlignment(Alignment.STRETCH, menuBar, calendar);
        add(menuBar, calendar);
    }

    private void updateMonthReadout(HasText label, LocalDate intervalStart) {
        String text = intervalStart.format(FORMATTER.withLocale(calendar.getLocale()));
        label.setText(text);
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // simulate the date picker light that we can use in polymer
        DatePicker gotoDate = new DatePicker();
        gotoDate.addValueChangeListener(event1 -> calendar.gotoDate(event1.getValue()));
        gotoDate.getElement().getStyle().set("visibility", "hidden");
        gotoDate.getElement().getStyle().set("position", "fixed");
        gotoDate.setWidth("0px");
        gotoDate.setHeight("0px");
        gotoDate.setWeekNumbersVisible(true);

        buttonDatePicker.addThemeVariants(ButtonVariant.LUMO_TERTIARY_INLINE);
        buttonDatePicker.getElement().appendChild(gotoDate.getElement());
        buttonDatePicker.addClickListener(event -> gotoDate.open());
        buttonDatePicker.setWidthFull();

        menuBar.addItem(VaadinIcon.ANGLE_LEFT.create(), e -> calendar.previous());
        menuBar.addItem(buttonDatePicker);
        menuBar.addItem(VaadinIcon.ANGLE_RIGHT.create(), e -> calendar.next());
        menuBar.addItem("Today", e -> calendar.today());

        return menuBar;
    }

    @Override
    public void onComponentEvent(DatesRenderedEvent event) {
        LocalDate intervalStart = event.getIntervalStart();
        LocalDate intervalEnd = event.getIntervalEnd();

        String username = Session.getUsername();

        updateMonthReadout(buttonDatePicker, intervalStart);

        calendar.removeAllEntries();

        userService.findByName(username).ifPresent(user -> {
            rostaService.getDays(user, intervalStart, intervalEnd).forEach(day -> {
                Entry entry = new Entry();
                entry.setStart(day.getDate());
                entry.setColor((day instanceof Holiday) ? HOLIDAY_COLOUR : VOLUNTEER_DAY_COLOUR);
                entry.setAllDay(true);

                // TODO centre align the text in the entry on the calendar
                if (day.isAllDay()) {
                    entry.setTitle("All Day");
                } else if (day.isMorning()) {
                    entry.setTitle("Morning");
                } else if (day.isAfternoon()) {
                    entry.setTitle("Afternoon");
                }

                calendar.addEntry(entry);
            });
        });
    }

    public void onEntryClickedEvent(EntryClickedEvent event) {
        String username = Session.getUsername();

        userService.findByName(username).ifPresent(user -> {
            dialog = new DeleteDialog(user.isEmployee(), event.getEntry());
            dialog.setHeader("Delete");
            dialog.setFooter(new CompactHorizontalLayout(new Button("Delete", this::onDelete), new Button("Cancel", this::onCancel)));
            dialog.open();
        });
    }

    public void onTimeslotsSelectedEvent(TimeslotsSelectedEvent event) {
        String username = Session.getUsername();

        userService.findByName(username).ifPresent(user -> {
            String header = String.format("Add %s", (user.isEmployee() ? "Holiday" : "Volunteer Day"));

            dialog = new CreateDialog(user.isEmployee(), event.getStartDate());
            dialog.setHeader(header);
            dialog.setFooter(new HorizontalLayout(new Button("Save", this::onCreate), new Button("Cancel", this::onCancel)));
            dialog.open();
        });
    }

    private void onCreate(ClickEvent<Button> event) {
        CreateDialog createDialog = (CreateDialog) dialog;

        Entry entry = new Entry();
        entry.setStart(createDialog.getDate());
        entry.setColor(createDialog.isHoliday() ? HOLIDAY_COLOUR : VOLUNTEER_DAY_COLOUR);
        entry.setAllDay(true);

        // TODO centre align the text in the entry on the calendar
        if (createDialog.isAllDay()) {
            entry.setTitle("All Day");
        } else if (createDialog.isMorning()) {
            entry.setTitle("Morning");
        } else if (createDialog.isAfternoon()) {
            entry.setTitle("Afternoon");
        }

        calendar.addEntry(entry);

        dialog.close();
    }

    private void onDelete(ClickEvent<Button> event) {
        Entry entry = ((DeleteDialog) dialog).getEntry();

        calendar.removeEntry(entry);

        dialog.close();
    }

    private void onCancel(ClickEvent<Button> event) {
        dialog.close();
    }

}
