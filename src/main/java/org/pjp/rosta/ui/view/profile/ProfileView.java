package org.pjp.rosta.ui.view.profile;

import javax.annotation.security.RolesAllowed;

import org.pjp.rosta.ui.view.MainLayout;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@RolesAllowed("USER")
@PageTitle("Profile")
@Route(value = "profile", layout = MainLayout.class)
public class ProfileView extends VerticalLayout implements AfterNavigationObserver {

    private static final long serialVersionUID = -2024122079440962290L;

    public ProfileView() {
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {

    }

}
