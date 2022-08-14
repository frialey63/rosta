package org.pjp.rosta.ui.view.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;

import javax.annotation.security.PermitAll;

import org.pjp.rosta.model.ShopDocument;
import org.pjp.rosta.model.User;
import org.pjp.rosta.service.DocumentService;
import org.pjp.rosta.service.UserService.ExistingUser;
import org.pjp.rosta.ui.view.AbstractView;
import org.pjp.rosta.ui.view.MainLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vaadin.crudui.crud.CrudOperation;
import org.vaadin.crudui.crud.CrudOperationException;
import org.vaadin.crudui.crud.impl.GridCrud;
import org.vaadin.crudui.form.impl.form.factory.DefaultCrudFormFactory;
import org.vaadin.olli.FileDownloadWrapper;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasValueAndElement;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid.SelectionMode;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.AfterNavigationEvent;
import com.vaadin.flow.router.AfterNavigationObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;

@PermitAll
@PageTitle("Shop Documents")
@Route(value = "document", layout = MainLayout.class)
public class ShopDocumentView extends AbstractView implements AfterNavigationObserver {

    private static final long serialVersionUID = 2727631739617042032L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ShopDocumentView.class);

    private static final File DOCUMENTS_FOLDER = DocumentService.FOLDER;

    private static final int MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024;	// 10 MB

    private static final boolean SHOW_NOTIFICATIONS = true;

    private final GridCrud<ShopDocument> crud = new GridCrud<>(ShopDocument.class);

    private final MemoryBuffer memoryBuffer = new MemoryBuffer();

    private Optional<User> optUser;

    private String fileName;

    @Autowired
    private DocumentService documentService;

    public ShopDocumentView() {
        super();

        // grid configuration
        crud.getGrid().setColumns("title", "filename");
        crud.getGrid().setColumnReorderingAllowed(true);
        crud.getGrid().addComponentColumn(doc -> {
            FileDownloadWrapper buttonWrapper = new FileDownloadWrapper(null);
            buttonWrapper.wrapComponent(new Button("Download"));

            buttonWrapper.setResource(new StreamResource(doc.getFilename(), () -> {
                InputStream result = null;

                try {
                    File file = new File(DOCUMENTS_FOLDER, doc.getFilename());

                    result = new FileInputStream(file);
                } catch (FileNotFoundException fnfe) {
                    LOGGER.error("failed to locate file while attempting to download document", fnfe);
                }

                return result;
            }));

            return buttonWrapper;
        });

        crud.setFindAllOperationVisible(false);
        crud.setWidthFull();
        crud.setShowNotifications(SHOW_NOTIFICATIONS);

        crud.setCrudFormFactory(new DefaultCrudFormFactory<>(ShopDocument.class) {
            private static final long serialVersionUID = 6194957473564895897L;

            private CrudOperation operation;

            private Button operationButton;

            @Override
            public Component buildNewForm(CrudOperation operation, ShopDocument domainObject, boolean readOnly, ComponentEventListener<ClickEvent<Button>> cancelButtonClickListener, ComponentEventListener<ClickEvent<Button>> operationButtonClickListener) {
                this.operation = operation;
                return super.buildNewForm(operation, domainObject, readOnly, cancelButtonClickListener, operationButtonClickListener);
            }

            @Override
            protected Button buildOperationButton(CrudOperation operation, ShopDocument domainObject, ComponentEventListener<ClickEvent<Button>> clickListener) {
                operationButton = super.buildOperationButton(operation, domainObject, clickListener);
                operationButton.setEnabled(operation != CrudOperation.ADD);
                return operationButton;
            }

            @Override
            protected void configureForm(FormLayout formLayout, @SuppressWarnings("rawtypes") List<HasValueAndElement> fields) {
                if (operation == CrudOperation.ADD) {
                    Upload upload = new Upload(memoryBuffer);
                    upload.setMaxFileSize(MAX_FILE_SIZE_BYTES);

                    upload.getElement().addEventListener("file-remove", event -> {
                        operationButton.setEnabled(false);
                    });

                    upload.addSucceededListener(event -> {
                        operationButton.setEnabled(true);

                        fileName = event.getFileName();
                    });

                    upload.addFileRejectedListener(event -> {
                        Notification.show(event.getErrorMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
                    });

                    formLayout.add(new Paragraph(), upload);
                    formLayout.getChildren().forEach(c -> formLayout.setColspan(c, 2));
                }
            }
        });

        // form configuration
        crud.getCrudFormFactory().setUseBeanValidation(true);
        crud.getCrudFormFactory().setVisibleProperties("title", "filename");
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.ADD, "title");
        crud.getCrudFormFactory().setVisibleProperties(CrudOperation.UPDATE, "title");
        crud.getCrudFormFactory().setShowNotifications(SHOW_NOTIFICATIONS);

        // logic configuration
        crud.setOperations(
                () -> documentService.findAll(),
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

        setHorizontalComponentAlignment(Alignment.START, crud);
        add(crud);

        // layout configuration
        setMargin(false);
        setPadding(true);
        setSizeFull();
    }

    @Override
    public void afterNavigation(AfterNavigationEvent event) {
        super.afterNavigation(event);

        optUser = userService.findByUsername(getUsername());

        optUser.ifPresent(user -> {
            if (user.isManager()) {
                crud.addUpdateButtonColumn();
            } else {
                crud.setAddOperationVisible(false);
                crud.setUpdateOperationVisible(false);
                crud.setDeleteOperationVisible(false);
            }
        });

        crud.refreshGrid();
    }

    private void deleteDocument(ShopDocument document) {
        documentService.delete(document);

        File file = new File(DOCUMENTS_FOLDER, document.getFilename());
        if (!file.delete()) {
            LOGGER.warn("failed to delete document {}", document);
        }
    }

    private ShopDocument updateDocument(ShopDocument document) {
        try {
            return documentService.save(document);
        } catch (ExistingUser e) {
            throw new CrudOperationException("A document with this name already exists");
        }
    }

    private ShopDocument addDocument(ShopDocument document) {
        File file = new File(DOCUMENTS_FOLDER, fileName);

        try {
            Files.copy(memoryBuffer.getInputStream(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);

            document.setFilename(fileName);

            return documentService.save(document);

        } catch (IOException e) {
            throw new CrudOperationException("Failed to write the uploaded document");
        } catch (ExistingUser e) {
            throw new CrudOperationException("A document with this name already exists");
        }
    }

}
