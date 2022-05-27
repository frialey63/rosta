package org.pjp.rosta.service;

import java.util.List;

import org.pjp.rosta.model.User;
import org.pjp.rosta.repository.ShiftRepository;
import org.pjp.rosta.repository.UserRepository;
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

    public UserService(UserRepository userRepository, ShiftRepository shiftRepository) {
        this.userRepository = userRepository;
        this.shiftRepository = shiftRepository;
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
        if (user.getUuid() == null) {
            if (countAll() >= USERS_COUNT_LIMIT) {
                throw new LimitReached();
            }

            userRepository.findByName(user.getName()).ifPresent(u -> {
                throw new ExistingUser();
            });
        } else {
            userRepository.findById(user.getUuid()).ifPresent(existingUser -> {
                String name = user.getName();

                if (!existingUser.getName().equals(name) && userRepository.findByName(name).isPresent()) {
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
