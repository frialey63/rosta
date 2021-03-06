package org.pjp.rosta.ui.view.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;

import org.pjp.rosta.model.AbstractDay;
import org.pjp.rosta.model.DayType;
import org.pjp.rosta.model.PartOfDay;
import org.pjp.rosta.model.Shift;
import org.pjp.rosta.model.ShiftDay;
import org.pjp.rosta.model.User;
import org.pjp.rosta.service.RostaService;
import org.pjp.rosta.ui.event.DrawerToggleEvent;
import org.pjp.rosta.ui.util.CompactHorizontalLayout;
import org.pjp.rosta.ui.util.MyDatePicker;
import org.pjp.rosta.ui.view.AbstractView;
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
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.HasDynamicTitle;
import com.vaadin.flow.router.Route;

@PermitAll
@Route(value = "calendar", layout = MainLayout.class)
public class CalendarView extends AbstractView implements AfterNavigationObserver, HasDynamicTitle, ComponentEventListener<DatesRenderedEvent> {

    private static final long serialVersionUID = -4423320972580039035L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarView.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

    static final String KEY_UUID = "uuid";

    static final String KEY_DAY_CLASS = "dayClass";

    private static String getTitle(PartOfDay partOfDay) {
        if (partOfDay.isEvening() == null) {
            if (partOfDay.isAllDay()) {
                return "All Day";
            } else if (partOfDay.isMorning()) {
                return "Morning";
            } else if (partOfDay.isAfternoon()) {
                return "Afternoon";
            }

            throw new IllegalStateException();
        } else {
            if (partOfDay.isAllDay()) {
                return "All Day";
            } else {
                List<String> str = new ArrayList<>();

                if (partOfDay.isMorning()) {
                    str.add("Morning");
                }

                if (partOfDay.isAfternoon()) {
                    str.add("Afternoon");
                }

                if (partOfDay.isEvening()) {
                    str.add("Evening");
                }

                return String.join(" & ", str);
            }
        }
    }

    private static class MutableLocalDate {
        private LocalDate date;

        public MutableLocalDate(LocalDate date) {
            super();
            this.date = date;
        }

        public LocalDate get() {
            return date;
        }

        public void plusOneDay() {
            date = date.plusDays(1);
        }
    }

    private Optional<User> optUser;

    private String pageTitle = "Calendar";

    private FullCalendar calendar;

    private final Button buttonDatePicker = new Button("", VaadinIcon.CALENDAR.create());

    private final Button mySummary = new Button("My Summary", this::onMySummary);

    private EnhancedDialog dialog;

    private DatesRenderedEvent datesRenderedEvent;

    @Autowired
    private RostaService rostaService;

    public CalendarView() {
        super();

        MenuBar menuBar = createMenuBar();

        Span filler = new Span();
        setFlexGrow(1, filler);

        HorizontalLayout hl = new CompactHorizontalLayout(menuBar, filler, mySummary);
        hl.setVerticalComponentAlignment(Alignment.CENTER, menuBar, filler, mySummary);
        hl.setAlignItems(Alignment.STRETCH);
        hl.setWidthFull();

        // Create a new calendar instance and attach it to our layout
        calendar = createCalendar();

//        String customCss = "" +
//                ".fc {" + // marks today with red
//                "   --fc-today-bg-color: red;" +
//                "}";
//        calendar.addCustomStyles(customCss);

        setSizeFull();
        setFlexGrow(1, calendar);

        Span helpText = new Span("Click on a day to enter voluntary day, holiday and/or absence; click on week number to access shift pattern (employees only).");
        helpText.getStyle().set("font-style", "italic");

        add(hl, calendar, helpText);
        setHorizontalComponentAlignment(Alignment.STRETCH, hl, calendar, helpText);

        ComponentUtil.addListener(UI.getCurrent(), DrawerToggleEvent.class, event -> {
            remove(calendar, helpText);
            calendar = createCalendar();

            setSizeFull();
            setFlexGrow(1, calendar);

            add(calendar, helpText);
            setHorizontalComponentAlignment(Alignment.STRETCH, calendar, helpText);
        });
    }

    private FullCalendar createCalendar() {
        calendar = FullCalendarBuilder.create().build();
        calendar.setHeightByParent();
        calendar.setFirstDay(DayOfWeek.MONDAY);

        calendar.addTimeslotClickedListener(this::onTimeslotClickedEvent);
        calendar.addEntryClickedListener(this::onEntryClickedEvent);
        calendar.addWeekNumberClickedListener(this::onWeekNumberClickedEvent);
        calendar.addDatesRenderedListener(this);

        return calendar;
    }

