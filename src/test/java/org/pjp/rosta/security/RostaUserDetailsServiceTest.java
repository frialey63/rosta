package org.pjp.rosta.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pjp.rosta.model.User;
import org.pjp.rosta.model.UserRole;
import org.pjp.rosta.repository.UserRepository;
import org.pjp.rosta.util.UuidStr;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class RostaUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RostaUserDetailsService service;

    @Test
    void testLoadUserByUsername() {

        // GIVEN

        String username = "username";
        User user = new User(UuidStr.random(), username, UserRole.WORKER, "name", "password", false, "name@email.com", false, false, false);
        user.setEnabled(true);

        // WHEN

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // THEN

        UserDetails userDetails = service.loadUserByUsername(username);

        assertNotNull(userDetails);
        assertEquals(user.getUsername(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());

        assertThat(userDetails.getAuthorities(), hasSize(1));
        assertThat(userDetails.getAuthorities(), contains(new SimpleGrantedAuthority(user.getUserRole().getRole())));
    }

    @Test
    void testLoadUserByUsernameNotFound() {

        // GIVEN

        String username = "username";

        // WHEN

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.empty());

        // THEN

        assertThrows(UsernameNotFoundException.class, () -> {
            service.loadUserByUsername(username);
        });
    }

    @Test
    void testLoadUserByUsernameDisabled() {

        // GIVEN

        String username = "username";
        User user = new User(UuidStr.random(), username, UserRole.WORKER, "name", "password", false, "name@email.com", false, false, false);

        // WHEN

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // THEN

        assertThrows(DisabledException.class, () -> {
            service.loadUserByUsername(username);
        });
    }

    @Test
    void testLoadUserByUsernamePasswordExpired() {

        // GIVEN

        LocalDateTime passwordExpiry = LocalDateTime.now().minusDays(1);

        String username = "username";
        User user = new User(UuidStr.random(), username, UserRole.WORKER, "name", "password", false, "name@email.com", false, false, false);
        user.setEnabled(true);
        user.setPasswordExpiry(passwordExpiry.toInstant(ZoneOffset.UTC));

        // WHEN

        Mockito.when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // THEN

        assertThrows(CredentialsExpiredException.class, () -> {
            service.loadUserByUsername(username);
        });
    }

}
