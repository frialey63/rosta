package org.pjp.rosta.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.pjp.rosta.model.User;
import org.pjp.rosta.model.UserRole;
import org.pjp.rosta.util.UuidStr;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

class RostaUserPrincipalTest {

    @Test
    void testGetAuthorities() {
        UserRole userRole = UserRole.WORKER;
        User user = new User(UuidStr.random(), "username", userRole, "name", "password", false, "name@email.com", false, false, false);

        RostaUserPrincipal rup = new RostaUserPrincipal(user);

        Collection<? extends GrantedAuthority> authorities = rup.getAuthorities();

        assertThat(authorities, hasSize(1));
        assertThat(authorities, contains(new SimpleGrantedAuthority("ROLE_WORKER")));
    }

    @Test
    void testGetPassword() {
        String password = "password";
        User user = new User(UuidStr.random(), "username", UserRole.WORKER, "name", password, false, "name@email.com", false, false, false);

        RostaUserPrincipal rup = new RostaUserPrincipal(user);

        assertEquals(password, rup.getPassword());
    }

    @Test
    void testGetUsername() {
        String username = "username";
        User user = new User(UuidStr.random(), username, UserRole.WORKER, "name", "password", false, "name@email.com", false, false, false);

        RostaUserPrincipal rup = new RostaUserPrincipal(user);

        assertEquals(username, rup.getUsername());
    }

    @Test
    void testSetUsername() {
        String newUsername = "newUsername";
        User user = new User(UuidStr.random(), "oldUsername", UserRole.WORKER, "name", "password", false, "name@email.com", false, false, false);

        RostaUserPrincipal rup = new RostaUserPrincipal(user);
        rup.setUsername(newUsername);

        assertEquals(newUsername, rup.getUsername());
    }

    @Test
    void testIsAccountNonExpired() {
        RostaUserPrincipal rup = new RostaUserPrincipal(null);

        assertTrue(rup.isCredentialsNonExpired());
    }

    @Test
    void testIsAccountNonLocked() {
        RostaUserPrincipal rup = new RostaUserPrincipal(null);

        assertTrue(rup.isAccountNonLocked());
    }

    @Test
    void testIsCredentialsNonExpired() {
        RostaUserPrincipal rup = new RostaUserPrincipal(null);

        assertTrue(rup.isCredentialsNonExpired());
    }

    @Test
    void testIsEnabled() {
        User user = new User(UuidStr.random(), "username", UserRole.WORKER, "name", "password", false, "name@email.com", false, false, false);

        RostaUserPrincipal rup = new RostaUserPrincipal(user);

        assertFalse(rup.isEnabled());

        user.setEnabled(true);

        assertTrue(rup.isEnabled());
    }

    @Test
    void testIsPasswordChange() {
        User user = new User(UuidStr.random(), "username", UserRole.WORKER, "name", "password", false, "name@email.com", false, false, false);

        RostaUserPrincipal rup = new RostaUserPrincipal(user);

        assertFalse(rup.isPasswordChange());

        user.setPasswordChange(true);

        assertTrue(rup.isPasswordChange());
    }

    @Test
    void testSetPasswordChange() {
        User user = new User(UuidStr.random(), "username", UserRole.WORKER, "name", "password", false, "name@email.com", false, false, false);

        RostaUserPrincipal rup = new RostaUserPrincipal(user);

        assertFalse(rup.isPasswordChange());

        rup.setPasswordChange(true);

        assertTrue(rup.isPasswordChange());
    }

    @Test
    void testGetAndSetFirst() {
        User user = new User(UuidStr.random(), "username", UserRole.WORKER, "name", "password", false, "name@email.com", false, false, false);

        RostaUserPrincipal rup = new RostaUserPrincipal(user);

        assertTrue(rup.getAndSetFirst(false));

        assertFalse(rup.getAndSetFirst(true));
    }

}
