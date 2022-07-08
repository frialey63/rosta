package org.pjp.rosta.ui.view.profile;

import java.io.IOException;

import javax.annotation.security.PermitAll;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.pjp.rosta.model.User;
import org.pjp.rosta.security.RostaUserPrincipal;
import org.pjp.rosta.security.SecurityUtil;
import org.pjp.rosta.service.UserService;
import org.pjp.rosta.ui.util.CompactHorizontalLayout;
import org.pjp.rosta.ui.util.CompactVerticalLayout;
import org.pjp.rosta.ui.view.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.nulabinc.zxcvbn.StandardDictionaries;
import com.nulabinc.zxcvbn.StandardKeyboards;
import com.nulabinc.zxcvbn.Strength;
import com.nulabinc.zxcvbn.Zxcvbn;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.component.textfield.Autocomplete;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@CssImport(themeFor = "vaadin-progress-bar", value = "./styles/dynamically-change-progressbar-color.css")
@PermitAll
@PageTitle("Change Password")
@Route(value = "password", layout = MainLayout.class)
public class PasswordChangeView extends VerticalLayout implements AfterNavigationObserver {

    private static final long serialVersionUID = -2024122079440962290L;

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordChangeView.class);

    public static class PasswordBean {
        @Size(min = 8, max = 20)
        @NotNull
        private String oldPassword;

        @Size(min = 8, max = 20)
        @NotNull
        private String password;

        private String confirmPassword;

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

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

    static {
        try {
            StandardDictionaries.loadAllDictionaries();
            StandardKeyboards.loadAllKeyboards();
        } catch (IOException e) {
            LOGGER.warn("failed to load Zxcvbn files", e);
        }
    }

    private final Zxcvbn zxcvbn = new Zxcvbn();

    private final PasswordField oldPassword = new PasswordField("Old Password");

    private final PasswordField password = new PasswordField("Password");

    private final PasswordField confirmPassword = new PasswordField("Confirm Password");

    private final ProgressBar strengthBarIndicator = new ProgressBar();

    private final Span errorMessageField = new Span();

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

        password.setAutocomplete(Autocomplete.NEW_PASSWORD);
        confirmPassword.setAutocomplete(Autocomplete.NEW_PASSWORD);

        password.addValueChangeListener(l -> {
            Strength strength = zxcvbn.measure(l.getValue());
            int score = strength.getScore();

            strengthBarIndicator.setValue(score);

            if (score < 2) {
                strengthBarIndicator.getStyle().set("--progress-color", "#ff0000");
            } else if (score > 2) {
                strengthBarIndicator.getStyle().set("--progress-color", "#00ff00");
            } else {
                strengthBarIndicator.getStyle().set("--progress-color", "#0000ff");
            }
        });
        password.setValueChangeMode(ValueChangeMode.EAGER);

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

        HorizontalLayout oldPassword = getOldPassword();

        FormLayout formLayout = new FormLayout();
        formLayout.add(oldPassword, password, confirmPassword);
        formLayout.setResponsiveSteps(
                // Use one column by default
                new ResponsiveStep("0", 1),
                // Use two columns, if layout's width exceeds 500px
                new ResponsiveStep("500px", 2));
        formLayout.setColspan(oldPassword, 2);

        Button save = new Button("Save", e -> {
             try {
                 binder.writeBean(passwordBean);

                 if (userService.changePassword(user, passwordBean.getOldPassword(), passwordBean.getPassword())) {
                     ((RostaUserPrincipal) securityUtil.getAuthenticatedUser()).setPasswordChange(false);
                     Notification.show("Password changed");
                 } else {
                     Notification.show("Password not changed, check old password");
                 }

             } catch (ValidationException ex) {
                 // nothing to do
             }
        });
        Button reset = new Button("Reset", e -> binder.readBean(passwordBean));

        binder.addStatusChangeListener(event -> {
            boolean isValid = event.getBinder().isValid();
            boolean hasChanges = event.getBinder().hasChanges();

            save.setEnabled(hasChanges && isValid);
            reset.setEnabled(hasChanges);
        });

        VerticalLayout strengthBar = getStrengthBar();

        HorizontalLayout controls = new CompactHorizontalLayout(save, reset);
        controls.setJustifyContentMode(JustifyContentMode.CENTER);
        controls.setWidthFull();

        add(formLayout, strengthBar, errorMessageField, controls);
    }

    private HorizontalLayout getOldPassword() {
        Span filler = new Span();
        setFlexGrow(1, filler);

        HorizontalLayout hl = new CompactHorizontalLayout(oldPassword, filler);
        oldPassword.setWidth("50%");
        return hl;
    }

    private VerticalLayout getStrengthBar() {
        Div strengthBarLabel = new Div();
        strengthBarLabel.setText("Password strength");

        strengthBarIndicator.setMin(0);
        strengthBarIndicator.setMax(4);

        VerticalLayout vl = new CompactVerticalLayout(strengthBarLabel, strengthBarIndicator);
        vl.setWidth("50%");
        return vl;
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
     * 2) Password is sufficiently strong
     * <p>
     * 3) Values in both fields match each other
     */
    private ValidationResult passwordValidator(String pass1, ValueContext ctx) {
        if (pass1 == null || pass1.length() < 8) {
            return ValidationResult.error("Password should be at least 8 characters long");
        }

        Strength strength = zxcvbn.measure(pass1);
        if (strength.getScore() < 2) {
            return ValidationResult.error("Password is not of sufficient strength: " + strength.getFeedback().getWarning());
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
