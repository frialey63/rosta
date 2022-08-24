package org.pjp.rosta.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.pjp.rosta.bean.UserBean;
import org.pjp.rosta.model.User;
import org.pjp.rosta.model.UserRole;
import org.pjp.rosta.repository.UserRepository;
import org.pjp.rosta.security.CrunchifyRandomPasswordGenerator;
import org.pjp.rosta.util.UuidStr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

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

    @Value("${init.data:false}")
    private boolean initData;

    @Value("${initial.manager.password:password}")
    private String initialManagerPassword;

    private final UserRepository userRepository;

    private final EmailService emailService;

    @Autowired
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @PostConstruct
    public void postConstruct() {
        if (initData) {
            userRepository.deleteAll();

            LOGGER.info("initialising data and creating the manager");

            String id = UuidStr.random();
            User user = new User(id, "manager", UserRole.MANAGER, "Manager", ("{bcrypt}" + passwordEncoder.encode(initialManagerPassword)), true, "manager@gmail.com", false, true, false);
            userRepository.save(user);
        }
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public int countAll() {
        return (int) userRepository.count();
    }

    public List<User> findAllNonManager(Boolean employee) {
        if (employee == null) {
            return userRepository.findAllByUserRole(new UserRole[] { UserRole.SUPERVISOR, UserRole.WORKER });
        }

        return userRepository.findAllByUserRoleAndEmployee(new UserRole[] { UserRole.SUPERVISOR, UserRole.WORKER }, employee);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Map<String, User> getAllNonManager() {
        Map<String, User> map = findAll().stream().filter(user -> !user.isManager()).collect(Collectors.toMap(User::getUuid, Function.identity()));

        return Collections.unmodifiableMap(map);
    }

    public User save(User user) {
        if (user.getUuid() == null) {
            userRepository.findByUsername(user.getUsername()).ifPresent(u -> {
                LOGGER.debug("existing user {}", user.getUsername());
                throw new ExistingUser();
            });

            user.setUuid(UuidStr.random());

            if (user.getPassword() == null) {
                String newPassword = passwordGenerator.generatePassword(PASSWORD_LENGTH);

                user.setPassword("{bcrypt}" + passwordEncoder.encode(newPassword));
                user.setPasswordExpiry(null);	// in this scenario it is necessary for the user to perform "forgot password" process
            }
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

    public LocalDateTime updateLastLoggedIn(String username) {
        LOGGER.debug("update last logged-in for {}", username);

        LocalDateTime result = null;

        Optional<User> optUser = userRepository.findByUsername(username);

        if (optUser.isPresent()) {
            User user = optUser.get();

            result = user.getLastLoggedIn();

            user.setLastLoggedIn(LocalDateTime.now());
            userRepository.save(user);
        } else {
            LOGGER.debug("failed to lookup user {}", username);
        }

        return result;
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

    public String registerUser(UserBean userBean) {
        LOGGER.info("registering user {}", userBean);

        String name = userBean.getFirstName() + " " + userBean.getLastName();
        String username = userBean.getEmail().split("\\@")[0];

        String password = "{bcrypt}" + passwordEncoder.encode(userBean.getPassword());
        Instant passwordExpiry = Instant.now().plus(FORGOT_PASSWORD_EXPIRY_HOURS, ChronoUnit.HOURS);

        User user = new User(UuidStr.random(), username, UserRole.WORKER, name, password, true, userBean.getEmail(), true, userBean.isEmployee(), false);
        user.setPasswordChange(true);
        user.setPasswordExpiry(passwordExpiry);

        userRepository.save(user);

        LOGGER.info("user registered as {}", user);

        return username;
    }
}
