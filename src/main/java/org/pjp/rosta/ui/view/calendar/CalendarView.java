package org.pjp.rosta.ui.view.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.pjp.rosta.model.AbstractDay;
import org.pjp.rosta.model.DayType;
import org.pjp.rosta.model.PartOfDay;
import org.pjp.rosta.model.Shift;
import org.pjp.rosta.model.ShiftDay;
import org.pjp.rosta.model.User;
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
import org.vaadin.stefan.fullcalendar.TimeslotClickedEvent;
import org.vaadin.stefan.fullcalendar.WeekNumberClickedEvent;

import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

@Route(value = "calendar", layout = MainLayout.class)
public class CalendarView extends VerticalLayout implements AfterNavigationObserver, HasDynamicTitle, ComponentEventListener<DatesRenderedEvent> {

    private static final long serialVersionUID = -4423320972580039035L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarView.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

    static final String KEY_UUID = "uuid";

    static final String KEY_DAY_CLASS = "dayClass";

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

    private Optional<User> optUser;

    private String pageTitle = "Calendar";

    private final FullCalendar calendar = FullCalendarBuilder.create().build();

    private final Button buttonDatePicker = new Button("", VaadinIcon.CALENDAR.create());

    private EnhancedDialog dialog;

    @Autowired
    private UserService userService;

    @Autowired
    private RostaService rostaService;

    public CalendarView() {
        MenuBar menuBar = createMenuBar();

        Span filler = new Span();
        setFlexGrow(1, filler);

        Button mySummary = new Button("My Summary", this::onMySummary);

        HorizontalLayout hl = new CompactHorizontalLayout(menuBar, filler, mySummary);
        hl.setVerticalComponentAlignment(Alignment.CENTER, menuBar, filler, mySummary);
        hl.setAlignItems(Alignment.STRETCH);
        hl.setWidthFull();

        // Create a new calendar instance and attach it to our layout
        calendar.setFirstDay(DayOfWeek.MONDAY);
        calendar.addTimeslotClickedListener(this::onTimeslotClickedEvent);
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

        Span helpText = new Span("Click on a day to enter voluntary day, holiday and/or absence; click on week number to access shift pattern (employees only).");
        helpText.getElement().getStyle().set("font-style", "italic");

        setHorizontalComponentAlignment(Alignment.STRETCH, hl, calendar, helpText);
        add(hl, calendar, helpText);
    }

