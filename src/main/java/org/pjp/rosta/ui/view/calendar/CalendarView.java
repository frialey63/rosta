package org.pjp.rosta.ui.view.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.pjp.rosta.model.Holiday;
import org.pjp.rosta.service.RostaService;
import org.pjp.rosta.service.UserService;
import org.pjp.rosta.ui.view.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.stefan.fullcalendar.DatesRenderedEvent;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.FullCalendar;
import org.vaadin.stefan.fullcalendar.FullCalendarBuilder;
import org.vaadin.stefan.fullcalendar.TimeslotsSelectedEvent;

import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Calendar of Volunteer Days & Holidays")
@Route(value = "calendar", layout = MainLayout.class)
public class CalendarView extends VerticalLayout implements ComponentEventListener<DatesRenderedEvent> {

    private static final long serialVersionUID = -4423320972580039035L;

    private static final String HOLIDAY_COLOUR = "#ff3333";

    private static final String VOLUNTEER_DAY_COLOUR = "#33ff33";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

    private final FullCalendar calendar = FullCalendarBuilder.create().build();

    private final Button buttonDatePicker = new Button("", VaadinIcon.CALENDAR.create());

    @Autowired
    private UserService userService;

    @Autowired
    private RostaService rostaService;

    public CalendarView() {
        MenuBar menuBar = createMenuBar();

        // Create a new calendar instance and attach it to our layout
        calendar.setFirstDay(DayOfWeek.MONDAY);
        calendar.addTimeslotsSelectedListener(this::onTimeslotsSelectedEvent);
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

        updateMonthReadout(buttonDatePicker, intervalStart);

        calendar.removeAllEntries();

        userService.findByName("Anne").ifPresent(user -> {
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

    // TODO enter a new volunteer day and/or holiday
    public void onTimeslotsSelectedEvent(TimeslotsSelectedEvent event) {
        // react on the selected timeslot, for instance create a new instance and let the user edit it
        Entry entry = new Entry();

        entry.setStart(event.getStartDate()); // also event times are always utc based
        entry.setEnd(event.getEndDate());
        //entry.setAllDay(event.isAllDay());

        entry.setColor("dodgerblue");

        // ... show and editor
        calendar.addEntry(entry);
    }

}
