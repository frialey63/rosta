package org.pjp.rosta.ui.view.maninfo;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.security.RolesAllowed;

import org.pjp.rosta.model.AbsenceDay;
import org.pjp.rosta.model.AbstractDay;
import org.pjp.rosta.model.DayType;
import org.pjp.rosta.model.Holiday;
import org.pjp.rosta.model.User;
import org.pjp.rosta.service.RotaService;
import org.pjp.rosta.ui.component.CompactHorizontalLayout;
import org.pjp.rosta.ui.component.datepicker.MyDatePicker;
import org.pjp.rosta.ui.view.AbstractView;
import org.pjp.rosta.ui.view.MainLayout;
import org.pjp.rosta.ui.view.calendar.SummaryDialog;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.stefan.table.Table;
import org.vaadin.stefan.table.TableDataCell;
import org.vaadin.stefan.table.TableHeaderCell;
import org.vaadin.stefan.table.TableRow;

import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@RolesAllowed("MANAGER")
@PageTitle("Management Information")
@Route(value = "maninfo", layout = MainLayout.class)
public class ManagementInformationView extends AbstractView implements AfterNavigationObserver, ComponentEventListener<ClickEvent<Button>> {

    private enum UserType { ALL, EMPLOYEE, VOLUNTEER, SPECIFIC }

    private enum RangeType { YEAR_TO_DATE, MONTH_TO_DATE, CUSTOM }

    private static void buildHeaderRow(TableRow headerRow) {
        TableHeaderCell headerCell = headerRow.addHeaderCell();
        headerCell.setText("User");
        headerCell.setWidth("20%");
        headerCell = headerRow.addHeaderCell();
        headerCell.setText("Work");
        headerCell.setWidth("20%");
        headerCell = headerRow.addHeaderCell();
        headerCell.setText("Holiday");
        headerCell.setWidth("20%");
        headerCell = headerRow.addHeaderCell();
        headerCell.setText("Absence");
        headerCell.setWidth("20%");
        headerCell = headerRow.addHeaderCell();
        headerCell.setText("Download");
        headerCell.setWidth("20%");
    }

