package org.pjp.rosta.ui.view.user;

import javax.annotation.security.RolesAllowed;

import org.pjp.rosta.model.User;
import org.pjp.rosta.service.UserService;
import org.pjp.rosta.service.UserService.ExistingUser;
import org.pjp.rosta.service.UserService.UserInUsage;
import org.pjp.rosta.ui.view.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.CrudOperationException;
import org.vaadin.crudui.crud.impl.GridCrud;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@RolesAllowed("ADMIN")
@PageTitle("User Management")
@Route(value = "user", layout = MainLayout.class)
public class UserManagementView extends VerticalLayout implements AfterNavigationObserver {

    private static final long serialVersionUID = -8981630272855085797L;

    private final GridCrud<User> crud = new GridCrud<>(User.class);

    @Autowired
    private UserService userService;

    public UserManagementView() {

        // grid configuration
        crud.getGrid().setColumns("username", "admin", "name", "enabled", "email", "notifications", "employee");
        crud.getGrid().setColumnReorderingAllowed(true);
        crud.setFindAllOperationVisible(false);
        crud.setWidth("98%");
        crud.addUpdateButtonColumn();
        crud.setShowNotifications(false);

        // form configuration
        crud.getCrudFormFactory().setUseBeanValidation(true);
        crud.getCrudFormFactory().setVisibleProperties("username", "name", "email", "admin", "enabled", "notifications", "employee");
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "username", "name", "email", "admin", "enabled", "notifications", "employee");
        crud.getCrudFormFactory().setShowNotifications(false);

        // logic configuration
        crud.setOperations(
                () -> userService.findAll(),
                user -> addUser(userService, user),
                user -> updateUser(userService, user),
                user -> deleteUser(userService, user)
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

        setHorizontalComponentAlignment(Alignment.START, crud);
        add(crud);
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        crud.refreshGrid();
    }

    private void deleteUser(UserService userService, User user) {
        try {
            userService.delete(user);
        } catch (UserInUsage e) {
            throw new CrudOperationException("This user has rosta(s) defined");
        }
    }

    private User updateUser(UserService userService, User user) {
        try {
            return userService.save(user);
        } catch (ExistingUser e) {
            throw new CrudOperationException("A user with this name already exists");
        }
    }

    private User addUser(UserService userService, User user) {
        try {
            return userService.save(user);
        } catch (ExistingUser e) {
            throw new CrudOperationException("A user with this name already exists");
        }
    }

}