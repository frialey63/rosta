package org.pjp.rosta.ui.view.user;

import javax.annotation.security.RolesAllowed;

import org.pjp.rosta.model.User;
import org.pjp.rosta.service.RostaService;
import org.pjp.rosta.service.UserService.ExistingUser;
import org.pjp.rosta.ui.view.AbstractView;
import org.pjp.rosta.ui.view.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.CrudOperationException;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@RolesAllowed("ROLE_MANAGER")
@PageTitle("User Management")
@Route(value = "user", layout = MainLayout.class)
public class UserManagementView extends AbstractView implements AfterNavigationObserver {

    private static final long serialVersionUID = -8981630272855085797L;

    private static final boolean SHOW_NOTIFICATIONS = true;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final GridCrud<User> crud = new GridCrud<>(User.class);

    @Value("${test.manage.password:false}")
    private boolean testManagePassword;

    @Autowired
    private RostaService rostaService;

    public UserManagementView() {
        super();

        // grid configuration
        crud.getGrid().setColumns("username", "name", "userRole", "enabled", "lastLoggedInStr", "employee", "keyholder", "notifications");
        crud.getGrid().setColumnReorderingAllowed(true);

        crud.setFindAllOperationVisible(false);
        crud.setWidth("98%");
        crud.addUpdateButtonColumn();
        crud.setShowNotifications(SHOW_NOTIFICATIONS);

        crud.setCrudFormFactory(new DefaultCrudFormFactory<>(User.class) {
            private static final long serialVersionUID = -1958239881821633214L;

            @Override
            public String buildCaption(CrudOperation operation, User user) {
                if (operation == CrudOperation.DELETE) {
                    return "Are you sure you want to delete this user? Associated shifts and/or days will also be deleted!";
                }

                return super.buildCaption(operation, user);
            }
        });

        // form configuration
        crud.getCrudFormFactory().setUseBeanValidation(true);
        crud.getCrudFormFactory().setVisibleProperties("username", "name", "email", "telephone", "emergencyName", "emergencyTelephone", "userRole", "enabled", "employee", "keyholder", "notifications");
        crud.getCrudFormFactory().setShowNotifications(SHOW_NOTIFICATIONS);

        // logic configuration
        crud.setOperations(
                () -> userService.findAll(),
                user -> addUser(user),
                user -> updateUser(user),
                user -> deleteUser(user)
        );

        crud.getGrid().setSelectionMode(SelectionMode.SINGLE);
        crud.getGrid().addSelectionListener(l -> {
            crud.setUpdateButtonColumnEnabled(false);

            l.getFirstSelectedItem().ifPresent(selUser -> {
                Button updateButton = crud.getUpdateButton(selUser);
                if (updateButton != null) {
                    updateButton.setEnabled(true);
                }
            });
        });

        // layout configuration
        setMargin(true);
        setPadding(false);
        setSizeFull();

        Span helpText = new Span("The manager(s) cannot operate as an employee or volunteer ('employee' and 'keyholder' are ignored).");
        helpText.getStyle().set("font-style", "italic");

        setHorizontalComponentAlignment(Alignment.START, crud);
        add(crud, helpText);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        super.afterNavigation(event);

        if (testManagePassword) {
            crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD,
                    "username", "password", "name", "email", "telephone", "emergencyName", "emergencyTelephone", "userRole", "enabled", "employee", "keyholder", "notifications");
        } else {
            crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD,
                    "username", "name", "email", "telephone", "emergencyName", "emergencyTelephone", "userRole", "enabled", "employee", "keyholder", "notifications");
        }

        crud.refreshGrid();
    }

    private void deleteUser(User user) {
        rostaService.deleteUser(user);
    }

    private User updateUser(User user) {
        try {
            return userService.save(user);
        } catch (ExistingUser e) {
            throw new CrudOperationException("A user with this name already exists");
        }
    }

    private User addUser(User user) {
        try {
            if (testManagePassword) {
                user.setPassword("{bcrypt}" + passwordEncoder.encode(user.getPassword()));
                user.setPasswordChange(true);
            }

            return userService.save(user);
        } catch (ExistingUser e) {
            throw new CrudOperationException("A user with this name already exists");
        }
    }

}
