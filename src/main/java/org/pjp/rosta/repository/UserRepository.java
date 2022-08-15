package org.pjp.rosta.repository;

import java.util.List;
import java.util.Optional;

import org.pjp.rosta.model.User;
import org.pjp.rosta.model.UserRole;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    @Query(value = "{'userRole':{ $in: ?0}}")
    List<User> findAllByUserRole(UserRole[] role);

    @Query(value = "{'userRole':{ $in: ?0}, 'employee':{ $eq: ?1}}")
    List<User> findAllByUserRoleAndEmployee(UserRole[] role, boolean employee);

    List<User> findAllByNameContainingIgnoreCase(String name);

    Optional<User> findByUsername(String username);

}
