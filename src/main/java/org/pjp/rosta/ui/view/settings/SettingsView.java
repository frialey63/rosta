package org.pjp.rosta.ui.view.settings;

import javax.annotation.security.PermitAll;

import org.pjp.rosta.ui.view.AbstractView;
import org.pjp.rosta.ui.view.MainLayout;
import org.springframework.beans.factory.annotation.Value;
import org.vaadin.stefan.table.Table;
import org.vaadin.stefan.table.TableRow;

import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PermitAll
@PageTitle("Settings")
@Route(value = "settings", layout = MainLayout.class)
public class SettingsView extends AbstractView implements AfterNavigationObserver {

    private static final long serialVersionUID = -4308583634742400882L;

    @Value("${check.rosta.director.email}")
    private String checkRostaDirectorEmail;

    @Value("${check.rosta.cron}")
    private String checkRostaCron;

    private Table table;

    public SettingsView() {
        setSpacing(false);

        add(new H1("Settings for Notifications"));

        table = new Table();
        table.getElement().setAttribute("border", "1px solid black");
        table.setWidthFull();

        TableRow headerRow = table.addRow();
        headerRow.addHeaderCell().setText("Setting");
        headerRow.addHeaderCell().setText("Value");

        add(table);

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        TableRow detailsRow = table.addRow();
        detailsRow.addDataCell().setText("check.rosta.director.email");
        detailsRow.addDataCell().setText(checkRostaDirectorEmail);

        detailsRow = table.addRow();
        detailsRow.addDataCell().setText("check.rosta.cron");
        detailsRow.addDataCell().setText(checkRostaCron);
    }

}