    private MenuBar createMenuBar() {
        MenuBar menuBar = new MenuBar();

        // simulate the date picker light that we can use in polymer
        DatePicker gotoDate = new MyDatePicker();
        gotoDate.addValueChangeListener(event1 -> calendar.gotoDate(event1.getValue()));
        gotoDate.getStyle().set("visibility", "hidden");
        gotoDate.getStyle().set("position", "fixed");
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

    private void updateMonthReadout(HasText label, LocalDate date) {
        if (date.getDayOfMonth() != 1) {
            date = date.with(TemporalAdjusters.firstDayOfNextMonth());
        }

        String text = date.format(FORMATTER.withLocale(calendar.getLocale()));
        label.setText(text);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        super.afterNavigation(event);

        optUser = userService.findByUsername(getUsername());

        optUser.ifPresent(user -> {
            if (user.isAdmin()) {
                mySummary.setEnabled(false);
            }
        });
    }

    @Override
    public String getPageTitle() {
        userService.findByUsername(getUsername()).ifPresent(user -> {
            if (user.isAdmin()) {
                pageTitle = "Global " + pageTitle;
            } else {
                pageTitle += user.isEmployee() ? " of Shifts & Holidays" : " of Volunteer Days";
            }
        });

        return pageTitle;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onComponentEvent(DatesRenderedEvent event) {
        this.datesRenderedEvent = event;

        LocalDate start = event.getStart();
        LocalDate end = event.getEnd();

        LOGGER.debug("start = {}, end = {}", start, end);

        updateMonthReadout(buttonDatePicker, start);

        calendar.removeAllEntries();

        optUser.ifPresent(user -> {
            if (user.isAdmin()) {
                Map<String, User> userMap = userService.getAllNonAdmin();

                rostaService.getDays(Set.of(DayType.values()), start, end).forEach(day -> {
                    User entryUser = userMap.get(day.getUserUuid());

                    if (entryUser != null) {
                        String title = entryUser.getDisplayName() + " - " + getTitle(day);

                        Entry entry = new Entry();
                        entry.setCustomProperty(KEY_UUID, day.getUuid());
                        entry.setCustomProperty(KEY_DAY_CLASS, day.getClass().getCanonicalName());
                        entry.setStart(day.getDate());
                        entry.setColor(day.getColour());
                        entry.setAllDay(true);
                        entry.setTitle(title);	// TODO centre align the text in the entry on the calendar
                        entry.setRenderingMode(RenderingMode.BLOCK);

                        entry.setDurationEditable(false);
                        entry.setEditable(false);
                        entry.setDurationEditable(false);

                        calendar.addEntry(entry);
                    }
                });
            } else {
                if (user.isEmployee()) {
                    LocalDate monday = start.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

                    while (monday.isBefore(end)) {
                        LocalDate sunday = monday.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
                        MutableLocalDate date = new MutableLocalDate(monday);

                        rostaService.getShiftForUser(user.getUuid(), sunday).ifPresent(shift -> {
                            shift.getShiftDayIterator().forEachRemaining(shiftDay -> {
                               if (shiftDay.isWorking()) {
                                   Entry entry = new Entry();
                                    entry.setStart(date.get());
                                    entry.setColor(shiftDay.getColour());
                                    entry.setAllDay(true);
                                    entry.setRenderingMode(RenderingMode.BACKGROUND);

                                    calendar.addEntry(entry);
                                }

                               date.plusOneDay();
                            });
                        });

                        monday = monday.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                    }
                }

                Set<DayType> dayTypes = user.isEmployee() ? Set.of(DayType.ABSENCE, DayType.HOLIDAY) : Set.of(DayType.VOLUNTARY);

                rostaService.getDays(user, dayTypes, start, end).forEach(day -> {
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
            }
        });
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
            dialog.setHeight("80%");
            dialog.setWidth("40%");
            dialog.open();
        });
    }

    private void onWeekNumberClickedEvent(WeekNumberClickedEvent event) {
        optUser.ifPresent(user -> {
            if (!user.isAdmin()) {
                LocalDate date = event.getDate();

                assert date.getDayOfWeek() == DayOfWeek.MONDAY;

                LocalDate nowMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate dateSunday = date.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

                if (date.isBefore(nowMonday)) {
                    rostaService.getShiftForUser(user.getUuid(), dateSunday).ifPresentOrElse(shift -> {
                        dialog = new ShiftDialog(shift);
                        dialog.setHeader("View Shift");
                        dialog.open();
                    }, () -> {
                        dialog = new EnhancedDialog();
                        dialog.setHeader("No Shift");
                        dialog.open();
                    });
                } else {
                    Span filler = new Span();
                    CompactHorizontalLayout footer = new CompactHorizontalLayout(filler, new Button("Save", e -> {
                        ShiftDialog shiftDialog = (ShiftDialog) dialog;

                        Shift shift = shiftDialog.isEdit() ? rostaService.getShift(shiftDialog.getShiftUuid()).get() : new Shift(date, user.getUuid());

                        shiftDialog.getEntries().forEach(entry -> {
                            ShiftDay shiftDay = shift.getShiftDay(entry.getDayOfWeek());
                            shiftDay.setMorning(entry.isMorning());
                            shiftDay.setAfternoon(entry.isAfternoon());
                        });

                        rostaService.saveShift(shift);

                        dialog.close();

                        onComponentEvent(datesRenderedEvent);

                    }), new Button("Cancel", this::onCancel));
                    footer.setAlignItems(Alignment.STRETCH);
                    footer.setFlexGrow(1, filler);

                    rostaService.getShiftForUser(user.getUuid(), dateSunday).ifPresentOrElse(shift -> {
                        dialog = new ShiftDialog(date, shift);
                        dialog.setHeader("Modify Shift (" + ShiftDialog.FORMATTER.format(shift.getFromDate()) + ")");
                        dialog.setFooter(footer);
                        dialog.open();
                    }, () -> {
                        dialog = new ShiftDialog(date);
                        dialog.setHeader("New Shift");
                        dialog.setFooter(footer);
                        dialog.open();
                    });
                }
            }
        });
    }

    private void onEntryClickedEvent(EntryClickedEvent event) {
        optUser.ifPresent(user -> {
            LocalDate startDate = event.getEntry().getStartAsLocalDate();
            LocalDate nowMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            if (user.isAdmin() || !startDate.isBefore(nowMonday)) {
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
            LocalDate nowMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

            if (user.isAdmin()) {
                String header = "Add Holiday or Absence";

                dialog = new CreateDialog(startDate);
                dialog.setHeader(header);
                dialog.setFooter(getDialogFooter(true, true, false));
                dialog.open();
            } else {
                if (!startDate.isBefore(nowMonday) && !hasExistingEntry(RenderingMode.BLOCK, startDate)) {
                    String header = String.format("Add %s", (user.isEmployee() ? "Holiday or Absence" : "Volunteer Day"));

                    dialog = new CreateDialog(startDate, user.isEmployee(), user);
                    dialog.setHeader(header);
                    dialog.setFooter(getDialogFooter(true, false));
                    dialog.open();
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    private boolean hasExistingEntry(RenderingMode renderingMode, LocalDate date) {
        return calendar.getEntries().stream().filter(e -> (e.getRenderingMode() == renderingMode) && e.getStartAsLocalDate().equals(date)).findFirst().isPresent();
    }

    @SuppressWarnings("deprecation")
    private void onCreate(ClickEvent<Button> event) {
        CreateDialog createDialog = (CreateDialog) dialog;

        LocalDate date = createDialog.getDate();

        AbstractDay day = AbstractDay.createDay(createDialog.getDayType(), date, createDialog, createDialog.getUser().getUuid());

        String title = (optUser.get().isAdmin() ? createDialog.getUser().getDisplayName() + " - " : "") + getTitle(createDialog);

        Entry entry = new Entry();
        entry.setCustomProperty(KEY_UUID, day.getUuid());
        entry.setCustomProperty(KEY_DAY_CLASS, day.getClass().getCanonicalName());
        entry.setStart(date);
        entry.setColor(day.getColour());
        entry.setAllDay(true);
        entry.setTitle(title); 	// TODO centre align the text in the entry on the calendar
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

    @SuppressWarnings("deprecation")
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

    private void onUserSelect(ComponentValueChangeEvent<Select<User>, User> event) {
        User user = event.getValue();
        boolean employee = user.isEmployee();

        String header = String.format("Add %s", (employee ? "Holiday or Absence" : "Volunteer Day"));
        dialog.setHeader(header);

        CreateDialog createDialog = (CreateDialog) dialog;
        createDialog.reconfigure(employee);
        createDialog.setUser(user);
    }

    private HorizontalLayout getDialogFooter(boolean userSelect, boolean save, boolean delete) {
        Span filler = new Span();

        List<Component> children = new ArrayList<>();
        children.add(filler);

        Button saveButton = new Button("Save", this::onCreate);
        if (save) {
            children.add(saveButton);
        }

        if (delete) {
            children.add(new Button("Delete", this::onDelete));
        }

        if (userSelect) {
            List<User> values = userService.getAllNonAdmin().values().stream().sorted().collect(Collectors.toList());

            Select<User> selector = new Select<>();
            selector.setItems(values);
            selector.addValueChangeListener(this::onUserSelect);

            if (values.isEmpty()) {
                saveButton.setEnabled(false);
            } else {
                selector.setValue(values.get(0));
            }

            children.add(0, selector);
        }

        children.add(new Button("Cancel", this::onCancel));

        HorizontalLayout footer = new CompactHorizontalLayout(children.toArray(new Component[0]));
        footer.setAlignItems(Alignment.STRETCH);
        footer.setVerticalComponentAlignment(Alignment.CENTER, children.toArray(new Component[0]));
        footer.setFlexGrow(1, filler);

        return footer;
    }

    private HorizontalLayout getDialogFooter(boolean save, boolean delete) {
        return getDialogFooter(false, save, delete);
    }

}
