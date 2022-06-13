package org.pjp.rosta.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.pjp.rosta.model.User;
import org.pjp.rosta.repository.ShiftRepository;
import org.pjp.rosta.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    public static final int USERS_COUNT_LIMIT = 1000;

    public static class LimitReached extends RuntimeException {
        private static final long serialVersionUID = 8079202795333924415L;
    }

    public static class ExistingUser extends RuntimeException {
        private static final long serialVersionUID = 78294508656273838L;
    }

    public static class UserInUsage extends RuntimeException {
        private static final long serialVersionUID = 7452108904169685125L;
    }

    private final UserRepository userRepository;

    private final ShiftRepository shiftRepository;

    @Autowired
    public UserService(UserRepository userRepository, ShiftRepository shiftRepository) {
        this.userRepository = userRepository;
        this.shiftRepository = shiftRepository;
    }

    public void initData() {
        userRepository.deleteAll();

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        String id = UUID.randomUUID().toString();
        User user = new User(id, "admin", true, "Admin", "{bcrypt}" + passwordEncoder.encode("password"), true, "admin@gmail.com", false, false);
        userRepository.save(user);
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

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User save(User user) {
        if (user.getUuid() == null) {
            if (countAll() >= USERS_COUNT_LIMIT) {
                throw new LimitReached();
            }

            userRepository.findByUsername(user.getName()).ifPresent(u -> {
                throw new ExistingUser();
            });
        } else {
            userRepository.findById(user.getUuid()).ifPresent(existingUser -> {
                String name = user.getName();

                if (!existingUser.getName().equals(name) && userRepository.findByUsername(name).isPresent()) {
                    throw new ExistingUser();
                }
            });
        }

        return userRepository.save(user);
    }

    public void delete(User user) {
        if (shiftRepository.countAllByUserUuid(user.getUuid()) > 0) {
            throw new UserInUsage();
        }

        userRepository.delete(user);
    }

}
