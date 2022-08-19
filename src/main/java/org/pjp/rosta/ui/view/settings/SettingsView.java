package org.pjp.rosta.ui.view.settings;

import javax.annotation.security.RolesAllowed;

import org.pjp.rosta.ui.view.AbstractView;
import org.pjp.rosta.ui.view.MainLayout;
import org.springframework.beans.factory.annotation.Value;
import org.vaadin.stefan.table.Table;
import org.vaadin.stefan.table.TableRow;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@RolesAllowed("MANAGER")
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

    @Value("${check.rota.director.email}")
    private String checkRotaDirectorEmail;

    @Value("${check.rota.cron}")
    private String checkRotaCron;

    private Table table;

    public SettingsView() {
        super();

        setSpacing(false);

        add(new H2("Notification Settings"));

        table = new Table();
        table.getElement().setAttribute("border", "1px solid black");
        table.setWidthFull();

        TableRow headerRow = table.addRow();
        headerRow.addHeaderCell().setText("Setting");
        headerRow.addHeaderCell().setText("Value");

        add(table);

        Paragraph helpText = new Paragraph("These settings can be modified by your System Administrator.");
        helpText.getStyle().set("font-style", "italic");

        Paragraph explainText = new Paragraph("At the specified cron (time) the app performs a rota check for the following week. If there are any periods with less than two workers or periods without a keyholder then an email is sent to all notifiable users to request assistance. A status email is (always) sent to each director.");

        add(new Paragraph(), helpText, explainText);
        setHorizontalComponentAlignment(Alignment.STRETCH, helpText, explainText);

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.START);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        super.afterNavigation(event);

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
        detailsRow.addDataCell().setText("check.rota.director.email");
        detailsRow.addDataCell().setText(checkRotaDirectorEmail);

        detailsRow = table.addRow();
        detailsRow.addDataCell().setText("check.rota.cron");
        detailsRow.addDataCell().setText(checkRotaCron);
    }

}
