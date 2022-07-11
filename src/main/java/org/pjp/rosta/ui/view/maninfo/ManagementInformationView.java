package org.pjp.rosta.ui.view.maninfo;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.security.PermitAll;

import org.pjp.rosta.model.User;
import org.pjp.rosta.service.RostaService;
import org.pjp.rosta.service.UserService;
import org.pjp.rosta.ui.util.CompactHorizontalLayout;
import org.pjp.rosta.ui.util.MyDatePicker;
import org.pjp.rosta.ui.view.AbstractView;
import org.pjp.rosta.ui.view.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.stefan.table.Table;
import org.vaadin.stefan.table.TableDataCell;
import org.vaadin.stefan.table.TableHeaderCell;
import org.vaadin.stefan.table.TableRow;

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

@PermitAll
@PageTitle("Management Information")
@Route(value = "maninfo", layout = MainLayout.class)
public class ManagementInformationView extends AbstractView implements AfterNavigationObserver, ComponentEventListener<ClickEvent<Button>> {

    private enum UserType { ALL, EMPLOYEE, VOLUNTEER, SPECIFIC }

    private enum RangeType { YEAR, MONTH, CUSTOM }

    private static void buildHeaderRow(TableRow headerRow) {
        TableHeaderCell headerCell = headerRow.addHeaderCell();
        headerCell.setText("User");
        headerCell.setWidth("20%");
        headerCell = headerRow.addHeaderCell();
        headerCell.setText("Work Periods");
        headerCell.setWidth("20%");
        headerCell = headerRow.addHeaderCell();
        headerCell.setText("Holidays");
        headerCell.setWidth("20%");
        headerCell = headerRow.addHeaderCell();
        headerCell.setText("Absences");
        headerCell.setWidth("20%");
        headerCell = headerRow.addHeaderCell();
        headerCell.setText("Download");
        headerCell.setWidth("20%");
    }

    private static void buildDataRow(User user, TableRow detailsRow) {
        TableDataCell dataCell = detailsRow.addDataCell();
        dataCell.setText(user.getDisplayName());
        dataCell.getStyle().set("text-align", "center");
        dataCell = detailsRow.addDataCell();
        dataCell.setText("999");
        dataCell.getStyle().set("text-align", "center");
        dataCell = detailsRow.addDataCell();
        dataCell.setText("999");
        dataCell.getStyle().set("text-align", "center");
        dataCell = detailsRow.addDataCell();
        dataCell.setText("999");
        dataCell.getStyle().set("text-align", "center");
        dataCell = detailsRow.addDataCell();
        dataCell.add(new Button("Download"));
    }

    private static final long serialVersionUID = 4484038138117594303L;

    private final RadioButtonGroup<UserType> userType = new RadioButtonGroup<>();

    private final RadioButtonGroup<RangeType> rangeType = new RadioButtonGroup<>();

    private final Select<String> selectUser = new Select<>();

    private final DatePicker startDate = new MyDatePicker("Start Date");
    private final DatePicker endDate = new MyDatePicker("End Date");

    private Table results;

    @Autowired
    private UserService userService;

    @Autowired
    private RostaService rostaService;

    public ManagementInformationView() {
        setSpacing(false);

        H2 h2 = new H2("Management Information (TBC)");
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
        rangeType.setValue(RangeType.YEAR);

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
        List<String> usernames = userService.findAll(null).stream().map(User::getUsername).sorted().collect(Collectors.toList());

        selectUser.setItems(usernames);
        if (usernames.size() > 0) {
            selectUser.setValue(usernames.get(0));
        }
    }

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {
        if (results.getRows().size() > 1) {
            results.removeAllRows();

            TableRow headerRow = results.addRow();
            buildHeaderRow(headerRow);
        }

        findUsersByCriteria().forEach(user -> {
            TableRow detailsRow = results.addRow();

            buildDataRow(user, detailsRow);
        });
    }

    private List<User> findUsersByCriteria() {
        return switch (userType.getValue()) {
        case ALL -> userService.findAll(null);
        case EMPLOYEE -> userService.findAll(true);
        case VOLUNTEER -> userService.findAll(false);
        case SPECIFIC -> userService.findByUsername(selectUser.getValue()).stream().collect(Collectors.toList());
        };
    }

}
