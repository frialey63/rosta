package org.pjp.rosta.ui.view.maninfo;

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
@PageTitle("Management Information")
@Route(value = "maninfo", layout = MainLayout.class)
public class ManagementInformationView extends AbstractView implements AfterNavigationObserver {

    private static final long serialVersionUID = 4484038138117594303L;

    public ManagementInformationView() {
        setSpacing(false);

        add(new H2("TODO"));
        add(new Paragraph("Query periods worked, holidays and absences by employee/volunteer and time period."));

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
    }

}
