package org.pjp.rosta.ui.view.profile;

import javax.annotation.security.RolesAllowed;

import org.pjp.rosta.model.User;
import org.pjp.rosta.security.RostaUserPrincipal;
import org.pjp.rosta.security.SecurityUtil;
import org.pjp.rosta.service.UserService;
import org.pjp.rosta.service.UserService.ExistingUser;
import org.pjp.rosta.ui.util.CompactHorizontalLayout;
import org.pjp.rosta.ui.view.AbstractView;
import org.pjp.rosta.ui.view.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.formlayout.FormLayout.ResponsiveStep;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@RolesAllowed("USER")
@PageTitle("Profile")
@Route(value = "profile", layout = MainLayout.class)
public class ProfileView extends AbstractView implements AfterNavigationObserver {

    private static final long serialVersionUID = -2024122079440962290L;

    private static final int MAX_NAME_LEN = 50;

    private static final int MAX_TELEPHONE_LEN = 20;

    private final TextField username = new TextField("Username");

    private final TextField name = new TextField("Name");

    private final TextField email = new TextField("Email");

    private final TextField telephone = new TextField("Telephone");

    private final TextField emergencyName = new TextField("Emergency Name");

    private final TextField emergencyTelephone = new TextField("Emergency Telephone");

    private final Checkbox employee = new Checkbox("Employee (Leave unchecked for volunteer)");

    private final Checkbox notifications = new Checkbox("Notifications for missing cover");

    private final Binder<User> binder = new BeanValidationBinder<>(User.class);

    private User user;

    @Autowired
    private SecurityUtil securityUtil;

    @Autowired
    private UserService userService;

    public ProfileView() {
        username.setMaxLength(20);
        name.setMaxLength(MAX_NAME_LEN);
        email.setMaxLength(50);
        telephone.setMaxLength(MAX_TELEPHONE_LEN);
        emergencyName.setMaxLength(MAX_NAME_LEN);
        emergencyTelephone.setMaxLength(MAX_TELEPHONE_LEN);

        binder.bindInstanceFields(this);

        FormLayout formLayout = new FormLayout();
        formLayout.add(username, name, email, telephone, emergencyName, emergencyTelephone, employee, notifications);
        formLayout.setResponsiveSteps(
                // Use one column by default
                new ResponsiveStep("0", 1),
                // Use two columns, if layout's width exceeds 500px
                new ResponsiveStep("500px", 2));
        // Stretch the username field over 2 columns
        formLayout.setColspan(username, 1);

        Button save = new Button("Save", e -> {
             try {
                 boolean usernameChanged = !user.getUsername().equals(username.getValue());

                 binder.writeBean(user);
                 userService.save(user);

                 Notification.show("Profile saved");

                 if (usernameChanged) {
                     ((RostaUserPrincipal) securityUtil.getAuthenticatedUser()).setUsername(user.getUsername());
                 }
             } catch (ValidationException ex) {
                 // nothing to do
             } catch (ExistingUser ex) {
                 Notification.show("Unable to save profile, check username for uniqueness").addThemeVariants(NotificationVariant.LUMO_ERROR);
                 populateUser();
             }
        });
        Button reset = new Button("Reset", e -> binder.readBean(user));

        binder.addStatusChangeListener(event -> {
            boolean isValid = event.getBinder().isValid();
            boolean hasChanges = event.getBinder().hasChanges();

            save.setEnabled(hasChanges && isValid);
            reset.setEnabled(hasChanges);
        });

        HorizontalLayout controls = new CompactHorizontalLayout(save, reset);
        controls.setJustifyContentMode(JustifyContentMode.CENTER);
        controls.setWidthFull();

        add(formLayout, controls);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        populateUser();
    }

    private void populateUser() {
        String username = securityUtil.getAuthenticatedUser().getUsername();

        userService.findByUsername(username).ifPresent(user -> {
            this.user = user;
            binder.readBean(user);
        });
    }

}
