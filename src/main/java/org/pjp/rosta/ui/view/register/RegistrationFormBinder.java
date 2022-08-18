package org.pjp.rosta.ui.view.register;

import org.pjp.rosta.bean.UserBean;
import org.pjp.rosta.service.UserService;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.ValueContext;

class RegistrationFormBinder {

    private UserService userService;

    private RegistrationForm registrationForm;

    /**
     * Flag for disabling first run for password validation
     */
    private boolean enablePasswordValidation;

    public RegistrationFormBinder(RegistrationForm registrationForm, UserService userService) {
        this.registrationForm = registrationForm;
        this.userService = userService;
    }

    /**
     * Method to add the data binding and validation logics
     * to the registration form
     */
    public void addBindingAndValidation() {
        BeanValidationBinder<UserBean> binder = new BeanValidationBinder<>(UserBean.class);

        // The bindInstanceFields method facilitates automatic data binding and validation:
        // it automatically matches the fields of the RegistrationForm object to the
        // properties of the UserDetails object based on their names.
        binder.bindInstanceFields(registrationForm);

        // A custom validator for password fields to handle password-confirmation logic
        binder.forField(registrationForm.getPasswordField()).withValidator(this::passwordValidator).bind("password");

        // The second password field is not connected to the Binder, but we
        // want the binder to re-check the password validator when the field
        // value changes. The easiest way is just to do that manually.
        registrationForm.getPasswordConfirmField().addValueChangeListener(e -> {
            // The user has modified the second field, now we can validate and show errors.
            // See passwordValidator() for how this flag is used.
            enablePasswordValidation = true;

            binder.validate();
        });

        // Set the label where bean-level error messages go
        binder.setStatusLabel(registrationForm.getErrorMessageField());

        // And finally the submit button
        registrationForm.getSubmitButton().addClickListener(event -> {
            try {

                // Create empty bean to store the details into
                UserBean userBean = new UserBean();

                // Run validators and write the values to the bean
                binder.writeBean(userBean);

                // Typically, you would here call backend to store the bean
                String username = userService.registerUser(userBean);

                // Show success message if everything went well
                showSuccess(userBean, username);

                UI.getCurrent().navigate("login");

            } catch (ValidationException exception) {
                // validation errors are already visible for each field,
                // and bean-level errors are shown in the status label.

                // We could show additional messages here if we want, do logging, etc.

            }
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

        String pass2 = registrationForm.getPasswordConfirmField().getValue();

        if (pass1 != null && pass1.equals(pass2)) {
            return ValidationResult.ok();
        }

        return ValidationResult.error("Passwords do not match");
    }

    /**
     * We call this method when form submission has succeeded
     */
    private void showSuccess(UserBean userBean, String username) {
        Notification notification = Notification.show("Data saved, welcome " + userBean.getFirstName() + ". You may now login using the username of " + username, 60_000, Position.BOTTOM_START);

        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
