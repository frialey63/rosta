package org.pjp.rosta.service;

import java.util.List;

import org.pjp.rosta.model.User;
import org.pjp.rosta.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public static final int USERS_COUNT_LIMIT = 1000;

    public static class LimitReached extends RuntimeException {
        private static final long serialVersionUID = 8079202795333924415L;
    }

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public int countAll() {
        return (int) userRepository.count();
    }

    public List<User> findByNameContainingIgnoreCase(String name) {
        return userRepository.findByNameContainingIgnoreCase(name);
    }

    public User save(User user) {
        if (countAll() >= USERS_COUNT_LIMIT) {
            throw new LimitReached();
        }

        return userRepository.save(user);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }

}
