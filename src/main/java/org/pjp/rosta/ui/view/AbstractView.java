package org.pjp.rosta.ui.view;

import org.pjp.rosta.security.RostaUserPrincipal;
import org.pjp.rosta.security.SecurityUtil;
import org.pjp.rosta.ui.view.profile.PasswordChangeView;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;

public abstract class AbstractView extends VerticalLayout implements BeforeEnterObserver {

    private static final long serialVersionUID = 2208170317290395433L;

    @Autowired
    private SecurityUtil securityUtil;

    protected String getUsername() {
        return securityUtil.getAuthenticatedUser().getUsername();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (((RostaUserPrincipal) securityUtil.getAuthenticatedUser()).isPasswordChange()) {
            event.rerouteTo(PasswordChangeView.class);
        }
    }

}
