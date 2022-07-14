package org.pjp.rosta.ui.view.document;

import java.io.InputStream;
import java.util.List;

import javax.annotation.security.RolesAllowed;

import org.pjp.rosta.model.ShopDocument;
import org.pjp.rosta.service.DocumentService;
import org.pjp.rosta.service.UserService.ExistingUser;
import org.pjp.rosta.ui.view.AbstractView;
import org.pjp.rosta.ui.view.MainLayout;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.CrudOperationException;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

@RolesAllowed("ADMIN")
@PageTitle("Shop Documents")
@Route(value = "document", layout = MainLayout.class)
public class ShopDocumentView extends AbstractView implements AfterNavigationObserver {

    private static final long serialVersionUID = 2727631739617042032L;

    private static final boolean SHOW_NOTIFICATIONS = true;

    private final GridCrud<ShopDocument> crud = new GridCrud<>(ShopDocument.class);

    private final MemoryBuffer memoryBuffer = new MemoryBuffer();

    @Autowired
    private DocumentService service;

    public ShopDocumentView() {
        // grid configuration
        crud.getGrid().setColumns("title", "filename");
        crud.getGrid().setColumnReorderingAllowed(true);
        crud.setFindAllOperationVisible(false);
        crud.setWidth("98%");
        crud.addUpdateButtonColumn();
        crud.setShowNotifications(SHOW_NOTIFICATIONS);

        crud.setCrudFormFactory(new DefaultCrudFormFactory<>(ShopDocument.class) {
            private static final long serialVersionUID = 6194957473564895897L;

            private Button operationButton;

            @Override
            protected Button buildOperationButton(CrudOperation operation, ShopDocument domainObject, ComponentEventListener<ClickEvent<Button>> clickListener) {
                operationButton = super.buildOperationButton(operation, domainObject, clickListener);
                operationButton.setEnabled(false);
                return operationButton;
            }

            @Override
            protected void configureForm(FormLayout formLayout, List<HasValueAndElement> fields) {
                Upload upload = new Upload(memoryBuffer);

                upload.getElement().addEventListener("file-remove", event -> {
                    operationButton.setEnabled(false);
                });

                upload.addSucceededListener(event -> {
                    operationButton.setEnabled(true);

                    // Get information about the uploaded file
                    InputStream fileData = memoryBuffer.getInputStream();
                    String fileName = event.getFileName();
                    long contentLength = event.getContentLength();
                    String mimeType = event.getMIMEType();

                    // Do something with the file data
                    // processFile(fileData, fileName, contentLength, mimeType);
                });

                formLayout.add(upload);

                formLayout.getChildren().forEach(c -> formLayout.setColspan(c, 2));
            }
        });

        // form configuration
        crud.getCrudFormFactory().setUseBeanValidation(true);
        crud.getCrudFormFactory().setVisibleProperties("title", "filename");
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "title");
        crud.getCrudFormFactory().setShowNotifications(SHOW_NOTIFICATIONS);

        // logic configuration
        crud.setOperations(
                () -> service.findAll(),
                document -> addDocument(document),
                document -> updateDocument(document),
                document -> deleteDocument(document)
        );

        crud.getGrid().setSelectionMode(SelectionMode.SINGLE);
        crud.getGrid().addSelectionListener(l -> {
            crud.setUpdateButtonColumnEnabled(false);

            l.getFirstSelectedItem().ifPresent(selDocument -> {
                Button updateButton = crud.getUpdateButton(selDocument);
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

    private void deleteDocument(ShopDocument document) {
        service.delete(document);
    }

    private ShopDocument updateDocument(ShopDocument document) {
        try {
            return service.save(document);
        } catch (ExistingUser e) {
            throw new CrudOperationException("A document with this name already exists");
        }
    }

    private ShopDocument addDocument(ShopDocument document) {
        try {
            return service.save(document);
        } catch (ExistingUser e) {
            throw new CrudOperationException("A document with this name already exists");
        }
    }

}
