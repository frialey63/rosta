package org.pjp.rosta.ui.view.settings;

import javax.annotation.security.PermitAll;

import org.pjp.rosta.ui.view.AbstractView;
import org.pjp.rosta.ui.view.MainLayout;
import org.springframework.beans.factory.annotation.Value;
import org.vaadin.stefan.table.Table;
import org.vaadin.stefan.table.TableRow;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PermitAll
@PageTitle("Settings")
@Route(value = "settings", layout = MainLayout.class)
public class SettingsView extends AbstractView implements AfterNavigationObserver {

    private static final long serialVersionUID = -4308583634742400882L;

    @Value("${spring.mail.host}")
    private String springMailHost;

    @Value("${spring.mail.port}")
    private String springMailPort;

    @Value("${spring.mail.username}")
    private String springMailUsername;

    @Value("${spring.mail.password}")
    private String springMailPassword;

    @Value("${check.rosta.director.email}")
    private String checkRostaDirectorEmail;

    @Value("${check.rosta.cron}")
    private String checkRostaCron;

    private Table table;

    public SettingsView() {
        setSpacing(false);

        add(new H2("Notification Settings"));

        table = new Table();
        table.getElement().setAttribute("border", "1px solid black");
        table.setWidthFull();

        TableRow headerRow = table.addRow();
        headerRow.addHeaderCell().setText("Setting");
        headerRow.addHeaderCell().setText("Value");

        add(table);

        Span helpText = new Span("These settings can be modified by your System Administrator.");
        helpText.getStyle().set("font-style", "italic");

        add(new Paragraph(), helpText);
        setHorizontalComponentAlignment(Alignment.STRETCH, helpText);

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.START);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        TableRow detailsRow;

        detailsRow = table.addRow();
        detailsRow.addDataCell().setText("spring.mail.host");
        detailsRow.addDataCell().setText(springMailHost);

        detailsRow = table.addRow();
        detailsRow.addDataCell().setText("spring.mail.port");
        detailsRow.addDataCell().setText(springMailPort);

        detailsRow = table.addRow();
        detailsRow.addDataCell().setText("spring.mail.username");
        detailsRow.addDataCell().setText(springMailUsername);

        detailsRow = table.addRow();
        detailsRow.addDataCell().setText("spring.mail.password");
        detailsRow.addDataCell().setText(springMailPassword);

        detailsRow = table.addRow();
        detailsRow.addDataCell().setText("check.rosta.director.email");
        detailsRow.addDataCell().setText(checkRostaDirectorEmail);

        detailsRow = table.addRow();
        detailsRow.addDataCell().setText("check.rosta.cron");
        detailsRow.addDataCell().setText(checkRostaCron);
    }

}
