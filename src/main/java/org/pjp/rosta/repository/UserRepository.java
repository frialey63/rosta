package org.pjp.rosta.repository;

import java.util.List;
import java.util.Optional;

import org.pjp.rosta.model.User;
import org.pjp.rosta.model.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    List<User> findAllByUserRole(UserRole[] role);

    List<User> findAllByUserRoleAndEmployee(UserRole[] role, boolean employee);

    List<User> findAllByNameContainingIgnoreCase(String name);

    Optional<User> findByUsername(String username);

}
