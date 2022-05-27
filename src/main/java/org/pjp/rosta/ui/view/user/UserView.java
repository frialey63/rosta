package org.pjp.rosta.ui.view.user;

import org.pjp.rosta.model.User;
import org.pjp.rosta.service.UserService;
import org.pjp.rosta.ui.view.MainLayout;
import org.vaadin.crudui.crud.CrudOperation;
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
                user -> userService.save(user),
                user -> userService.save(user),
                user -> userService.delete(user)
        );
    }

}
