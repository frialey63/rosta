package org.pjp.rosta.ui.view.maninfo;

import javax.annotation.security.PermitAll;

import org.pjp.rosta.ui.view.AbstractView;
import org.pjp.rosta.ui.view.CompactHorizontalLayout;
import org.pjp.rosta.ui.view.MainLayout;
import org.pjp.rosta.ui.view.MyDatePicker;
import org.vaadin.stefan.table.Table;
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

    private static final long serialVersionUID = 4484038138117594303L;

    private final RadioButtonGroup<UserType> userType = new RadioButtonGroup<>();

    private final RadioButtonGroup<RangeType> rangeType = new RadioButtonGroup<>();

    private final Select<String> selectUser = new Select<>();

    private final DatePicker startDate = new MyDatePicker("Start Date");
    private final DatePicker endDate = new MyDatePicker("End Date");

    private Table results;

    public ManagementInformationView() {
        setSpacing(false);

        H2 h2 = new H2("Management Information");
        add(h2);
        setHorizontalComponentAlignment(Alignment.CENTER, h2);

        Table table = new Table();
        table.setWidthFull();

        userType.setItems(UserType.values());
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
        headerRow.addHeaderCell().setText("User");
        headerRow.addHeaderCell().setText("Work Periods");
        headerRow.addHeaderCell().setText("Holidays");
        headerRow.addHeaderCell().setText("Absences");

        add(results);

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.START);
        setDefaultHorizontalComponentAlignment(Alignment.START);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {

    }

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {
        TableRow detailsRow = results.addRow();
        detailsRow.addDataCell().setText("Fred Bloggs");
        detailsRow.addDataCell().setText("999");
        detailsRow.addDataCell().setText("999");
        detailsRow.addDataCell().setText("999");
    }

}
