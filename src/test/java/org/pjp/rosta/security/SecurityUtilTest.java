package org.pjp.rosta.security;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.pjp.rosta.model.User;
import org.pjp.rosta.model.UserRole;
import org.pjp.rosta.util.UuidStr;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;

class SecurityUtilTest {

    @Test
    void testGetAuthenticatedUserAuthenticated() {
        SecurityContext context = new SecurityContextImpl();

        SecurityContextHolder.setContext(context);

        User user = new User(UuidStr.random(), "username", UserRole.WORKER, "name", "password", false, "name@email.com", false, false, false);

        RostaUserPrincipal rup = new RostaUserPrincipal(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(rup, user.getPassword());

        context.setAuthentication(authentication);

        SecurityUtil securityUtil = new SecurityUtil();

        UserDetails userDetails = securityUtil.getAuthenticatedUser();

        assertNotNull(userDetails);
        assertEquals(user.getUsername(), userDetails.getUsername());
        assertEquals(user.getPassword(), userDetails.getPassword());

        assertThat(userDetails.getAuthorities(), hasSize(1));
        assertThat(userDetails.getAuthorities(), contains(new SimpleGrantedAuthority(user.getUserRole().getRole())));
    }

    @Test
    void testGetAuthenticatedUserNotAuthenticated() {
        SecurityContext context = new SecurityContextImpl();

        SecurityContextHolder.setContext(context);

        Authentication authentication = new UsernamePasswordAuthenticationToken(null, null);

        context.setAuthentication(authentication);

        SecurityUtil securityUtil = new SecurityUtil();

        assertNull(securityUtil.getAuthenticatedUser());
    }

}
