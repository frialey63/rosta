package org.pjp.rosta.repository;

import org.pjp.rosta.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {

}
