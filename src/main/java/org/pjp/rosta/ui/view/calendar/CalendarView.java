package org.pjp.rosta.ui.view.calendar;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;

import org.pjp.api.holiday.QueryUtil;
import org.pjp.api.holiday.model.Division;
import org.pjp.api.holiday.model.Event;
import org.pjp.rosta.bean.Repeater.RepeatType;
import org.pjp.rosta.model.AbstractDay;
import org.pjp.rosta.model.DayType;
import org.pjp.rosta.model.PartOfDay;
import org.pjp.rosta.model.Shift;
import org.pjp.rosta.model.ShiftDay;
import org.pjp.rosta.model.User;
import org.pjp.rosta.service.RotaService;
import org.pjp.rosta.service.RotaService.MissingCover;
import org.pjp.rosta.ui.component.CompactHorizontalLayout;
import org.pjp.rosta.ui.component.datepicker.MyDatePicker;
import org.pjp.rosta.ui.component.fc.FullCalendarWithTooltip;
import org.pjp.rosta.ui.component.fc.FullCalendarWithTooltipBuilder;
import org.pjp.rosta.ui.event.DrawerToggleEvent;
import org.pjp.rosta.ui.util.Broadcaster;
import org.pjp.rosta.ui.util.RostaMessage;
import org.pjp.rosta.ui.util.RostaMessage.MessageType;
import org.pjp.rosta.ui.view.AbstractView;
import org.pjp.rosta.ui.view.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.concurrent.ListenableFuture;
import org.vaadin.stefan.fullcalendar.DatesRenderedEvent;
import org.vaadin.stefan.fullcalendar.Entry;
import org.vaadin.stefan.fullcalendar.Entry.RenderingMode;
import org.vaadin.stefan.fullcalendar.EntryClickedEvent;
import org.vaadin.stefan.fullcalendar.TimeslotClickedEvent;
import org.vaadin.stefan.fullcalendar.WeekNumberClickedEvent;

import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.AbstractField.ComponentValueChangeEvent;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.DetachEvent;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
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
import com.vaadin.flow.shared.Registration;

import elemental.json.Json;
import elemental.json.JsonObject;

@PermitAll
@Route(value = "calendar", layout = MainLayout.class)
public class CalendarView extends AbstractView implements AfterNavigationObserver, HasDynamicTitle, ComponentEventListener<DatesRenderedEvent> {

    private static final long serialVersionUID = -4423320972580039035L;

    private static final Logger LOGGER = LoggerFactory.getLogger(CalendarView.class);

    private static final CustomFixedDayGridWeekCalendarView CUSTOM_VIEW = new CustomFixedDayGridWeekCalendarView(1);

    private static final DefaultCalendarView DEFAULT_VIEW = new DefaultCalendarView();

    private static final String YELLOW = "#ffff00";
    private static final String BLACK = "#000000";
    private static final String LIGHT_RED = "#ffcccb";
    private static final String LIGHT_RED_25 = "#ff726f";

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

    static final String KEY_UUID = "uuid";
    static final String KEY_REPEAT_UUID = "repeatUuid";
    static final String KEY_DAY_CLASS = "dayClass";
    static final String KEY_TOOLTIP = "tooltip";
    static final String KEY_DISPLAY_ORDER = "displayOrder";

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

    private static Entry newEntry() {
        Entry entry = new Entry();

        entry.setRenderingMode(RenderingMode.BLOCK);
        entry.setAllDay(true);
        entry.setDurationEditable(false);
        entry.setEditable(false);

        return entry;
    }

    private static Entry createBankHolidayEntry(Event bankHoliday) {
        Entry entry = newEntry();

        String title = bankHoliday.getTitle();
        int displayOrder = 0;

        entry.setCustomProperty(KEY_TOOLTIP, title);
        entry.setCustomProperty(KEY_DISPLAY_ORDER, displayOrder);

        entry.setStart(bankHoliday.getDate());
        entry.setColor(YELLOW);
        entry.setTextColor(BLACK);
        entry.setTitle(String.format("%d %s", displayOrder, title));

        return entry;
    }

