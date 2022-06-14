package org.pjp.rosta.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.pjp.rosta.model.User;
import org.pjp.rosta.repository.ShiftRepository;
import org.pjp.rosta.repository.UserRepository;
import org.pjp.rosta.security.CrunchifyRandomPasswordGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public static final int USERS_COUNT_LIMIT = 1000;

    public static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

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

    private EmailService emailService;

    @Autowired
    public UserService(UserRepository userRepository, ShiftRepository shiftRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.shiftRepository = shiftRepository;
        this.emailService = emailService;
    }

    public void initData() {
        userRepository.deleteAll();

        String id = UUID.randomUUID().toString();
        User user = new User(id, "admin", true, "Admin", "{bcrypt}" + PASSWORD_ENCODER.encode("password"), true, "admin@gmail.com", false, false);
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

            userRepository.findByUsername(user.getUsername()).ifPresent(u -> {
                throw new ExistingUser();
            });
        } else {
            userRepository.findById(user.getUuid()).ifPresent(existingUser -> {
                String username = user.getUsername();

                if (!existingUser.getUsername().equals(username) && userRepository.findByUsername(username).isPresent()) {
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

    public void forgotPassword(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            CrunchifyRandomPasswordGenerator passwordGenerator = new CrunchifyRandomPasswordGenerator(5);

            String newPassword = passwordGenerator.generatePassword(8);

            try {
                emailService.sendSimpleMessage(user.getEmail(), "The information that you requested", newPassword);

                user.setPassword(newPassword);
                userRepository.save(user);
            } catch (Exception e) {
                LOGGER.warn("failed to send email to address "+ user.getEmail());
            }
        });
    }
}
