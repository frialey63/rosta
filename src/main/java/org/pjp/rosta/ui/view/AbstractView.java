package org.pjp.rosta.ui.view;

import java.util.Optional;

import org.pjp.rosta.model.User;
import org.pjp.rosta.security.RostaUserPrincipal;
import org.pjp.rosta.security.SecurityUtil;
import org.pjp.rosta.service.UserService;
import org.pjp.rosta.ui.view.profile.PasswordChangeView;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

public abstract class AbstractView extends VerticalLayout implements BeforeEnterObserver, AfterNavigationObserver {

    private static final long serialVersionUID = 2208170317290395433L;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private UserService userService;

    protected String getUsername() {
        return securityUtil.getAuthenticatedUser().getUsername();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (((RostaUserPrincipal) securityUtil.getAuthenticatedUser()).isPasswordChange()) {
            event.rerouteTo(PasswordChangeView.class);
        }
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        Optional<User> optUser = userService.findByUsername(getUsername());

        optUser.ifPresent(user -> {
            if (!user.isAdmin() && user.hasIncompleteProfile()) {
                Notification.show("Please complete your profile").addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
    }

}
