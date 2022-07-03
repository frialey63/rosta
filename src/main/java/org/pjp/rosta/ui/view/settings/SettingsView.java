package org.pjp.rosta.ui.view.settings;

import javax.annotation.security.PermitAll;

import org.pjp.rosta.ui.view.AbstractView;
import org.pjp.rosta.ui.view.MainLayout;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PermitAll
@PageTitle("Settings")
@Route(value = "settings", layout = MainLayout.class)
public class SettingsView extends AbstractView implements AfterNavigationObserver {

    private static final long serialVersionUID = -4308583634742400882L;

    public SettingsView() {
        setSpacing(false);

        add(new H2("TODO"));
        add(new Paragraph("Settings for notifications, etc."));

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
    }

}
