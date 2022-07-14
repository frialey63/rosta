package org.pjp.rosta.repository;

import java.util.Optional;

import org.pjp.rosta.model.ShopDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopDocumentRepository extends MongoRepository<ShopDocument, String> {

    Optional<ShopDocument> findByTitle(String title);

}
