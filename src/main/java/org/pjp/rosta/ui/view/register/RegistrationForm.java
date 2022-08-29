package org.pjp.rosta.ui.view.register;

import java.util.stream.Stream;

import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Emphasis;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;

/**
 * Create a FormLayout with all our components. The FormLayout
 * doesn't have any logic (validation, etc.), but it allows us to
 * configure responsiveness from Java code and its defaults looks
 * nicer than just using a Div or a VerticalLayout.
 * <p>
 * This FormLayout itself is added to the MainView, where it is made
 * accessible to the user.
 */
class RegistrationForm extends FormLayout {

    private static final long serialVersionUID = -7379451127997641924L;

    private H3 title;

    private TextField firstName;
    private TextField lastName;

    private EmailField email;

    private PasswordField password;
    private PasswordField passwordConfirm;

    private Span infoField;

    private Span errorMessageField;

    private Button submitButton;

    public RegistrationForm() {
        title = new H3("Signup Form");

        firstName = new TextField("First name");
        lastName = new TextField("Last name");

        email = new EmailField("Email");

        password = new PasswordField("Password");
        passwordConfirm = new PasswordField("Confirm password");

        setRequiredIndicatorVisible(firstName, lastName, email, password,
                passwordConfirm);

        infoField = new Span(new Emphasis("If employee and/or keyholder then inform the manager."));

        errorMessageField = new Span();

        submitButton = new Button("Join the Rota");
        submitButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(title, firstName, lastName, email, password, passwordConfirm, infoField, errorMessageField, submitButton);

        // Max width of the Form
        setMaxWidth("500px");

        // Allow the form layout to be responsive. On device widths 0-490px
        // we have one column, then we have two. Field labels are always on
        // top of the fields.
        setResponsiveSteps(
                new ResponsiveStep("0", 1, ResponsiveStep.LabelsPosition.TOP),
                new ResponsiveStep("490px", 2, ResponsiveStep.LabelsPosition.TOP));


        // These components take full width regardless if we use one column
        // or two (it just looks better that way)
        setColspan(title, 2);
        setColspan(email, 2);
        setColspan(infoField, 2);
        setColspan(errorMessageField, 2);
        setColspan(submitButton, 2);
    }

    public PasswordField getPasswordField() { return password; }

    public PasswordField getPasswordConfirmField() { return passwordConfirm; }

    public Span getErrorMessageField() { return errorMessageField; }

    public Button getSubmitButton() { return submitButton; }

    private void setRequiredIndicatorVisible(HasValueAndElement<?, ?>... components) {
        Stream.of(components).forEach(comp -> comp.setRequiredIndicatorVisible(true));
    }
}