    private static void buildDataRow(TableRow detailsRow, User user, float work, float holiday, float absence, ComponentEventListener<ClickEvent<Button>> listener) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);

        TableDataCell dataCell = detailsRow.addDataCell();
        dataCell.setText(user.getDisplayName());
        dataCell.getStyle().set("text-align", "center");
        dataCell = detailsRow.addDataCell();
        dataCell.setText(nf.format(work));
        dataCell.getStyle().set("text-align", "center");
        dataCell = detailsRow.addDataCell();
        dataCell.setText(nf.format(holiday));
        dataCell.getStyle().set("text-align", "center");
        dataCell = detailsRow.addDataCell();
        dataCell.setText(nf.format(absence));
        dataCell.getStyle().set("text-align", "center");
        dataCell = detailsRow.addDataCell();
        dataCell.add(new Button("Summary", listener));
        dataCell.getStyle().set("text-align", "center");
    }

    private static void buildTotalRow(TableRow detailsRow, float work, float holiday, float absence) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMinimumFractionDigits(1);
        nf.setMaximumFractionDigits(1);

        TableDataCell dataCell = detailsRow.addDataCell();
        dataCell.setText("");
        dataCell.getStyle().set("text-align", "center");
        dataCell = detailsRow.addDataCell();
        dataCell.setText(nf.format(work));
        dataCell.getStyle().set("text-align", "center");
        dataCell = detailsRow.addDataCell();
        dataCell.setText(nf.format(holiday));
        dataCell.getStyle().set("text-align", "center");
        dataCell = detailsRow.addDataCell();
        dataCell.setText(nf.format(absence));
        dataCell.getStyle().set("text-align", "center");
        dataCell = detailsRow.addDataCell();
        dataCell.setText("");
        dataCell.getStyle().set("text-align", "center");
    }

    private static final long serialVersionUID = 4484038138117594303L;

    private final RadioButtonGroup<UserType> userType = new RadioButtonGroup<>();

    private final RadioButtonGroup<RangeType> rangeType = new RadioButtonGroup<>();

    private final Select<User> selectUser = new Select<>();

    private final DatePicker startDate = new MyDatePicker("Start Date");
    private final DatePicker endDate = new MyDatePicker("End Date");

    private Table results;

    @Autowired
    private RotaService rotaService;

    public ManagementInformationView() {
        super();

        setSpacing(false);

        H2 h2 = new H2("Management Information");
        add(h2);
        setHorizontalComponentAlignment(Alignment.CENTER, h2);

        Table table = new Table();
        table.setWidthFull();

        userType.setItems(UserType.values());
        userType.addValueChangeListener(l -> {
            selectUser.setEnabled(l.getValue() == UserType.SPECIFIC && (selectUser.getValue() != null));
        });
        userType.setValue(UserType.ALL);

        TableRow detailsRow = table.addRow();
        detailsRow.addDataCell().setText("User Criteria:");
        detailsRow.addDataCell().add(userType);

        rangeType.setItems(RangeType.values());
        rangeType.addValueChangeListener(l -> {
            if (l.getValue() == RangeType.CUSTOM) {
                startDate.setEnabled(true);
                endDate.setEnabled(true);
            } else {
                startDate.setEnabled(false);
                endDate.setEnabled(false);
            }
        });
        rangeType.setValue(RangeType.YEAR_TO_DATE);

        detailsRow = table.addRow();
        detailsRow.addDataCell().setText("Specific User:");
        detailsRow.addDataCell().add(selectUser);

        detailsRow = table.addRow();
        detailsRow.addDataCell().setText("Range Criteria:");
        detailsRow.addDataCell().add(rangeType);

        detailsRow = table.addRow();
        detailsRow.addDataCell().setText("Custom Range:");
        detailsRow.addDataCell().add(new CompactHorizontalLayout(startDate, endDate));

        add(table);

        results = new Table();
        results.getElement().setAttribute("border", "1px solid black");
        results.setWidthFull();

        Button button = new Button("Run Query");
        button.addClickListener(this);
        add(new Paragraph(), button, new Paragraph());
        setHorizontalComponentAlignment(Alignment.CENTER, button);

        TableRow headerRow = results.addRow();
        buildHeaderRow(headerRow);

        add(results);

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.START);
        setDefaultHorizontalComponentAlignment(Alignment.START);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        super.afterNavigation(event);

        List<User> users = userService.findAllNonManager(null).stream().sorted().collect(Collectors.toList());

        selectUser.setItems(users);
        if (users.size() > 0) {
            selectUser.setValue(users.get(0));
        }

        LocalDate today = LocalDate.now();

        startDate.setValue(today.with(TemporalAdjusters.firstDayOfYear()));
        endDate.setValue(today);
    }

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {
        LocalDate today = LocalDate.now();

        LocalDate start = null;
        LocalDate end = null;

        switch (rangeType.getValue()) {
        case YEAR_TO_DATE:
            start = today.with(TemporalAdjusters.firstDayOfYear());
            end = today;
            break;

        case MONTH_TO_DATE:
            start = today.with(TemporalAdjusters.firstDayOfMonth());
            end = today;
            break;

        case CUSTOM:
            start = startDate.getValue();
            end = endDate.getValue();
            break;
        }

        if (results.getRows().size() > 1) {
            results.removeAllRows();

            buildHeaderRow(results.addRow());
        }

        float workTotal = 0;
        float holidayTotal = 0;
        float absenceTotal = 0;

        for (User user : findUsersByCriteria()) {
            boolean employee = user.isEmployee();
            Set<DayType> dayTypes = employee ? Set.of(DayType.HOLIDAY, DayType.ABSENCE) : Set.of(DayType.VOLUNTARY);
            List<AbstractDay> days = rotaService.getDays(user, dayTypes, start, end);

            float holiday = 0;
            float absence = 0;
            float work = 0;

            if (employee) {
                List<Holiday> holidays = days.stream().filter(abstractDay -> (abstractDay instanceof Holiday)).map(Holiday.class::cast).sorted().collect(Collectors.toList());
                List<AbsenceDay> absences = days.stream().filter(abstractDay -> (abstractDay instanceof AbsenceDay)).map(AbsenceDay.class::cast).sorted().collect(Collectors.toList());

                holiday = holidays.stream().map(Holiday::getPartCount).map(BigDecimal::valueOf).reduce(BigDecimal.ZERO, BigDecimal::add).floatValue();
                absence = absences.stream().map(AbsenceDay::getPartCount).map(BigDecimal::valueOf).reduce(BigDecimal.ZERO, BigDecimal::add).floatValue();

                work = rotaService.calculateShiftWork(user, start, end) - holiday - absence;
            } else {
                work = days.stream().map(day -> day.getPartCount()).map(BigDecimal::valueOf).reduce(BigDecimal.ZERO, BigDecimal::add).floatValue();
            }

            workTotal += work;
            holidayTotal += holiday;
            absenceTotal += absence;

            buildDataRow(results.addRow(), user, work, holiday, absence, dlEvent -> {
                EnhancedDialog dialog = new SummaryDialog(employee, days);
                dialog.setHeader(user.getDisplayName() + " Summary for " + rangeType.getValue());
                dialog.setHeight("80%");
                dialog.setWidth("40%");
                dialog.open();
            });
        }

        buildTotalRow(results.addRow(), workTotal, holidayTotal, absenceTotal);
    }

    private List<User> findUsersByCriteria() {
        return switch (userType.getValue()) {
        case ALL -> userService.findAllNonManager(null);
        case EMPLOYEE -> userService.findAllNonManager(true);
        case VOLUNTEER -> userService.findAllNonManager(false);
        case SPECIFIC -> userService.findByUsername(selectUser.getValue().getUsername()).stream().collect(Collectors.toList());
        };
    }

}
