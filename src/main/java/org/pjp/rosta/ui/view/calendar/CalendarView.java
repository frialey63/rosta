package org.pjp.rosta.ui.view.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.pjp.rosta.model.AbstractDay;
import org.pjp.rosta.model.Holiday;
import org.pjp.rosta.model.PartOfDay;
import org.pjp.rosta.model.Shift;
import org.pjp.rosta.model.ShiftDay;
import org.pjp.rosta.security.Session;
import org.pjp.rosta.service.RostaService;
import org.pjp.rosta.service.UserService;
import org.pjp.rosta.ui.view.CompactHorizontalLayout;
import org.pjp.rosta.ui.view.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.stefan.fullcalendar.DatesRenderedEvent;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.Entry.RenderingMode;
import org.vaadin.stefan.fullcalendar.EntryClickedEvent;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.fullcalendar.TimeslotsSelectedEvent;
import org.vaadin.stefan.fullcalendar.WeekNumberClickedEvent;

import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.ClickEvent;
//.import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Calendar of Volunteer Days & Holidays")
@Route(value = "calendar", layout = MainLayout.class)
public class CalendarView extends VerticalLayout implements ComponentEventListener<DatesRenderedEvent> {

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
        calendar.addWeekNumberClickedListener(this::onWeekNumberClickedEvent);
        calendar.addDatesRenderedListener(this);

//        String customCss = "" +
//                ".fc {" + // marks today with red
//                "   --fc-today-bg-color: red;" +
//                "}";
//        calendar.addCustomStyles(customCss);

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
                entry.setRenderingMode(RenderingMode.BLOCK);

                calendar.addEntry(entry);
            });
        });
    }

    public void onWeekNumberClickedEvent(WeekNumberClickedEvent event) {
        String username = Session.getUsername();

        userService.findByName(username).ifPresent(user -> {
            LocalDate date = event.getDate();

            dialog = new ShiftDialog(date);
            dialog.setHeader("Create New Shift");
            dialog.setFooter(new CompactHorizontalLayout(new Button("Save", e -> {
                Shift shift = new Shift(date, user.getUuid());

                ((ShiftDialog) dialog).getEntries().forEach(entry -> {
                    ShiftDay shiftDay = shift.getShiftDay(entry.getDayOfWeek());
                    shiftDay.setMorning(entry.isMorning());
                    shiftDay.setAfternoon(entry.isAfternoon());
                });

                rostaService.saveShift(shift);

                dialog.close();

            }), new Button("Cancel", this::onCancel)));
            dialog.open();
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
        entry.setRenderingMode(RenderingMode.BLOCK);

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
