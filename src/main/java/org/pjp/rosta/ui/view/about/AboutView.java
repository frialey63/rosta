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

    private static final String APP_NAME = "Shop Rota App version ";

    @Value("${about.heading}")
    private String aboutHeading;

    @Value("${about.image}")
    private String aboutImage;

    @Value("${application.version}")
    private String applicationVersion;

    private final Image image = new Image("images/empty-plant.png", "logo");

    private final H2 heading = new H2();

    private final Paragraph appParagraph = new Paragraph();

    public AboutView() {
        super();

        setSpacing(false);

        add(image, heading, appParagraph);

        setSizeFull();
        setJustifyContentMode(JustifyContentMode.CENTER);
        setDefaultHorizontalComponentAlignment(Alignment.CENTER);
        getStyle().set("text-align", "center");
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        super.afterNavigation(event);

        image.setSrc(aboutImage);
        image.setWidth("200px");

        heading.setText(aboutHeading);

        appParagraph.setText(APP_NAME + applicationVersion);
    }

}
