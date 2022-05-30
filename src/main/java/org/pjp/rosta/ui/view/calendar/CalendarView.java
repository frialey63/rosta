package org.pjp.rosta.ui.view.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.pjp.rosta.model.AbstractDay;
import org.pjp.rosta.model.Holiday;
import org.pjp.rosta.model.PartOfDay;
import org.pjp.rosta.security.Session;
import org.pjp.rosta.service.RostaService;
import org.pjp.rosta.service.UserService;
import org.pjp.rosta.ui.view.CompactHorizontalLayout;
import org.pjp.rosta.ui.view.CompactVerticalLayout;
import org.pjp.rosta.ui.view.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static class CreateDialog extends EnhancedDialog implements PartOfDay {
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

    private static final long serialVersionUID = -4423320972580039035L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarView.class);

    private static final String HOLIDAY_COLOUR = "#ff3333";

    private static final String VOLUNTEER_DAY_COLOUR = "#33ff33";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

    private static final String KEY_UUID = "uuid";

    private static String getTitle(PartOfDay partOfDay) {
        if (partOfDay.isMorning() && partOfDay.isAfternoon()) {
            return "All Day";
        } else if (partOfDay.isMorning()) {
            return "Morning";
        } else if (partOfDay.isAfternoon()) {
            return "Afternoon";
        }

        throw new IllegalStateException();
    }

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

    private void updateMonthReadout(HasText label, LocalDate date) {
        String text = date.format(FORMATTER.withLocale(calendar.getLocale()));
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
        LocalDate start = event.getStart();
        LocalDate end = event.getEnd();

        LOGGER.info("start = {}, end = {}", start, end);

        String username = Session.getUsername();

        updateMonthReadout(buttonDatePicker, start);

        calendar.removeAllEntries();

        userService.findByName(username).ifPresent(user -> {
            rostaService.getDays(user, start, end).forEach(day -> {
                Entry entry = new Entry();
                entry.setCustomProperty(KEY_UUID, day.getUuid());
                entry.setStart(day.getDate());
                entry.setColor((day instanceof Holiday) ? HOLIDAY_COLOUR : VOLUNTEER_DAY_COLOUR);
                entry.setAllDay(true);
                entry.setTitle(getTitle(day));	// TODO centre align the text in the entry on the calendar

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

            dialog = new CreateDialog(user.isEmployee(), event.getStartDate(), user.getUuid());
            dialog.setHeader(header);
            dialog.setFooter(new HorizontalLayout(new Button("Save", this::onCreate), new Button("Cancel", this::onCancel)));
            dialog.open();
        });
    }

    private void onCreate(ClickEvent<Button> event) {
        CreateDialog createDialog = (CreateDialog) dialog;

        LocalDate date = createDialog.getDate();
        boolean holiday = createDialog.isHoliday();

        Entry entry = new Entry();
        entry.setStart(date);
        entry.setColor(holiday ? HOLIDAY_COLOUR : VOLUNTEER_DAY_COLOUR);
        entry.setAllDay(true);
        entry.setTitle(getTitle(createDialog)); 	// TODO centre align the text in the entry on the calendar

        AbstractDay day = AbstractDay.createDay(holiday, date, createDialog, createDialog.getUserUuid());

        try {
            rostaService.saveDay(day);
            calendar.addEntry(entry);
        } finally {
            dialog.close();
        }
    }

    private void onDelete(ClickEvent<Button> event) {
        Entry entry = ((DeleteDialog) dialog).getEntry();

        try {
            rostaService.removeDay(entry.getCustomProperty(KEY_UUID));
            calendar.removeEntry(entry);
        } finally {
            dialog.close();
        }
    }

    private void onCancel(ClickEvent<Button> event) {
        dialog.close();
    }

}
