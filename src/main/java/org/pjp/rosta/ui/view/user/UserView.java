package org.pjp.rosta.ui.view.user;

import org.pjp.rosta.model.User;
import org.pjp.rosta.service.UserService;
import org.pjp.rosta.service.UserService.ExistingUser;
import org.pjp.rosta.service.UserService.UserInUsage;
import org.pjp.rosta.ui.view.MainLayout;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.CrudOperationException;
import org.vaadin.crudui.crud.impl.GridCrud;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@PageTitle("User")
@Route(value = "user", layout = MainLayout.class)
public class UserView extends VerticalLayout {

    private static final long serialVersionUID = -8981630272855085797L;

    // @see https://vaadin.com/directory/component/crud-ui-add-on

    public UserView(UserService userService) {
        // crud instance
        GridCrud<User> crud = new GridCrud<>(User.class);

        // grid configuration
        crud.getGrid().setColumns("name", "email", "employee");
        crud.getGrid().setColumnReorderingAllowed(true);
        crud.addUpdateButtonColumn();

        // form configuration
        crud.getCrudFormFactory().setUseBeanValidation(true);
        crud.getCrudFormFactory().setVisibleProperties("name", "email", "employee");
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "name", "email", "employee");

        // layout configuration
        setSizeFull();
        add(crud);
        crud.setFindAllOperationVisible(false);

        // logic configuration
        crud.setOperations(
                () -> userService.findAll(),
                user -> addUser(userService, user),
                user -> updateUser(userService, user),
                user -> deleteUser(userService, user)
        );
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
