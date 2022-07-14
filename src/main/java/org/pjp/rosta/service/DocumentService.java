package org.pjp.rosta.service;

import java.util.List;
import java.util.UUID;

import org.pjp.rosta.model.ShopDocument;
import org.pjp.rosta.repository.ShopDocumentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DocumentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentService.class);

    public static class ExistingDocument extends RuntimeException {
        private static final long serialVersionUID = -1080429253717683067L;
    }

    private final ShopDocumentRepository repository;

    @Autowired
    public DocumentService(ShopDocumentRepository repository) {
        this.repository = repository;
    }

    public List<ShopDocument> findAll() {
        return repository.findAll();
    }

    public ShopDocument save(ShopDocument document) {
        if (document.getUuid() == null) {
            repository.findByTitle(document.getTitle()).ifPresent(d -> {
                LOGGER.debug("existing document {}", document.getTitle());
                throw new ExistingDocument();
            });

            document.setUuid(UUID.randomUUID().toString());
        } else {
            repository.findById(document.getUuid()).ifPresent(existingDocument -> {
                String title = document.getTitle();

                if (!existingDocument.getTitle().equals(title) && repository.findByTitle(title).isPresent()) {
                    LOGGER.debug("existing document {} for title change", document.getTitle());
                    throw new ExistingDocument();
                }
            });
        }

        LOGGER.debug("saving document {}", document);
        return repository.save(document);
    }

    public void delete(ShopDocument document) {
        repository.delete(document);
    }

}
