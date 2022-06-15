package org.pjp.rosta.ui.view.login;

import org.apache.logging.log4j.util.Strings;
import org.pjp.rosta.service.UserService;
import org.pjp.rosta.ui.view.CompactHorizontalLayout;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.componentfactory.EnhancedDialog;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("Login")
@Route("login")
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private static final long serialVersionUID = 4838429459481914860L;

    private static HorizontalLayout getDialogFooter(EnhancedDialog dialog) {
        Span filler = new Span();

        HorizontalLayout footer = new CompactHorizontalLayout(filler, new Button("OK", e -> dialog.close()));
        footer.setAlignItems(Alignment.STRETCH);
        footer.setFlexGrow(1, filler);

        return footer;
    }

    private final LoginForm login = new LoginForm();

    @Autowired
    private UserService userService;

    public LoginView() {
        addClassName("login-view");
        setSizeFull();

        setJustifyContentMode(JustifyContentMode.CENTER);
        setAlignItems(Alignment.CENTER);

        login.setAction("login");
        login.addLoginListener(l -> {
            userService.loggedIn(l.getUsername());
        });
        login.addForgotPasswordListener(l -> {
            UI.getCurrent().getPage()
                .executeJs("return document.getElementById('vaadinLoginUsername').value;")
                .then(String.class, username -> {
                    if (Strings.isNotBlank(username)) {
                        if (userService.forgotPassword(username)) {
                            EnhancedDialog dialog = new EnhancedDialog();
                            dialog.setHeader("Forgot Password");
                            dialog.setContent(new Span("A temporary password has been sent to your email address."));
                            dialog.setFooter(getDialogFooter(dialog));
                            dialog.open();
                        }
                    }
                });
        });

        add(new H1("Shop Rota App"), login);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        if (beforeEnterEvent.getLocation().getQueryParameters().getParameters().containsKey("error")) {
            login.setError(true);
        }
    }
}