    private static Entry createAbstractDayEntry(boolean otherUser, AbstractDay day, String title) {
        Entry entry = newEntry();

        int displayOrder = day.getDisplayOrder();
        if (otherUser) {
            displayOrder += 5;
        }

        entry.setCustomProperty(KEY_DISPLAY_ORDER, displayOrder);
        entry.setCustomProperty(KEY_UUID, day.getUuid());
        entry.setCustomProperty(KEY_REPEAT_UUID, day.getRepeatUuid());
        entry.setCustomProperty(KEY_DAY_CLASS, day.getClass().getCanonicalName());
        entry.setCustomProperty(KEY_TOOLTIP, title);

        entry.setStart(day.getDate());
        entry.setColor(day.getColour());
        entry.setTitle(String.format("%d %s", displayOrder, abbreviate(title)));

        return entry;
    }

    private static Entry createShiftDayEntry(MutableLocalDate date, ShiftDay shiftDay) {
        String title = PartOfDay.getTitle(shiftDay);

        Entry entry = newEntry();

        entry.setCustomProperty(KEY_DISPLAY_ORDER, ShiftDay.getDisplayOrder());
        entry.setCustomProperty(KEY_TOOLTIP, title);

        entry.setStart(date.get());
        entry.setColor(shiftDay.getColour());
        //entry.setTitle(String.format("%d %s", ShiftDay.getDisplayOrder(), abbreviate(title)));
        entry.setTitle("$ " + abbreviate(title));

        return entry;
    }

    private static Entry createMissingCoverEntry(LocalDate date, MissingCover[] missingCover) {
        String colour = (missingCover.length > 1) ? LIGHT_RED_25 : LIGHT_RED;

        Entry entry = new Entry();
        entry.setRenderingMode(RenderingMode.BACKGROUND);
        entry.setAllDay(true);
        entry.setCustomProperty(KEY_TOOLTIP, missingCover);
        entry.setStart(date);
        entry.setColor(colour);

        return entry;
    }

    private static final String SEPARATOR = " - ";

    private static String abbreviate(String str) {
        if (str.contains(SEPARATOR)) {
            String[] strArr = str.split(SEPARATOR);
            String[] nameArr = strArr[0].split(" ");

            String initials = "";

            for (String name : nameArr) {
                initials += name.substring(0, 1);
            }

            str = initials + SEPARATOR + strArr[1];
        }

        if (str.contains("&")) {
            str = str.replace("Morning", "Mrng").replace("Afternoon", "Aftn").replace("Evening", "Evng");
        }

        return str;
    }

    private Optional<User> optUser;

    private String pageTitle = "Calendar";

    private FullCalendarWithTooltip calendar;

    private JsonObject defaultInitialOptions = Json.createObject();
    {
        // FIXME the FullCalendar eventOrder option does not work
        //defaultInitialOptions.put("eventOrderStrict", Json.create(true));
        //defaultInitialOptions.put("eventOrder", Json.create("start,-duration,allDay,displayOrder"));

        DEFAULT_VIEW.extendInitialOptions(defaultInitialOptions);
        CUSTOM_VIEW.extendInitialOptions(defaultInitialOptions);
    }

    private boolean customView;

    private final Button buttonDatePicker = new Button("", VaadinIcon.CALENDAR.create());

    private final Button mySummary = new Button("My Summary", this::onMySummary);

    private EnhancedDialog dialog;

    private DatesRenderedEvent datesRenderedEvent;

    private Registration broadcasterRegistration;

    @Autowired
    private RotaService rotaService;

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
        calendar = createCalendar(defaultInitialOptions);

        setSizeFull();
        setFlexGrow(1, calendar);

        Span helpText = new Span("Click on a day to enter volunteer, holiday and/or absence; click on week number to access shift pattern (employees only).");
        helpText.getStyle().set("font-style", "italic");

        add(hl, calendar, helpText);
        setHorizontalComponentAlignment(Alignment.STRETCH, hl, calendar, helpText);

        ComponentUtil.addListener(UI.getCurrent(), DrawerToggleEvent.class, event -> {
            remove(calendar, helpText);

            calendar = createCalendar(defaultInitialOptions);
            if (customView) {
                calendar.changeView(CUSTOM_VIEW);
            }

            setSizeFull();
            setFlexGrow(1, calendar);

            add(calendar, helpText);
            setHorizontalComponentAlignment(Alignment.STRETCH, calendar, helpText);
        });
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        UI ui = attachEvent.getUI();

