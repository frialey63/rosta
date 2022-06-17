package org.pjp.rosta.ui.view.about;

import javax.annotation.security.PermitAll;

import org.pjp.rosta.ui.view.AbstractView;
import org.pjp.rosta.ui.view.MainLayout;
import org.springframework.beans.factory.annotation.Value;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PermitAll
@PageTitle("About")
@Route(value = "about", layout = MainLayout.class)
public class AboutView extends AbstractView implements AfterNavigationObserver {

    private static final long serialVersionUID = -2468462280700402027L;

    @Value("${application.version}")
    private String applicationVersion;

    private final Paragraph paragraph = new Paragraph();

    public AboutView() {
        setSpacing(false);

        Image img = new Image("images/raf-manston-logo.png", "logo");
        img.setWidth("200px");
        add(img);

        add(new H2("RAF Manston History Museum"));
        add(paragraph);

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        paragraph.setText("Shop Rota App version " + applicationVersion);
    }

}