    private void updateMonthReadout(HasText label, LocalDate date) {
        if (date.getDayOfMonth() != 1) {
            date = date.with(TemporalAdjusters.firstDayOfNextMonth());
        }

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
    public void afterNavigation(AfterNavigationEvent event) {
        String username = Session.getUsername();

        optUser = userService.findByUsername(username);
    }

    @Override
    public String getPageTitle() {
        String username = Session.getUsername();

        userService.findByUsername(username).ifPresent(user -> {
            pageTitle += user.isEmployee() ? " of Shifts & Holidays" : " of Volunteer Days";
        });

        return pageTitle;
    }

    @Override
    public void onComponentEvent(DatesRenderedEvent event) {
        LocalDate start = event.getStart();
        LocalDate end = event.getEnd();

        LOGGER.info("start = {}, end = {}", start, end);

        updateMonthReadout(buttonDatePicker, start);

        calendar.removeAllEntries();

        optUser.ifPresent(user -> {
            rostaService.getDays(user, Set.of(DayType.values()), start, end).forEach(day -> {
                Entry entry = new Entry();
                entry.setCustomProperty(KEY_UUID, day.getUuid());
                entry.setCustomProperty(KEY_DAY_CLASS, day.getClass().getCanonicalName());
                entry.setStart(day.getDate());
                entry.setColor(day.getColour());
                entry.setAllDay(true);
                entry.setTitle(getTitle(day));	// TODO centre align the text in the entry on the calendar
                entry.setRenderingMode(RenderingMode.BLOCK);

                entry.setDurationEditable(false);
                entry.setEditable(false);
                entry.setDurationEditable(false);

                calendar.addEntry(entry);
            });
        });

//        Entry entry = new Entry();
//        entry.setStart(LocalDate.of(2022, 6, 9));
//        entry.setColor(new String("#00ff00"));
//        entry.setAllDay(true);
//        entry.setRenderingMode(RenderingMode.BACKGROUND);
//
//        calendar.addEntry(entry);

    }

    private void onMySummary(ClickEvent<Button> event) {
        optUser.ifPresent(user -> {
            LocalDate today = LocalDate.now();

            LocalDate start = today.with(TemporalAdjusters.firstDayOfYear());
            LocalDate end = today.with(TemporalAdjusters.lastDayOfYear());

            boolean employee = user.isEmployee();
            Set<DayType> dayTypes = employee ? Set.of(DayType.HOLIDAY, DayType.ABSENCE) : Set.of(DayType.VOLUNTARY);
            List<AbstractDay> days = rostaService.getDays(user, dayTypes, start, end);

            dialog = new SummaryDialog(employee, days);
            dialog.setHeader("My Summary for " + today.getYear());
            dialog.setFooter(getDialogFooter());
            dialog.setHeight("80%");
            dialog.setWidth("40%");
            dialog.open();
        });
    }

    private void onWeekNumberClickedEvent(WeekNumberClickedEvent event) {
        optUser.ifPresent(user -> {
            LocalDate date = event.getDate();

            assert date.getDayOfWeek() == DayOfWeek.MONDAY;

            LocalDate nowMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            LocalDate dateSunday = date.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

            if (date.isBefore(nowMonday)) {
                rostaService.getShiftForUser(dateSunday, user).ifPresentOrElse(shift -> {
                    dialog = new ShiftDialog(shift);
                    dialog.setHeader("View Shift");
                    dialog.setFooter(getDialogFooter());
                    dialog.open();
                }, () -> {
                    dialog = new EnhancedDialog();
                    dialog.setHeader("No Shift");
                    dialog.setFooter(getDialogFooter());
                    dialog.open();
                });
            } else {
                rostaService.getShiftForUser(dateSunday, user).ifPresentOrElse(shift -> {
                    dialog = new ShiftDialog(date, shift);
                    dialog.setHeader("Modify Shift (" + ShiftDialog.FORMATTER.format(shift.getFromDate()) + ")");
                    dialog.setFooter(new CompactHorizontalLayout(new Button("Save", e -> {
                        Shift newShift = new Shift(date, user.getUuid());

                        ((ShiftDialog) dialog).getEntries().forEach(entry -> {
                            ShiftDay shiftDay = newShift.getShiftDay(entry.getDayOfWeek());
                            shiftDay.setOpener(entry.isOpener());
                            shiftDay.setMorning(entry.isMorning());
                            shiftDay.setAfternoon(entry.isAfternoon());
                        });

                        rostaService.saveShift(newShift);

                        dialog.close();

                    }), new Button("Cancel", this::onCancel)));
                    dialog.open();
                }, () -> {
                    dialog = new ShiftDialog(date);
                    dialog.setHeader("New Shift");
                    dialog.setFooter(new CompactHorizontalLayout(new Button("Save", e -> {
                        Shift newShift = new Shift(date, user.getUuid());

                        ((ShiftDialog) dialog).getEntries().forEach(entry -> {
                            ShiftDay shiftDay = newShift.getShiftDay(entry.getDayOfWeek());
                            shiftDay.setOpener(entry.isOpener());
                            shiftDay.setMorning(entry.isMorning());
                            shiftDay.setAfternoon(entry.isAfternoon());
                        });

                        rostaService.saveShift(newShift);

                        dialog.close();

                    }), new Button("Cancel", this::onCancel)));
                    dialog.open();
                });
            }
        });
    }

    private void onEntryClickedEvent(EntryClickedEvent event) {
        optUser.ifPresent(user -> {
            LocalDate startDate = event.getEntry().getStartAsLocalDate();

            if (!startDate.isBefore(LocalDate.now())) {
                dialog = new DeleteDialog(event.getEntry());
                dialog.setHeader("Delete");
                dialog.setFooter(getDialogFooter(false, true));
                dialog.open();
            }
        });
    }

    private void onTimeslotClickedEvent(TimeslotClickedEvent event) {
        optUser.ifPresent(user -> {
            LocalDate startDate = event.getDate();

            if (!startDate.isBefore(LocalDate.now())) {
                String header = String.format("Add %s", (user.isEmployee() ? "Holiday or Absence" : "Volunteer Day"));

                dialog = new CreateDialog(user.isEmployee(), startDate, user.getUuid());
                dialog.setHeader(header);
                dialog.setFooter(getDialogFooter(true, false));
                dialog.open();
            }
        });
    }

    private void onCreate(ClickEvent<Button> event) {
        CreateDialog createDialog = (CreateDialog) dialog;

        LocalDate date = createDialog.getDate();

        AbstractDay day = AbstractDay.createDay(createDialog.getDayType(), date, createDialog, createDialog.getUserUuid());

        Entry entry = new Entry();
        entry.setCustomProperty(KEY_UUID, day.getUuid());
        entry.setCustomProperty(KEY_DAY_CLASS, day.getClass().getCanonicalName());
        entry.setStart(date);
        entry.setColor(day.getColour());
        entry.setAllDay(true);
        entry.setTitle(getTitle(createDialog)); 	// TODO centre align the text in the entry on the calendar
        entry.setRenderingMode(RenderingMode.BLOCK);


        entry.setDurationEditable(false);
        entry.setEditable(false);
        entry.setDurationEditable(false);

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

    private HorizontalLayout getDialogFooter(boolean save, boolean delete) {
        Span filler = new Span();

        List<Component> children = new ArrayList<>();
        children.add(filler);

        if (save) {
            children.add(new Button("Save", this::onCreate));
        }

        if (delete) {
            children.add(new Button("Delete", this::onDelete));
        }

        children.add(new Button("Cancel", this::onCancel));

        HorizontalLayout footer = new CompactHorizontalLayout(children.toArray(new Component[0]));
        footer.setAlignItems(Alignment.STRETCH);
        footer.setFlexGrow(1, filler);

        return footer;
    }

    private HorizontalLayout getDialogFooter() {
        return getDialogFooter(false, false);
    }

}