        broadcasterRegistration = Broadcaster.register(newMessage -> {
            ui.access(() -> {
                LOGGER.debug("received message {}", newMessage);

                LocalDate date = newMessage.date();
                LocalDate start = datesRenderedEvent.getStart();
                LocalDate end = datesRenderedEvent.getEnd();

                switch (newMessage.messageType()) {
                case DAY_CREATE:
                case DAY_DELETE:
                    if (ui.getUIId() == newMessage.uiId()) {
                        refreshMissingCover();
                    } else {
                        if (!date.isBefore(start) && !date.isAfter(end)) {
                            refresh(datesRenderedEvent);
                        }
                    }
                    break;
                case SHIFT_UPDATE:
                    if (ui.getUIId() == newMessage.uiId()) {
                        refresh(datesRenderedEvent);
                    } else {
                        if (!date.isBefore(start) && !date.isAfter(end)) {
                            refresh(datesRenderedEvent);
                        }
                    }
                    break;
                }
            });
        });
    }

    @Override
    protected void onDetach(DetachEvent detachEvent) {
        broadcasterRegistration.remove();
        broadcasterRegistration = null;
    }

    private FullCalendarWithTooltip createCalendar(JsonObject initialOptions) {
        calendar = FullCalendarWithTooltipBuilder.create().withInitialOptions(initialOptions).build();
        calendar.setHeightByParent();
        calendar.setFirstDay(DayOfWeek.MONDAY);

        calendar.addTimeslotClickedListener(this::onTimeslotClickedEvent);
        calendar.addEntryClickedListener(this::onEntryClickedEvent);
        calendar.addWeekNumberClickedListener(this::onWeekNumberClickedEvent);
        calendar.addDatesRenderedListener(this);
        /*
        String customCss = "" +
                ".fc {" + // marks today with red
                "   --fc-today-bg-color: red;" +
                "}";
        calendar.addCustomStyles(customCss);
        */
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

        menuBar.addItem("Refresh", e -> refresh(datesRenderedEvent));
        menuBar.addItem(VaadinIcon.ANGLE_LEFT.create(), e -> calendar.previous());
        menuBar.addItem(buttonDatePicker);
        menuBar.addItem(VaadinIcon.ANGLE_RIGHT.create(), e -> calendar.next());
        menuBar.addItem("One Week", e -> {
            MenuItem menuItem = e.getSource();

            if (customView) {
                calendar.changeView(DEFAULT_VIEW);
                menuItem.setText("One Week");
            } else {
                calendar.changeView(CUSTOM_VIEW);
                menuItem.setText("Default");
            }

            calendar.today();

            customView = !customView;
        });

        return menuBar;
    }

    private void updateMonthReadout(HasText label, LocalDate date) {
        if (customView) {
            // date is first day of the week and accept this as the current month
        } else {
            // date may be first day of week in the previous month
            if (date.getDayOfMonth() != 1) {
                date = date.with(TemporalAdjusters.firstDayOfNextMonth());
            }
        }

        String text = date.format(FORMATTER.withLocale(calendar.getLocale()));
        label.setText(text);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        super.afterNavigation(event);

        optUser = userService.findByUsername(getUsername());

        optUser.ifPresent(user -> {
            if (user.isManager()) {
                mySummary.setEnabled(false);
            }
        });
    }

    @Override
    public String getPageTitle() {
        userService.findByUsername(getUsername()).ifPresent(user -> {
            if (user.isManager() || user.isSupervisor()) {
                pageTitle = "Global " + pageTitle;
            } else {
                pageTitle += user.isEmployee() ? " of Shifts & Holidays" : " of Volunteer Days";
            }
        });

        return pageTitle;
    }

    @Override
    public void onComponentEvent(DatesRenderedEvent event) {
        this.datesRenderedEvent = event;

        refresh(event);
    }

    @SuppressWarnings("deprecation")
    private void refresh(DatesRenderedEvent event) {
        LocalDate start = event.getStart();
        LocalDate end = event.getEnd();

        LOGGER.debug("start = {}, end = {}", start, end);

        updateMonthReadout(buttonDatePicker, start);

        calendar.removeAllEntries();

        addBankHolidays(start, end);

        optUser.ifPresent(user -> {
            if (user.isEmployee()) {
                addShiftDays(user, start, end);
            }

            addAbstractDays(user, start, end);

            if (user.isManager() || user.isSupervisor()) {
                addAbstractDaysForOtherUsers(start, end, user);

                addMissingCover(start, end);
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void addMissingCover(LocalDate start, LocalDate end) {
        LocalDate monday = start;

        while (!monday.isAfter(end)) {
            ListenableFuture<Map<DayOfWeek, MissingCover[]>> future = rotaService.checkRota(monday);

            final var capturedMonday = monday;

            UI ui = UI.getCurrent();

            future.addCallback(map -> {
                LOGGER.debug("successful background calculation of missing cover");

                map.keySet().forEach(dayOfWeek -> {
                    LocalDate date = capturedMonday.with(TemporalAdjusters.nextOrSame(dayOfWeek));

                    MissingCover[] missingCover = map.get(dayOfWeek);

                    Entry entry = createMissingCoverEntry(date, missingCover);

                    ui.access(() -> {
                        calendar.addEntry(entry);
                    });
                });
            }, failure -> {
                LOGGER.error("failed background calculation of missing cover");
            });

            monday = monday.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        }
    }

    @SuppressWarnings("deprecation")
    private void addAbstractDaysForOtherUsers(LocalDate start, LocalDate end, User user) {
        Map<String, User> userMap = userService.getAllNonManager();

        rotaService.getDays(Set.of(DayType.values()), start, end).forEach(day -> {
            User entryUser = userMap.get(day.getUserUuid());

            if ((entryUser != null) && !entryUser.equals(user)) {	// avoid adding events for a supervisor twice
                String title = entryUser.getDisplayName() + SEPARATOR + PartOfDay.getTitle(day);

                calendar.addEntry(createAbstractDayEntry(true, day, title));
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void addAbstractDays(User user, LocalDate start, LocalDate end) {
        Set<DayType> dayTypes = user.isEmployee() ? Set.of(DayType.ABSENCE, DayType.HOLIDAY) : Set.of(DayType.VOLUNTEER);

        rotaService.getDays(user, dayTypes, start, end).forEach(day -> {
            String title = PartOfDay.getTitle(day);

            calendar.addEntry(createAbstractDayEntry(false, day, title));
        });
    }

    @SuppressWarnings("deprecation")
    private void addShiftDays(User user, LocalDate start, LocalDate end) {
        LocalDate monday = start.with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));

        while (monday.isBefore(end)) {
            LocalDate sunday = monday.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
            MutableLocalDate date = new MutableLocalDate(monday);

            rotaService.getShiftForUser(user.getUuid(), sunday).ifPresent(shift -> {
                shift.getShiftDayIterator().forEachRemaining(shiftDay -> {
                   if (shiftDay.isWorking()) {
                       calendar.addEntry(createShiftDayEntry(date, shiftDay));
                    }

                   date.plusOneDay();
                });
            });

            monday = monday.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        }
    }

    @SuppressWarnings("deprecation")
    private void addBankHolidays(LocalDate start, LocalDate end) {
        QueryUtil.getInstance().getHolidays(Division.ENGLAND_AND_WALES, start.getYear()).forEach(bankHoliday -> {
            calendar.addEntry(createBankHolidayEntry(bankHoliday));
        });

        if (end.getYear() != start.getYear()) {
            QueryUtil.getInstance().getHolidays(Division.ENGLAND_AND_WALES, end.getYear()).forEach(bankHoliday -> {
                calendar.addEntry(createBankHolidayEntry(bankHoliday));
            });
        }
    }

    @SuppressWarnings("deprecation")
    private void removeAllEntries(RenderingMode renderingMode) {
        calendar.getEntries().stream().filter(e -> e.getRenderingMode() == renderingMode).forEach(entry -> calendar.removeEntry(entry));
    }

    private void onMySummary(ClickEvent<Button> event) {
        optUser.ifPresent(user -> {
            LocalDate today = LocalDate.now();

            LocalDate start = today.with(TemporalAdjusters.firstDayOfYear());
            LocalDate end = today.with(TemporalAdjusters.lastDayOfYear());

            boolean employee = user.isEmployee();
            Set<DayType> dayTypes = employee ? Set.of(DayType.HOLIDAY, DayType.ABSENCE) : Set.of(DayType.VOLUNTEER);
            List<AbstractDay> days = rotaService.getDays(user, dayTypes, start, end);

            dialog = new SummaryDialog(employee, days);
            dialog.setHeader("My Summary for " + today.getYear());
            dialog.setHeight("80%");
            dialog.setWidth("40%");
            dialog.open();
        });
    }

    private void onWeekNumberClickedEvent(WeekNumberClickedEvent event) {
        optUser.ifPresent(user -> {
            if (!user.isManager()) {
                LocalDate date = event.getDate();

                assert date.getDayOfWeek() == DayOfWeek.MONDAY;

                LocalDate nowMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                LocalDate dateSunday = date.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));

                if (date.isBefore(nowMonday)) {
                    rotaService.getShiftForUser(user.getUuid(), dateSunday).ifPresentOrElse(shift -> {
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
                    CompactHorizontalLayout footer = new CompactHorizontalLayout(filler, new Button("Save", this::onShiftSave), new Button("Cancel", this::onCancel));
                    footer.setAlignItems(Alignment.STRETCH);
                    footer.setFlexGrow(1, filler);

                    rotaService.getShiftForUser(user.getUuid(), dateSunday).ifPresentOrElse(shift -> {
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
        Entry entry = event.getEntry();

        if ((entry.getRenderingMode() == RenderingMode.BLOCK) && (entry.getCustomProperty(KEY_UUID) != null)) {
            optUser.ifPresent(user -> {
                LocalDate startDate = entry.getStartAsLocalDate();
                LocalDate nowMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

                if (user.isManager() || user.isSupervisor() || !startDate.isBefore(nowMonday)) {	// the worker can only delete from current week onwards
                    dialog = new DeleteDialog(entry);
                    dialog.setHeader("Delete");
                    dialog.setFooter(getDialogFooter(false, true));
                    dialog.open();
                }
            });
        }
    }

    private void onTimeslotClickedEvent(TimeslotClickedEvent event) {
        optUser.ifPresent(user -> {
            LocalDate startDate = event.getDate();
            LocalDate nowMonday = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            boolean dateFlag = !startDate.isBefore(nowMonday);

            if (user.isManager() || (dateFlag && user.isSupervisor())) {
                String header = String.format("Add %s", (user.isEmployee() ? "Holiday or Absence" : "Volunteer Day"));

                dialog = new CreateDialog(startDate, user.isEmployee());
                dialog.setHeader(header);
                dialog.setFooter(getDialogFooter(user, true, false));
                dialog.open();
            } else {
                if (dateFlag) {
                    String header = String.format("Add %s", (user.isEmployee() ? "Holiday or Absence" : "Volunteer Day"));

                    dialog = new CreateDialog(startDate, user);
                    dialog.setHeader(header);
                    dialog.setFooter(getDialogFooter(true, false));
                    dialog.open();
                }
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void onDayCreate(ClickEvent<Button> event) {
        CreateDialog createDialog = (CreateDialog) dialog;

        LocalDate date = createDialog.getDate();
        User dayUser = (createDialog.getUser() != null) ? createDialog.getUser() : optUser.get();

        List<AbstractDay> days = new ArrayList<>();

        if (createDialog.getRepeater().repeatType() == RepeatType.NONE) {
            days.add(AbstractDay.createDay(createDialog.getDayType(), date, (PartOfDay) createDialog, dayUser.getUuid()));
        } else {
            days.addAll(AbstractDay.createVolunteerDays(date, createDialog.getRepeater(), (PartOfDay) createDialog, dayUser.getUuid()));
        }

        User user = optUser.get();
        int uiId = UI.getCurrent().getUIId();

        try {
            LocalDate dateOfFirstConflict = rotaService.saveDays(days, user.getUuid());

            if (dateOfFirstConflict == null) {
                boolean otherUser = !user.equals(dayUser);

                String title = (otherUser ? (dayUser.getDisplayName() + SEPARATOR) : "") + PartOfDay.getTitle(createDialog);

                for (AbstractDay tempDay : days) {
                    calendar.addEntry(createAbstractDayEntry(otherUser, tempDay, title));
                    Broadcaster.broadcast(new RostaMessage(MessageType.DAY_CREATE, uiId, tempDay.getDate()));
                }

            } else {
                EnhancedDialog conflictDialog = new ConflictDialog(createDialog.getDayType(), dateOfFirstConflict);
                conflictDialog.setHeader("Conflict");
                conflictDialog.setFooter(getDialogFooter(conflictDialog));
                conflictDialog.open();
            }
        } finally {
            dialog.close();
        }
    }

    private void refreshMissingCover() {
        removeAllEntries(RenderingMode.BACKGROUND);

        optUser.ifPresent(user -> {
            if (user.isManager() || user.isSupervisor()) {
                addMissingCover(datesRenderedEvent.getStart(), datesRenderedEvent.getEnd());
            }
        });
    }

    @SuppressWarnings("deprecation")
    private void onDayDelete(ClickEvent<Button> event) {
        Entry entry = ((DeleteDialog) dialog).getEntry();

        try {
            User user = optUser.get();
            int uiId = UI.getCurrent().getUIId();

            rotaService.removeDay(entry.getCustomProperty(KEY_UUID), user.getUuid());

            calendar.removeEntry(entry);
            Broadcaster.broadcast(new RostaMessage(MessageType.DAY_DELETE, uiId, entry.getStartAsLocalDate()));

            if (((DeleteDialog) dialog).isRepeated()) {
                String repeatUuid = entry.getCustomProperty(KEY_REPEAT_UUID);

                rotaService.removeDays(repeatUuid, user.getUuid());

                calendar.getEntries().stream().filter(e -> Objects.equals(repeatUuid, e.getCustomProperty(KEY_REPEAT_UUID))).forEach(tempEntry -> {
                    calendar.removeEntry(tempEntry);
                    Broadcaster.broadcast(new RostaMessage(MessageType.DAY_DELETE, uiId, tempEntry.getStartAsLocalDate()));
                });
            }

        } finally {
            dialog.close();
        }
    }

    private void onShiftSave(ClickEvent<Button> event) {
        try {
            ShiftDialog shiftDialog = (ShiftDialog) dialog;

            LocalDate date = shiftDialog.getDate();

            assert date.getDayOfWeek() == DayOfWeek.MONDAY;

            Shift shift = shiftDialog.isEdit() ? rotaService.getShift(shiftDialog.getShiftUuid()).get() : new Shift(date, optUser.get().getUuid());

            shiftDialog.getEntries().forEach(entry -> {
                ShiftDay shiftDay = shift.getShiftDay(entry.getDayOfWeek());
                shiftDay.setMorning(entry.isMorning());
                shiftDay.setAfternoon(entry.isAfternoon());
            });

            rotaService.saveShift(shift);

            Broadcaster.broadcast(new RostaMessage(MessageType.SHIFT_UPDATE, UI.getCurrent().getUIId(), date));
        } finally {
            dialog.close();
        }
    }

    private void onCancel(ClickEvent<Button> event) {
        dialog.close();
    }

    private void onUserSelect(ComponentValueChangeEvent<Select<User>, User> event) {
        User user = event.getValue();

        // check for "Me" selection
        if (user == null) {
            user = optUser.get();
        }

        boolean employee = user.isEmployee();

        String header = String.format("Add %s", (employee ? "Holiday or Absence" : "Volunteer Day"));
        dialog.setHeader(header);

        CreateDialog createDialog = (CreateDialog) dialog;
        createDialog.reconfigure(employee);
        createDialog.setUser(user);
    }

    private HorizontalLayout getDialogFooter(User user, boolean save, boolean delete) {
        Span filler = new Span();

        List<Component> children = new ArrayList<>();
        children.add(filler);

        Button saveButton = new Button("Save", this::onDayCreate);
        if (save) {
            children.add(saveButton);
        }

        if (delete) {
            children.add(new Button("Delete", this::onDayDelete));
        }

        if (user != null) {
            List<User> others = userService.getAllNonManager().values().stream().filter(other -> {
                if (user.isSupervisor()) {
                    return !other.equals(user);
                }

                return true;
            }).sorted().collect(Collectors.toList());

            Select<User> selector = new Select<>();
            selector.setItems(others);

            if (user.isSupervisor()) {
                selector.setEmptySelectionAllowed(true);
                selector.setEmptySelectionCaption("Me");
                selector.setValue(null);
            } else {
                if (others.isEmpty()) {
                    saveButton.setEnabled(false);
                } else {
                    selector.setValue(others.get(0));
                }
            }

            selector.addValueChangeListener(this::onUserSelect);

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
        return getDialogFooter(null, save, delete);
    }

    private HorizontalLayout getDialogFooter(EnhancedDialog dialog) {
        Span filler = new Span();

        List<Component> children = new ArrayList<>();
        children.add(filler);

        children.add(new Button("Cancel", e -> dialog.close()));

        HorizontalLayout footer = new CompactHorizontalLayout(children.toArray(new Component[0]));
        footer.setAlignItems(Alignment.STRETCH);
        footer.setVerticalComponentAlignment(Alignment.CENTER, children.toArray(new Component[0]));
        footer.setFlexGrow(1, filler);

        return footer;
    }

}
