package org.pjp.rosta.security;

import java.time.Instant;
import java.util.Optional;

import org.pjp.rosta.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class RostaUserDetailsService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RostaUserDetailsService.class);

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        LOGGER.debug("attemping to load user {}", username);

        Optional<org.pjp.rosta.model.User> optUser = userRepository.findByUsername(username);

        if (optUser.isEmpty()) {
            LOGGER.warn("non-existent user {}", username);
            throw new UsernameNotFoundException(username);
        }

        org.pjp.rosta.model.User user = optUser.get();

        if (!user.isEnabled()) {
            LOGGER.warn("disabled user {}", user);
            throw new DisabledException(username);
        }

        Instant passwordExpiry = user.getPasswordExpiry();

        if ((passwordExpiry != null) && Instant.now().isAfter(passwordExpiry)) {
            LOGGER.warn("credential expired user {}", user);
            throw new CredentialsExpiredException(username);
        }

        LOGGER.info("successfully loaded user {}", user);

        return new RostaUserPrincipal(user);
    }
}
