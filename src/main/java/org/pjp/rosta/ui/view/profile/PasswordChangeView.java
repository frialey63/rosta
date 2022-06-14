package org.pjp.rosta.ui.view.profile;

import javax.annotation.security.RolesAllowed;

import org.pjp.rosta.model.User;
import org.pjp.rosta.security.SecurityUtil;
import org.pjp.rosta.service.UserService;
import org.pjp.rosta.ui.view.CompactHorizontalLayout;
import org.pjp.rosta.ui.view.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@RolesAllowed("USER")
@PageTitle("Change Password")
@Route(value = "password", layout = MainLayout.class)
public class PasswordChangeView extends VerticalLayout implements AfterNavigationObserver {

    private static final long serialVersionUID = -2024122079440962290L;

    public static class PasswordBean {
        private String password;
        private String confirmPassword;

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getConfirmPassword() {
            return confirmPassword;
        }

        public void setConfirmPassword(String confirmPassword) {
            this.confirmPassword = confirmPassword;
        }
    }

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final PasswordField password = new PasswordField("Password");

    private final PasswordField confirmPassword = new PasswordField("Confirm Password");

    private Span errorMessageField = new Span();

    private final Binder<PasswordBean> binder = new BeanValidationBinder<>(PasswordBean.class);

    private PasswordBean passwordBean;

    private boolean enablePasswordValidation;

    private User user;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private UserService userService;

    public PasswordChangeView() {
        password.setMaxLength(20);
        confirmPassword.setMaxLength(password.getMaxLength());

        binder.bindInstanceFields(this);

        // Set the label where bean-level error messages go
        binder.setStatusLabel(errorMessageField);

        // A custom validator for password fields
        binder.forField(password).withValidator(this::passwordValidator).bind("password");

        // The second password field is not connected to the Binder, but we
        // want the binder to re-check the password validator when the field
        // value changes. The easiest way is just to do that manually.
        confirmPassword.addValueChangeListener(e -> {
            // The user has modified the second field, now we can validate and show errors.
            // See passwordValidator() for how this flag is used.
            enablePasswordValidation = true;

            binder.validate();
        });

        FormLayout formLayout = new FormLayout();
        formLayout.add(password, confirmPassword);
        formLayout.setResponsiveSteps(
                // Use one column by default
                new ResponsiveStep("0", 1),
                // Use two columns, if layout's width exceeds 500px
                new ResponsiveStep("500px", 2));

        Button save = new Button("Save", e -> {
             try {
                 binder.writeBean(passwordBean);

                 user.setPassword("{bcrypt}" + passwordEncoder.encode(passwordBean.getPassword()));
                 userService.save(user);
             } catch (ValidationException ex) {
                 // TODO
             }
        });
        Button reset = new Button("Reset", e -> binder.readBean(passwordBean));

        binder.addStatusChangeListener(event -> {
            boolean isValid = event.getBinder().isValid();
            boolean hasChanges = event.getBinder().hasChanges();

            save.setEnabled(hasChanges && isValid);
            reset.setEnabled(hasChanges);
        });

        HorizontalLayout controls = new CompactHorizontalLayout(save, reset);
        controls.setJustifyContentMode(JustifyContentMode.CENTER);
        controls.setWidthFull();

        add(formLayout, errorMessageField, controls);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        String username = securityUtil.getAuthenticatedUser().getUsername();

        userService.findByUsername(username).ifPresent(user -> {
            this.user = user;

            passwordBean = new PasswordBean();
            binder.readBean(passwordBean);
        });
    }

    /**
     * Method to validate that:
     * <p>
     * 1) Password is at least 8 characters long
     * <p>
     * 2) Values in both fields match each other
     */
    private ValidationResult passwordValidator(String pass1, ValueContext ctx) {
        /*
         * Just a simple length check. A real version should check for password
         * complexity as well!
         */

        if (pass1 == null || pass1.length() < 8) {
            return ValidationResult.error("Password should be at least 8 characters long");
        }

        if (!enablePasswordValidation) {
            // user hasn't visited the field yet, so don't validate just yet, but next time.
            enablePasswordValidation = true;
            return ValidationResult.ok();
        }

        String pass2 = confirmPassword.getValue();

        if (pass1 != null && pass1.equals(pass2)) {
            return ValidationResult.ok();
        }

        return ValidationResult.error("Passwords do not match");
    }

}
