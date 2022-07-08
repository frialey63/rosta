package org.pjp.rosta.ui.view.user;

import javax.annotation.security.RolesAllowed;

import org.pjp.rosta.model.User;
import org.pjp.rosta.service.RostaService;
import org.pjp.rosta.service.UserService;
import org.pjp.rosta.service.UserService.ExistingUser;
import org.pjp.rosta.ui.view.AbstractView;
import org.pjp.rosta.ui.view.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.CrudOperationException;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@RolesAllowed("ADMIN")
@PageTitle("User Management")
@Route(value = "user", layout = MainLayout.class)
public class UserManagementView extends AbstractView implements AfterNavigationObserver {

    private static final long serialVersionUID = -8981630272855085797L;

    private static final boolean SHOW_NOTIFICATIONS = true;

    private final GridCrud<User> crud = new GridCrud<>(User.class);

    @Autowired
    private UserService userService;

    @Autowired
    private RostaService rostaService;

    public UserManagementView() {

        // grid configuration
        crud.getGrid().setColumns("username", "admin", "name", "enabled", "email", "notifications", "employee", "keyholder");
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
        crud.getCrudFormFactory().setVisibleProperties("username", "name", "email", "admin", "enabled", "notifications", "employee", "keyholder");
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "username", "name", "email", "admin", "enabled", "notifications", "employee", "keyholder");
        crud.getCrudFormFactory().setShowNotifications(SHOW_NOTIFICATIONS);

        // logic configuration
        crud.setOperations(
                () -> userService.findAll(),
                user -> addUser(userService, user),
                user -> updateUser(userService, user),
                user -> deleteUser(rostaService, user)
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

    private void deleteUser(RostaService service, User user) {
        service.deleteUser(user);
    }

    private User updateUser(UserService service, User user) {
        try {
            return service.save(user);
        } catch (ExistingUser e) {
            throw new CrudOperationException("A user with this name already exists");
        }
    }

    private User addUser(UserService service, User user) {
        try {
            return service.save(user);
        } catch (ExistingUser e) {
            throw new CrudOperationException("A user with this name already exists");
        }
    }

}
