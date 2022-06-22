package org.pjp.rosta.service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.pjp.rosta.model.User;
import org.pjp.rosta.repository.ShiftRepository;
import org.pjp.rosta.repository.UserRepository;
import org.pjp.rosta.security.CrunchifyRandomPasswordGenerator;
import org.pjp.rosta.ui.view.register.UserBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    static final String ADMIN = "admin";

    private static final int PASSWORD_LENGTH = 8;

    private static final int FORGOT_PASSWORD_EXPIRY_HOURS = 1;

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    public static class ExistingUser extends RuntimeException {
        private static final long serialVersionUID = 78294508656273838L;
    }

    public static class UserInUsage extends RuntimeException {
        private static final long serialVersionUID = 7452108904169685125L;
    }

    private final CrunchifyRandomPasswordGenerator passwordGenerator = new CrunchifyRandomPasswordGenerator(5);

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private final UserRepository userRepository;

    private final ShiftRepository shiftRepository;

    private final EmailService emailService;

    @Autowired
    public UserService(UserRepository userRepository, ShiftRepository shiftRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.shiftRepository = shiftRepository;
        this.emailService = emailService;
    }

    public void initData() {
        userRepository.deleteAll();

        String id = UUID.randomUUID().toString();
        User user = new User(id, ADMIN, true, "Administrator", ("{bcrypt}" + passwordEncoder.encode("password")), null, true, "admin@gmail.com", false, false);
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
            userRepository.findByUsername(user.getUsername()).ifPresent(u -> {
                LOGGER.debug("existing user {}", user.getUsername());
                throw new ExistingUser();
            });

            String newPassword = passwordGenerator.generatePassword(PASSWORD_LENGTH);

            user.setUuid(UUID.randomUUID().toString());
            user.setPassword("{bcrypt}" + passwordEncoder.encode(newPassword));
            user.setPasswordExpiry(null);	// in this scenario it is necessary for the user to perform "forgot password" process
        } else {
            userRepository.findById(user.getUuid()).ifPresent(existingUser -> {
                String username = user.getUsername();

                if (!existingUser.getUsername().equals(username) && userRepository.findByUsername(username).isPresent()) {
                    LOGGER.debug("existing user {} for username change", user.getUsername());
                    throw new ExistingUser();
                }
            });
        }

        LOGGER.debug("saving user {}", user);
        return userRepository.save(user);
    }

    public void delete(User user) {
        if (shiftRepository.countAllByUserUuid(user.getUuid()) > 0) {
            throw new UserInUsage();
        }

        userRepository.delete(user);
    }

    public int forgotPassword(String username) {
        LOGGER.info("forgot password for {}", username);

        Optional<User> optUser = userRepository.findByUsername(username);

        if (optUser.isPresent()) {
            User user = optUser.get();

            String newPassword = passwordGenerator.generatePassword(PASSWORD_LENGTH);
            Instant passwordExpiry = Instant.now().plus(FORGOT_PASSWORD_EXPIRY_HOURS, ChronoUnit.HOURS);

            emailService.sendSimpleMessage(user.getEmail(), "The information that you requested", newPassword);

            user.setPassword("{bcrypt}" + passwordEncoder.encode(newPassword));
            user.setPasswordChange(true);
            user.setPasswordExpiry(passwordExpiry);

            userRepository.save(user);
        } else {
            LOGGER.debug("failed to lookup user {}", username);
        }

        return FORGOT_PASSWORD_EXPIRY_HOURS;
    }

    public boolean changePassword(User user, String oldPassword, String password) {
        boolean result = false;

        if (passwordEncoder.matches(oldPassword, user.getPassword().replaceFirst("\\{bcrypt\\}", ""))) {
            LOGGER.info("changing password for {}", user.getUsername());

            user.setPassword("{bcrypt}" + passwordEncoder.encode(password));
            user.setPasswordChange(false);
            user.setPasswordExpiry(null);

            userRepository.save(user);

            result = true;
        }

        return result;
    }

    public void registerUser(UserBean userBean) {
        LOGGER.info("registered user {}", userBean);
    }
}
