package org.pjp.rosta.ui.view;

import java.time.LocalDateTime;
import java.util.Optional;

import org.pjp.rosta.model.User;
import org.pjp.rosta.security.RostaUserPrincipal;
import org.pjp.rosta.security.SecurityUtil;
import org.pjp.rosta.service.UserService;
import org.pjp.rosta.ui.view.profile.PasswordChangeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;

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
    protected SecurityUtil securityUtil;

    @Autowired
    protected UserService userService;

    public AbstractView() {
        super();
    }

    protected String getUsername() {
        return securityUtil.getAuthenticatedUser().getUsername();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        UserDetails authenticatedUser = securityUtil.getAuthenticatedUser();

        if (authenticatedUser instanceof RostaUserPrincipal rostaUserPrincipal) {
            if (rostaUserPrincipal.getAndSetFirst(false)) {
                LocalDateTime lastLoggedIn = userService.updateLastLoggedIn(authenticatedUser.getUsername());

                if (lastLoggedIn != null) {
                    Notification.show("Last logged-in at " + lastLoggedIn.format(User.FORMATTER));
                }
            }

            if (rostaUserPrincipal.isPasswordChange()) {
                event.rerouteTo(PasswordChangeView.class);
            }
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
