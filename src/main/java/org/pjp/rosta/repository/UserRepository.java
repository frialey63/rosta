package org.pjp.rosta.repository;

import java.util.List;
import java.util.Optional;

import org.pjp.rosta.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    List<User> findByNameContainingIgnoreCase(String name);

    Optional<User> findByUsername(String username);

}
